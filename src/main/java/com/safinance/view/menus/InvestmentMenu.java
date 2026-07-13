package com.safinance.view.menus;

import com.safinance.core.domain.Asset;
import com.safinance.core.domain.WalletAccount;
import com.safinance.core.domain.User;
import com.safinance.view.AbstractMenu;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

import java.util.List;

public class InvestmentMenu extends AbstractMenu {

    private final User accountOwner;
    private final MenuContext ctx;
    private final BaseMenu previousMenu;
    private final String selectedWalletName;

    public InvestmentMenu(User accountOwner, MenuContext ctx, BaseMenu previousMenu) {
        this(accountOwner, ctx, previousMenu, null);
    }

    public InvestmentMenu(User accountOwner, MenuContext ctx, BaseMenu previousMenu, String selectedWalletName) {
        this.accountOwner = accountOwner;
        this.ctx = ctx;
        this.previousMenu = previousMenu;
        this.selectedWalletName = selectedWalletName;

        registerCommand("1", "Comprar ativo", prompt -> {
            WalletAccount wallet = fetchWallet(prompt);
            if (wallet == null) {
                prompt.printWarning("Não é possível comprar sem uma conta carteira.");
                prompt.readString("Pressione Enter para voltar.");
                return this;
            }
            return handleBuy(prompt, wallet);
        });

        registerCommand("2", "Vender ativo", prompt -> {
            WalletAccount wallet = fetchWallet(prompt);
            if (wallet == null || wallet.getPortfolio().isEmpty()) {
                prompt.printWarning("Não há ativos para vender.");
                prompt.readString("Pressione Enter para voltar.");
                return this;
            }
            return handleSell(prompt, wallet);
        });

        registerCommand("3", "Ver portfólio", prompt -> {
            WalletAccount wallet = fetchWallet(prompt);
            showPortfolio(prompt, wallet);
            return this;
        });

        registerCommand("0", "Voltar/Sair", prompt -> previousMenu);
    }

    private WalletAccount fetchWallet(PromptService promptService) {
        List<WalletAccount> wallets = ctx.investmentUseCase().getWalletAccountsByUser(accountOwner);

        if (wallets == null || wallets.isEmpty()) {
            return null;
        } else if (selectedWalletName != null) {
            return ctx.investmentUseCase().getWalletAccountByUserAndName(accountOwner, selectedWalletName).orElse(null);
        } else if (wallets.size() > 1) {
            List<String> walletsStrings = wallets.stream().map(WalletAccount::getName).toList();
            String wantedWallet = promptService.readWithOptions("Selecione a carteira de investimentos que você quer acessar: ", walletsStrings);
            return ctx.investmentUseCase().getWalletAccountByUserAndName(accountOwner, wantedWallet).orElse(null);
        } else {
            return wallets.getFirst();
        }
    }

    @Override
    protected void printHeader(PromptService promptService) {
        ctx.investmentUseCase().catchUpToNow();
        promptService.printHeader("Área de Investimentos");
        List<WalletAccount> wallets = ctx.investmentUseCase().getWalletAccountsByUser(accountOwner);

        if (wallets == null || wallets.isEmpty()) {
            promptService.printWarning("Nenhuma conta carteira encontrada. Crie uma conta carteira antes de investir.");
            promptService.printInfo("");
            promptService.printInfo("Você pode criar uma conta carteira no menu de gerenciamento de contas.");
        } else if (selectedWalletName != null) {
            WalletAccount selectedWallet = ctx.investmentUseCase().getWalletAccountByUserAndName(accountOwner, selectedWalletName).orElse(null);
            
            if (selectedWallet != null) {
                promptService.printInfo(String.format("Carteira selecionada: %s", selectedWallet.getName()));
                promptService.printInfo(String.format("Saldo disponível: R$ %.2f", selectedWallet.getBalance()));
                promptService.printInfo("");
                promptService.printInfo("Portfólio atual:");
                if (selectedWallet.getPortfolio().isEmpty()) {
                    promptService.printInfo("  - Nenhuma posição no momento.");
                } else {
                    selectedWallet.getPortfolio().values().forEach(position -> {
                        Asset asset = position.getAsset();
                        promptService.printInfo(String.format("  - %s: %.4f unidades a R$ %.2f (Preço médio: R$ %.2f)",
                            asset.getTicker(), position.getQuantity(), ctx.investmentUseCase().getAssetPrice(asset.getTicker()), position.getAveragePrice()));
                    });
                }
            }
        }
        promptService.printInfo("");
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        String option = promptService.readStringWithRefresh(
            "> Escolha uma opção: ",
            10_000L,
            () -> {
                try {
                    ctx.investmentUseCase().advanceOneBlock();
                    StringBuilder block = new StringBuilder("\n══════════════════════════════════════════════\n        Área de Investimentos\n══════════════════════════════════════════════");
                    
                    WalletAccount wallet = null;
                    if (selectedWalletName != null) {
                        wallet = ctx.investmentUseCase().getWalletAccountByUserAndName(accountOwner, selectedWalletName).orElse(null);
                    } else {
                        wallet = ctx.investmentUseCase().getWalletAccountByUser(accountOwner).orElse(null);
                    }
                    if (wallet != null) {
                        block.append("\nSaldo disponível: R$ ").append(String.format("%.2f", wallet.getBalance()));
                        block.append("\n\nPortfólio atual:");
                        if (wallet.getPortfolio().isEmpty()) {
                            block.append("\n  - Nenhuma posição no momento.");
                        } else {
                            double totalValor = 0;
                            for (var position : wallet.getPortfolio().values()) {
                                Asset asset = position.getAsset();
                                double currentPrice = ctx.investmentUseCase().getAssetPrice(asset.getTicker());
                                double totalPosition = position.getQuantity() * currentPrice;
                                double gainLoss = totalPosition - (position.getQuantity() * position.getAveragePrice());
                                String gainLossStr = gainLoss >= 0 ? "+" : "";
                                
                                block.append(String.format("\n  - %s: %.4f unidades | Preço atual R$ %.2f | Valor total R$ %.2f (%s%.2f)",
                                    asset.getTicker(), 
                                    position.getQuantity(), 
                                    currentPrice, 
                                    totalPosition,
                                    gainLossStr,
                                    gainLoss));
                                totalValor += totalPosition;
                            }
                            block.append(String.format("\n  Valor total do portfólio: R$ %.2f", totalValor));
                        }
                    }
                    
                    block.append("\n\n1 - Comprar ativo");
                    block.append("\n2 - Vender ativo");
                    block.append("\n3 - Ver portfólio");
                    block.append("\n0 - Voltar/Sair");
                    
                    promptService.printLive(block.toString());
                } catch (Exception ignored) {
                }
            }
        );

        return processOption(promptService, option);
    }

    private BaseMenu handleBuy(PromptService promptService, WalletAccount wallet) {
        promptService.printInfo("Ativos disponíveis:");
        ctx.investmentUseCase().getMarketSummary().forEach(promptService::printInfo);
        promptService.printInfo("");
        promptService.printInfo("(Os preços atualizam automaticamente a cada 10s.)");

        String ticker = promptService.readStringWithRefresh(
            "Digite o ticker do ativo que deseja comprar: ",
            10_000L,
            () -> {
                try {
                    ctx.investmentUseCase().advanceOneBlock();
                    String stamp = java.time.LocalTime.now().withNano(0).toString();
                    StringBuilder block = new StringBuilder("── Ativos disponíveis [" + stamp + "] ──");
                    ctx.investmentUseCase().getMarketSummary().forEach(line -> block.append('\n').append(line));
                    promptService.printLive(block.toString());
                } catch (Exception ignored) {
                }
            }
        );
        
        Asset asset;
        try {
            asset = ctx.investmentUseCase().findAssetByTicker(ticker);
            ctx.investmentUseCase().getAssetPrice(asset.getTicker());
        } catch (Exception e) {
            promptService.printError("Ticker inválido ou não disponível.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        Double quantity = promptService.readDouble("Quantidade a comprar: ");
        if (quantity == null || quantity <= 0) {
            promptService.printError("Quantidade inválida.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        double price = ctx.investmentUseCase().getAssetPrice(asset.getTicker());
        try {
            WalletAccount updated = ctx.investmentUseCase().buyAsset(wallet, asset, quantity, price);
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
            promptService.printInfo(String.format("  - %s: %.4f unidades", position.getAssetTicker(), position.getQuantity())));
        promptService.printInfo("");

        String ticker = promptService.readString("Digite o ticker do ativo que deseja vender: ").trim().toUpperCase();
        var position = wallet.getPortfolio().get(ticker);
        if (position == null) {
            promptService.printError("Ticker não encontrado no portfólio.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        Double quantity = promptService.readDouble("Quantidade a vender: ");
        if (quantity == null || quantity <= 0) {
            promptService.printError("Quantidade inválida.");
            promptService.readString("Pressione Enter para voltar.");
            return this;
        }

        double price = ctx.investmentUseCase().getAssetPrice(position.getAssetTicker());
        try {
            WalletAccount updated = ctx.investmentUseCase().sellAsset(wallet, ticker, quantity, price);
            promptService.printSuccess(String.format("Venda concluída: %s x %.4f por R$ %.2f cada. Novo saldo: R$ %.2f", ticker, quantity, price, updated.getBalance()));
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
            promptService.printInfo(String.format("- %s (%s): %.4f unidades | Preço médio R$ %.2f | Valor atual R$ %.2f", position.getAssetName(), position.getAssetTicker(), position.getQuantity(), position.getAveragePrice(), ctx.investmentUseCase().getAssetPrice(position.getAssetTicker())));
        });
        promptService.readString("Pressione Enter para voltar.");
    }
}
