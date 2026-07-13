package com.safinance.core.domain;
public interface Asset extends Entity {
    String getTicker();
    String getName();
    double calculateNextPrice(double currentPrice);
}
