package com.safinance.view.menus;

import com.safinance.core.domain.Asset;
import com.safinance.core.domain.AssetMarket;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.User;
import com.safinance.core.usecases.AccountUseCase;
import com.safinance.core.usecases.InvestmentUseCase;
import com.safinance.core.usecases.TransactionUseCase;
import com.safinance.view.BaseMenu;
import com.safinance.view.PromptService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class InvestmentMenu implements BaseMenu {

    private final User user;
    private final InvestmentUseCase investmentUseCase;
    private final TransactionUseCase transactionUseCase;
    private final Map<String, Supplier<BaseMenu>> transitions = new HashMap<>();

    public InvestmentMenu(User user, AccountUseCase accountUseCase, InvestmentUseCase investmentUseCase, TransactionUseCase transactionUseCase) {
        this.user = user;
        this.investmentUseCase = investmentUseCase;
        this.transactionUseCase = transactionUseCase;

        registerTransition("1", () -> this, transitions);
        registerTransition("2", () -> this, transitions);
        registerTransition("3", () -> this, transitions);
        registerTransition("0", () -> new UserMenu(user, accountUseCase, investmentUseCase, this.transactionUseCase), transitions);
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Área de Investimentos");
        WalletAccount wallet = investmentUseCase.getWalletAccount(user);

        if (wallet == null) {
            promptService.printWarning("Nenhuma conta carteira encontrada. Crie uma conta carteira antes de investir.");
            promptService.printInfo("");
            promptService.printInfo("Você pode criar uma conta carteira no menu de gerenciamento de contas.");
        } else {
            promptService.printInfo(String.format("Saldo disponível: R$ %.2f", wallet.getBalance()));
            promptService.printInfo("");
            promptService.printInfo("Portfólio atual:");
            if (wallet.getPortfolio().isEmpty()) {
                promptService.printInfo("  - Nenhuma posição no momento.");
            } else {
                wallet.getPortfolio().values().forEach(position -> {
                    Asset asset = position.getAsset();
                    promptService.printInfo(String.format("  - %s: %.4f unidades a R$ %.2f (Preço médio: R$ %.2f)",
                        asset.getTicker(), position.getQuantity(), AssetMarket.priceFor(asset.getTicker()), position.getAveragePrice()));
                });
            }
        }

        promptService.printInfo("");
        promptService.printMenuOptions(
            "Comprar ativo",
            "Vender ativo",
            "Ver portfólio"
        );
    }

    @Override
    public List<String> getOptions() {
        return new ArrayList<>(transitions.keySet());
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readString("> Escolha uma opção: ");

        Supplier<BaseMenu> transition = transitions.get(option);

        if (transition == null) {
            promptService.printError("Opção inválida.");
            promptService.readString("Pressione Enter para tentar novamente.");
            return this;
        }

        WalletAccount wallet = investmentUseCase.getWalletAccount(user);
        //TODO: fazer um menu/form intermediario pra selecionar walletAccount (caso o usuario tenha mais de uma wallet account)

        switch (option) {
            case "1":
                if (wallet == null) {
                    promptService.printWarning("Não é possível comprar sem uma conta carteira.");
                    promptService.readString("Pressione Enter para voltar.");
                    return this;
                }
                return handleBuy(promptService, wallet);
            case "2":
                if (wallet == null || wallet.getPortfolio().isEmpty()) {
                    promptService.printWarning("Não há ativos para vender.");
                    promptService.readString("Pressione Enter para voltar.");
                    return this;
                }
                return handleSell(promptService, wallet);
            case "3":
                showPortfolio(promptService, wallet);
                return this;
            case "0":
                return transition.get();
            default:
                promptService.printError("Opção inválida.");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
        }
    }

    private BaseMenu handleBuy(PromptService promptService, WalletAccount wallet) {
        promptService.printInfo("Ativos disponíveis:");
        AssetMarket.marketSummary().forEach(promptService::printInfo);
        promptService.printInfo("");
        promptService.printInfo("(Os preços atualizam automaticamente a cada 10s.)");

        // Refresh ao vivo: enquanto o usuário escolhe o ativo, uma thread em segundo
        // plano avança o mercado a cada 10s e reimprime a lista acima do prompt.
        String ticker = promptService.readStringWithRefresh(
            "Digite o ticker do ativo que deseja comprar: ",
            10_000L,
            () -> {
                try {
                    AssetMarket.advanceOneBlock();
                    String stamp = java.time.LocalTime.now().withNano(0).toString();
                    // Bloco único: uma só chamada de printLive = um só redraw do prompt.
                    StringBuilder block = new StringBuilder("── Ativos disponíveis [" + stamp + "] ──");
                    AssetMarket.marketSummary().forEach(line -> block.append('\n').append(line));
                    promptService.printLive(block.toString());
                } catch (Exception ignored) {
                    // Um erro pontual no refresh não deve derrubar a thread.
                }
            }
        );
        Asset asset = AssetMarket.findByTicker(ticker);
        if (asset == null || AssetMarket.priceFor(asset.getTicker()) == null) {
            promptService.printError("Ticker inválido ou não disponível.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(promptService.readString("Quantidade a comprar: ").trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            promptService.printError("Quantidade inválida.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        // O preço é o vigente no momento da compra, não o da listagem inicial —
        // ele pode ter mudado durante os refreshes.
        double price = AssetMarket.priceFor(asset.getTicker());
        try {
            WalletAccount updated = investmentUseCase.buyAsset(wallet, asset, quantity, price);
            promptService.printSuccess(String.format("Compra concluída: %s x %.4f por R$ %.2f cada. Novo saldo: R$ %.2f", asset.getTicker(), quantity, price, updated.getBalance()));
        } catch (Exception e) {
            promptService.printError("Erro ao comprar ativo: " + e.getMessage());
        }

        promptService.readString("Pressione Enter para continuar.");
        return this;
    }

    private BaseMenu handleSell(PromptService promptService, WalletAccount wallet) {
        promptService.printInfo("Ativos no portfólio:");
        wallet.getPortfolio().values().forEach(position ->
            promptService.printInfo(String.format("  - %s: %.0f unidades", position.getAsset().getTicker(), position.getQuantity())));
        promptService.printInfo("");

        String ticker = promptService.readString("Digite o ticker do ativo que deseja vender: ").trim();
        var position = wallet.getPortfolio().get(ticker);
        if (position == null) {
            promptService.printError("Ticker não encontrado no portfólio.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(promptService.readString("Quantidade a vender: ").trim());
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            promptService.printError("Quantidade inválida.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        double price = AssetMarket.priceFor(position.getAsset().getTicker());
        try {
            WalletAccount updated = investmentUseCase.sellAsset(wallet, ticker, quantity, price);
            promptService.printSuccess(String.format("Venda concluída: %s x %.0f por R$ %.2f cada. Novo saldo: R$ %.2f", ticker, quantity, price, updated.getBalance()));
        } catch (Exception e) {
            promptService.printError("Erro ao vender ativo: " + e.getMessage());
        }

        promptService.readString("Pressione Enter para continuar.");
        return this;
    }

    private void showPortfolio(PromptService promptService, WalletAccount wallet) {
        if (wallet == null || wallet.getPortfolio().isEmpty()) {
            promptService.printInfo("Nenhum ativo registrado no portfólio.");
            promptService.readString("Pressione Enter para voltar.");
            return;
        }

        promptService.printInfo("Portfólio detalhado:");
        wallet.getPortfolio().values().forEach(position -> {
            Asset asset = position.getAsset();
            promptService.printInfo(String.format("- %s (%s): %.0f unidades | Preço médio R$ %.2f | Valor atual R$ %.2f", asset.getName(), asset.getTicker(), position.getQuantity(), position.getAveragePrice(), AssetMarket.priceFor(asset.getTicker())));
        });
        promptService.readString("Pressione Enter para voltar.");
    }
}
