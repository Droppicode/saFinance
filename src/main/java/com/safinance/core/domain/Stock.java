package com.safinance.core.domain;

public class Stock extends BaseAsset {
    private final String companyName;

    public Stock(String id, String ticker, String name, String companyName, double volatility) {
        super(id, ticker, name, volatility);
        if (companyName == null || companyName.isBlank()) throw new IllegalArgumentException("O nome da empresa não pode ser nulo ou vazio.");
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return this.companyName;
    }
}
