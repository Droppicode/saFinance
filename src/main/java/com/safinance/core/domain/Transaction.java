package com.safinance.core.domain;
import java.time.LocalDateTime;
public interface Transaction extends Entity {
    double getAmount();
    LocalDateTime getDate();
    String getDescription();
    String getAccountId();
}
