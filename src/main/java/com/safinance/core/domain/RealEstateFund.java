package com.safinance.core.domain;

public class RealEstateFund extends BaseAsset {
    private final String sector;
    public RealEstateFund(String id, String ticker, String name, String sector, double volatility) {
        super(id, ticker, name, volatility);
        this.sector = sector;
    }

    public String getSector() {
        return this.sector;
    }
}
