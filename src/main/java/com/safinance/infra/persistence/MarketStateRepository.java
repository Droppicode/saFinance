package com.safinance.infra.persistence;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * Persistência do estado do mercado simulado (preços correntes e instante do
 * último movimento) em um único arquivo JSON sobrescrito a cada movimento.
 *
 * <p>É o que permite os preços continuarem "andando" entre execuções: ao
 * reabrir o aplicativo, o estado salvo é restaurado e o catch-up aplica os
 * blocos de 10s decorridos desde o último movimento registrado.</p>
 */
public class MarketStateRepository {

    /** Estado serializado: preços por ticker e epoch (segundos) do último movimento. */
    public static class MarketState {
        private Map<String, Double> prices;
        private long lastMoveEpochSeconds;

        public MarketState() {
        }

        public MarketState(Map<String, Double> prices, long lastMoveEpochSeconds) {
            this.prices = prices;
            this.lastMoveEpochSeconds = lastMoveEpochSeconds;
        }

        public Map<String, Double> getPrices() {
            return prices;
        }

        public Instant getLastMove() {
            return Instant.ofEpochSecond(lastMoveEpochSeconds);
        }
    }

    private final String filePath;
    private final Gson gson;

    public MarketStateRepository(String filePath, Gson gson) {
        this.filePath = filePath;
        this.gson = gson;

        File parent = new File(filePath).getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    /**
     * Carrega o estado salvo, ou {@code null} se o arquivo não existe ou está
     * ilegível — nesse caso o mercado parte dos preços base.
     */
    public MarketState load() {
        File file = new File(filePath);
        if (!file.exists()) return null;
        try (FileReader reader = new FileReader(file)) {
            return gson.fromJson(reader, MarketState.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** Sobrescreve o arquivo com o estado corrente do mercado. */
    public synchronized void save(Map<String, Double> prices, Instant lastMove) {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(new MarketState(prices, lastMove.getEpochSecond()), writer);
        } catch (IOException e) {
            System.err.println("⚠ Não foi possível salvar o estado do mercado: " + e.getMessage());
        }
    }
}
