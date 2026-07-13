package com.safinance.core.domain;

import java.util.List;
import java.util.Map;

/**
 * Contrato para um mercado de ativos.
 */
public interface Market {
    /**
     * Retorna a lista de ativos disponíveis no mercado.
     */
    List<Asset> availableAssets();

    /**
     * Procura um ativo pelo ticker (case-insensitive).
     */
    Asset findByTicker(String ticker);

    /**
     * Retorna o preço corrente conhecido para um ticker.
     */
    Double priceFor(String ticker);

    /**
     * Avança os preços de todos os ativos.
     */
    void refreshPricesAfterOperation();

    /**
     * Retorna uma cópia dos preços correntes para exibição.
     */
    Map<String, Double> snapshotPrices();
}
