package com.safinance.core.domain;

import java.time.Instant;
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

    /**
     * Avança os preços de todos os ativos em UM bloco de 10 segundos, aplicando
     * uma variação aleatória composta escalada pela volatilidade de cada ativo.
     * Use no refresh ao vivo: cada tick visível na tela = uma chamada.
     */
    void advanceOneBlock();

    /**
     * Aplica de uma vez os blocos de 10s decorridos desde o último movimento.
     * Chame ao (re)abrir uma tela que exibe preços: cobre o tempo passado em
     * outras telas e também o tempo com o aplicativo fechado.
     */
    void catchUpToNow();

    /**
     * Retorna um resumo textual do mercado (ticker, nome e preço atual).
     */
    List<String> marketSummary();

    /**
     * Restaura preços e instante do último movimento (ex: carregados do disco).
     */
    void restoreState(Map<String, Double> prices, Instant lastMoveInstant);

    /**
     * Registra um callback executado após cada movimento de preços (ex: salvar em disco).
     */
    void setOnMove(Runnable callback);

    /**
     * Instante do último movimento de preços.
     */
    Instant lastMoveInstant();
}
