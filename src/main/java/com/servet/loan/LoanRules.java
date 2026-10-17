package com.servet.loan;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

/** Spesifikasyondaki R1, R2 ve R3 kuralları. */
public final class LoanRules {

    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 70;
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("1000");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("50000");

    private LoanRules() {
    }

    /** R1: 18–70 uygun; 0–120 dışı geçersiz girdi. */
    public static boolean isEligibleAge(int age) {
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Geçersiz yaş: " + age);
        }
        return age >= MIN_AGE && age <= MAX_AGE;
    }

    /** R2: 1.000 – 50.000 (dahil). */
    public static boolean isValidAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "Tutar boş olamaz");
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    /** R3: Kredi notuna göre yıllık faiz (%). Boş Optional = kredi verilmez. */
    public static Optional<BigDecimal> interestRate(int creditScore) {
        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("Geçersiz kredi notu: " + creditScore);
        }
        if (creditScore < 580) {
            return Optional.empty();
        }
        if (creditScore < 670) {
            return Optional.of(new BigDecimal("12"));
        }
        if (creditScore < 740) {
            return Optional.of(new BigDecimal("9"));
        }
        return Optional.of(new BigDecimal("6"));
    }
}