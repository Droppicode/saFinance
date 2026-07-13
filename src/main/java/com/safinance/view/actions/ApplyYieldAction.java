package com.safinance.view.actions;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.time.temporal.ChronoUnit;

import com.safinance.core.domain.SavingsAccount;
import com.safinance.view.BaseMenu;
import com.safinance.view.MenuContext;
import com.safinance.view.PromptService;

/**
 * Ação para aplicar rendimento em uma conta de poupança específica.
 */
public class ApplyYieldAction implements BaseMenu {

    private final MenuContext ctx;
    private final SavingsAccount sa;
    private final Supplier<BaseMenu> onComplete;

    public ApplyYieldAction(MenuContext ctx, SavingsAccount sa, Supplier<BaseMenu> onComplete) {
        this.ctx = ctx;
        this.sa = sa;
        this.onComplete = onComplete;
    }

    @Override
    public void renderHeader(PromptService promptService) {
        promptService.printHeader("Simulação de Rendimento da Poupança");
        
        YearMonth current = YearMonth.now();
        
        promptService.printInfo("O nosso banco simula a passagem do tempo na economia real.");
        promptService.printInfo("Data atual: " + current + "\n");
    }

    @Override
    public List<String> getOptions() {
        return Collections.emptyList();
    }

    @Override
    public BaseMenu handleInput(PromptService promptService) {
        try {
            String input = promptService.readString("Digite o ano e mês para simular (formato: YYYY-MM) ou '0' para sair: ");
            if (input.trim().equals("0")) {
                return onComplete.get();
            }
            
            YearMonth targetMonth = YearMonth.parse(input.trim());
            YearMonth currentMonth = YearMonth.now();
            
            if (targetMonth.isBefore(currentMonth) || targetMonth.equals(currentMonth)) {
                promptService.printError("O mês alvo deve ser no futuro em relação ao mês atual (" + currentMonth + ").");
                promptService.readString("Pressione Enter para tentar novamente.");
                return this;
            }
            
            double simulatedBalance = sa.getBalance();
            YearMonth cursor = currentMonth.plusMonths(1);
            
            // OTIMIZAÇÃO: Solicita a taxa do mês final ANTES do loop. 
            // Isso faz com que a classe Bank gere todos os meses intermediários de uma vez 
            // na memória e salve no JSON apenas 1 vez (evitando centenas de linhas no arquivo).
            ctx.bankUseCase().getYieldRate(targetMonth);
            
            // Loop para aplicar os juros compostos mês a mês
            while (!cursor.isAfter(targetMonth)) {
                double rate = ctx.bankUseCase().getYieldRate(cursor);
                simulatedBalance += simulatedBalance * rate;
                cursor = cursor.plusMonths(1);
            }
            
            double totalYield = simulatedBalance - sa.getBalance();
            
            long totalMonthsPassed = ChronoUnit.MONTHS.between(currentMonth, targetMonth);
            long simYears = totalMonthsPassed / 12;
            long simMonths = totalMonthsPassed % 12;
            
            promptService.printSuccess("--- Resultado da Simulação (Juros Compostos) ---");
            promptService.printInfo(String.format("Período projetado: %d anos e %d meses (de %s até %s)", simYears, simMonths, currentMonth, targetMonth));
            promptService.printInfo(String.format("Rendimento acumulado total: R$ %.2f", totalYield));
            promptService.printInfo(String.format("Saldo total projetado no final: R$ %.2f\n", simulatedBalance));
            
        } catch (java.time.format.DateTimeParseException e) {
            promptService.printError("Formato de data inválido.");
        } catch (IllegalArgumentException e) {
            promptService.printError(e.getMessage());
        } catch (Exception e) {
            promptService.printError("Erro ao simular rendimento.");
        }
        promptService.readString("Pressione Enter para simular outro mês.");
        return this; // Retorna para a mesma tela de simulação
    }
}
