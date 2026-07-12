package com.safinance.core.domain;

public class Stock extends BaseAsset {
    private final String companyName;

    public Stock(String id, String ticker, String name, String companyName) {
        super(id, ticker, name);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return this.companyName;
    }
}
