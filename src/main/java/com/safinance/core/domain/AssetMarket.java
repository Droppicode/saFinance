package com.safinance.core.domain;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.Instant;

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

    /** Duração de um bloco/tick de mercado, em segundos. */
    private static final int BLOCK_SECONDS = 10;

    /** Instante do último movimento de preços; base do catch-up e da persistência. */
    private static volatile Instant lastMove = Instant.now();

    /** Callback disparado após cada movimento de preços (ex: persistir o estado). */
    private static volatile Runnable onMove;

    private static final List<Asset> AVAILABLE_ASSETS = List.of(
        new Stock("FTX1", "FTX1", "Fictitious Tech", "Ficticious Tech Co.", 0.15),
        new Stock("VRPX", "VRPX", "Vertex Dynamics", "Vertex Dynamics & Cia.", 0.12),
        new RealEstateFund("NOVA11", "NOVA11", "Fundo Imobiliário Nova", "Logística", 0.10),
        new RealEstateFund("LUNA11", "LUNA11", "Fundo Imobiliário Luna", "Papel", 0.14),
        new FixedIncome("PRA100", "PRA100", "Prazo Certo 100 Dias", 0.065, 0.02),
        new FixedIncome("SIL200", "SIL200", "Série Silvestre 200 Dias", 0.055, 0.02)
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
     * Avança os preços de todos os ativos em UM bloco de 10 segundos, aplicando
     * uma variação aleatória composta escalada pela volatilidade de cada ativo.
     * Use no refresh ao vivo: cada tick visível na tela = uma chamada.
     */
    public static synchronized void advanceOneBlock() {
        for (var ticker : CURRENT_PRICES.keySet()) {
            double current = CURRENT_PRICES.get(ticker);
            double volatility = PRICE_VOLATILITY.getOrDefault(ticker, 0.1);
            CURRENT_PRICES.put(ticker, round2(stepOnce(current, volatility)));
        }
        registerMove(Instant.now());
    }

    /**
     * Adianta os preços por {@code blocks} blocos de 10s decorridos, usando um
     * único sorteio cuja magnitude cresce com a raiz de {@code blocks}. Use no
     * "catch-up" ao voltar à tela depois de um tempo — evita o disparo que
     * repetir {@link #advanceOneBlock()} milhares de vezes causaria.
     *
     * @param blocks número de blocos de 10s decorridos (valores <= 0 são ignorados)
     */
    public static synchronized void catchUp(long blocks) {
        if (blocks <= 0) return;
        for (var ticker : CURRENT_PRICES.keySet()) {
            double current = CURRENT_PRICES.get(ticker);
            double volatility = PRICE_VOLATILITY.getOrDefault(ticker, 0.1);
            CURRENT_PRICES.put(ticker, round2(stepScaled(current, volatility, blocks)));
        }
        // Avança o marco pelos blocos aplicados, preservando o resto do bloco
        // parcial para o próximo catch-up.
        registerMove(lastMove.plusSeconds(blocks * BLOCK_SECONDS));
    }

    /**
     * Aplica de uma vez os blocos de 10s decorridos desde o último movimento.
     * Chame ao (re)abrir uma tela que exibe preços: cobre o tempo passado em
     * outras telas e, com o estado restaurado via {@link #restoreState}, também
     * o tempo com o aplicativo fechado. Sem bloco completo decorrido, não faz nada.
     */
    public static synchronized void catchUpToNow() {
        catchUp(blocksBetween(lastMove, Instant.now()));
    }

    /**
     * Restaura preços e instante do último movimento (ex: carregados do disco).
     * Tickers desconhecidos são ignorados; os ausentes mantêm o preço corrente.
     */
    public static synchronized void restoreState(Map<String, Double> prices, Instant lastMoveInstant) {
        if (prices != null) {
            for (var entry : prices.entrySet()) {
                if (entry.getValue() != null && CURRENT_PRICES.containsKey(entry.getKey())) {
                    CURRENT_PRICES.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (lastMoveInstant != null) {
            lastMove = lastMoveInstant;
        }
    }

    /** Instante do último movimento de preços. */
    public static Instant lastMoveInstant() {
        return lastMove;
    }

    /** Registra um callback executado após cada movimento de preços (ex: salvar em disco). */
    public static void setOnMove(Runnable callback) {
        onMove = callback;
    }

    /** Atualiza o marco do último movimento e notifica o callback de persistência. */
    private static void registerMove(Instant instant) {
        lastMove = instant;
        Runnable callback = onMove;
        if (callback != null) {
            try {
                callback.run();
            } catch (Exception ignored) {
                // Falha ao persistir não pode derrubar o mercado.
            }
        }
    }

    /**
     * Compatibilidade: uma operação de mercado (compra/venda) avança um bloco.
     */
    public static void refreshPricesAfterOperation() {
        advanceOneBlock();
    }

    /** Um passo composto: preço * (1 + gaussiana * volatilidade), com piso em 1.0. */
    private static double stepOnce(double price, double volatility) {
        double factor = 1.0 + RANDOM.nextGaussian() * volatility;
        return Math.max(1.0, price * factor);
    }

    /** Sorteio único escalado por raiz(blocks): preço * (1 + gaussiana * volatilidade * raiz(blocks)), piso em 1.0. */
    private static double stepScaled(double price, double volatility, long blocks) {
        double factor = 1.0 + RANDOM.nextGaussian() * volatility * Math.sqrt(blocks);
        return Math.max(1.0, price * factor);
    }

    /** Arredonda um preço para duas casas decimais. */
    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Número de blocos inteiros de 10s entre dois instantes (nunca negativo).
     * O chamador (a tela/persistência) guarda o instante do último fechamento e
     * passa aqui para calcular quantos blocos aplicar no catch-up.
     */
    public static long blocksBetween(Instant from, Instant to) {
        long seconds = Duration.between(from, to).getSeconds();
        return Math.max(0, seconds / BLOCK_SECONDS);
    }

    /**
     * Retorna uma cópia imutável dos preços correntes para exibição.
     *
     * @return mapa de ticker -> preço (imutável)
     */
    public static synchronized Map<String, Double> snapshotPrices() {
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
