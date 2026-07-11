package com.safinance.core.domain;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The banking institution.
 *
 * <p>Unlike accounts and transactions (immutable value objects), the
 * {@code Bank} is a long-lived, <b>mutable</b> entity: it represents the
 * institution itself and accumulates state over time (the monthly savings yield
 * rates). A single instance is created in {@code Main} and injected into whoever
 * needs it, instead of a static Singleton, to avoid global mutable state.</p>
 */
public class Bank {

    /** Monthly savings yield rates, indexed by month. Grows on demand. */
    private final Map<YearMonth, Double> yieldRates = new HashMap<>();

    /** First month for which a rate exists (the bank's inception). */
    private final YearMonth inceptionMonth;

    /**
     * Most recent month with a known rate. Rates are always filled contiguously
     * from {@link #inceptionMonth} up to this point, so every month in between is
     * guaranteed to be present in {@link #yieldRates}.
     */
    private YearMonth lastKnownMonth;

    /** Randomness source used to generate rates for missing months. */
    private final Random random = new Random();

    /** Lower/upper bounds for a generated monthly yield (business rule, ~0.4%–0.6%). */
    private static final double MIN_RATE = 0.004;
    private static final double MAX_RATE = 0.006;

    /**
     * Creates the bank anchored at a starting month and its rate.
     *
     * @param startMonth the bank's inception month (earliest month with a rate)
     * @param startRate  the yield rate for {@code startMonth}
     */
    public Bank(YearMonth startMonth, double startRate) {
        if (startMonth == null) throw new IllegalArgumentException("Start month cannot be null.");
        if (startRate < 0) throw new IllegalArgumentException("Yield rate cannot be negative.");
        this.inceptionMonth = startMonth;
        this.lastKnownMonth = startMonth;
        this.yieldRates.put(startMonth, startRate);
    }

    /**
     * Returns the savings yield rate for the given month, generating any missing
     * months along the way.
     *
     * <p>If the requested month has no rate yet, every month from the last known
     * month up to it is filled with a random rate first. This models the rule
     * that skipping from, say, month 3 to month 7 generates months 4, 5, 6 and 7.</p>
     *
     * @param month the month whose rate is requested
     * @return the yield rate for {@code month}
     * @throws IllegalArgumentException if {@code month} is null or predates inception
     */
    public double getYieldRate(YearMonth month) {
        if (month == null) throw new IllegalArgumentException("Month cannot be null.");
        // Fail-fast: the bank cannot invent rates for a time before it existed.
        if (month.isBefore(inceptionMonth)) {
            throw new IllegalArgumentException("No yield rate available before the bank's inception month.");
        }
        // Fill any gap between the last known month and the requested one.
        if (month.isAfter(lastKnownMonth)) {
            generateMissingRatesUpTo(month);
        }
        return yieldRates.get(month);
    }

    /**
     * Manually sets (or overrides) the yield rate for a month — e.g. an admin
     * correcting a rate. If the month lies ahead of the known range, the gap is
     * filled with random rates first so no holes are left behind.
     *
     * @param month the month to set
     * @param rate  the yield rate to assign
     */
    public void setYieldRate(YearMonth month, double rate) {
        if (month == null) throw new IllegalArgumentException("Month cannot be null.");
        if (rate < 0) throw new IllegalArgumentException("Yield rate cannot be negative.");
        if (month.isBefore(inceptionMonth)) {
            throw new IllegalArgumentException("Cannot set a rate before the bank's inception month.");
        }
        if (month.isAfter(lastKnownMonth)) {
            generateMissingRatesUpTo(month); // fill the gap, then override just below
        }
        yieldRates.put(month, rate);
    }

    /**
     * Fills every month from {@code lastKnownMonth + 1} up to {@code target}
     * (inclusive) with a randomly generated rate, then advances the known range.
     */
    private void generateMissingRatesUpTo(YearMonth target) {
        YearMonth cursor = lastKnownMonth.plusMonths(1);
        while (!cursor.isAfter(target)) {
            yieldRates.put(cursor, randomRate());
            cursor = cursor.plusMonths(1);
        }
        lastKnownMonth = target;
    }

    /** Draws a random monthly yield within the configured business bounds. */
    private double randomRate() {
        return MIN_RATE + (MAX_RATE - MIN_RATE) * random.nextDouble();
    }
}
