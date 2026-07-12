package com.safinance.core.domain;

import java.util.Objects;

public abstract class BaseAsset implements Asset {
    private final String id;
    private final String ticker;
    private final String name;

    protected BaseAsset(String id, String ticker, String name) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("O ID do ativo não pode ser nulo.");
        if (ticker == null || ticker.isBlank()) throw new IllegalArgumentException("O ticker não pode ser nulo.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("O nome do ativo não pode ser nulo.");
        this.id = id;
        this.ticker = ticker;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTicker() {
        return ticker;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BaseAsset other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", name, ticker);
    }
}
