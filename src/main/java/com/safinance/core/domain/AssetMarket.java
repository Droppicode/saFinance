package com.safinance.core.domain;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Mercado de ativos simulado para a aplicação.
 *
 * <p>Esta classe mantém um catálogo de ativos disponíveis e um mapa de preços
 * em memória (`CURRENT_PRICES`). Os preços são estáveis para leitura até que
 * uma operação de compra/venda chame {@link #refreshPricesAfterOperation()},
 * que aplica movimentações de preço baseadas em volatilidade.
 *
 * <p>Os métodos desta classe são utilitários estáticos; a classe não deve ser
 * instanciada.</p>
 */
public final class AssetMarket {

    private static final Random RANDOM = new Random();

    private static final List<Asset> AVAILABLE_ASSETS = List.of(
        new Stock("FTX1", "FTX1", "Fictitious Tech", "Ficticious Tech Co."),
        new Stock("VRPX", "VRPX", "Vertex Dynamics", "Vertex Dynamics & Cia."),
        new RealEstateFund("NOVA11", "NOVA11", "Fundo Imobiliário Nova", "Logística"),
        new RealEstateFund("LUNA11", "LUNA11", "Fundo Imobiliário Luna", "Papel"),
        new FixedIncome("PRA100", "PRA100", "Prazo Certo 100 Dias", 0.065),
        new FixedIncome("SIL200", "SIL200", "Série Silvestre 200 Dias", 0.055)
    );

    private static final Map<String, Double> BASE_PRICES = Map.of(
        "FTX1", 85.00,
        "VRPX", 134.30,
        "NOVA11", 98.20,
        "LUNA11", 72.50,
        "PRA100", 1000.00,
        "SIL200", 1000.00
    );

    private static final Map<String, Double> PRICE_VOLATILITY = Map.of(
        "FTX1", 0.15,
        "VRPX", 0.12,
        "NOVA11", 0.10,
        "LUNA11", 0.14,
        "PRA100", 0.02,
        "SIL200", 0.02
    );

    /**
     * Preços correntes em memória. Chame {@link #refreshPricesAfterOperation()} para
     * avançar os preços após uma operação que afete o mercado.
     */
    private static final Map<String, Double> CURRENT_PRICES = initializeCurrentPrices();

    private static Map<String, Double> initializeCurrentPrices() {
        var prices = new java.util.HashMap<String, Double>();
        for (var asset : AVAILABLE_ASSETS) {
            prices.put(asset.getTicker(), BASE_PRICES.getOrDefault(asset.getTicker(), 1.0));
        }
        return prices;
    }

    private AssetMarket() {
    }

    /**
     * Retorna a lista de ativos disponíveis no mercado.
     *
     * @return lista imutável de ativos disponíveis
     */
    public static List<Asset> availableAssets() {
        return AVAILABLE_ASSETS;
    }

    /**
     * Procura um ativo pelo ticker (case-insensitive).
     *
     * @param ticker código do ativo
     * @return o ativo se encontrado, ou {@code null} caso contrário
     */
    public static Asset findByTicker(String ticker) {
        return AVAILABLE_ASSETS.stream()
            .filter(asset -> asset.getTicker().equalsIgnoreCase(ticker))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retorna o preço corrente conhecido para um ticker.
     *
     * @param ticker código do ativo
     * @return preço corrente ou {@code null} se o ticker não existir
     */
    public static Double priceFor(String ticker) {
        if (ticker == null) return null;
        return CURRENT_PRICES.get(ticker.toUpperCase());
    }

    /**
     * Avança os preços de todos os ativos aplicando uma variação aleatória
     * baseada na volatilidade configurada. Este método deve ser chamado após
     * operações de mercado (compra/venda) para simular movimentação de preços.
     */
    public static void refreshPricesAfterOperation() {
        for (var ticker : CURRENT_PRICES.keySet()) {
            Double current = CURRENT_PRICES.get(ticker);
            Double volatility = PRICE_VOLATILITY.getOrDefault(ticker, 0.1);
            double randomFactor = 1.0 + (RANDOM.nextGaussian() * volatility);
            double nextPrice = Math.max(1.0, current * randomFactor);
            CURRENT_PRICES.put(ticker, Math.round(nextPrice * 100.0) / 100.0);
        }
    }

    /**
     * Retorna uma cópia imutável dos preços correntes para exibição.
     *
     * @return mapa de ticker -> preço (imutável)
     */
    public static Map<String, Double> snapshotPrices() {
        return Map.copyOf(CURRENT_PRICES);
    }

    /**
     * Retorna um resumo textual do mercado (ticker, nome e preço atual).
     *
     * @return lista de strings formatadas com o resumo do mercado
     */
    public static List<String> marketSummary() {
        Map<String, Double> currentPrices = snapshotPrices();
        return AVAILABLE_ASSETS.stream()
            .map(asset -> String.format("%s - %s | R$ %.2f", asset.getTicker(), asset.getName(), currentPrices.get(asset.getTicker())))
            .collect(Collectors.toList());
    }
}
