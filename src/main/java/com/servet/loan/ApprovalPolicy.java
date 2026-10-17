package com.servet.loan;

import java.math.BigDecimal;

/** Spesifikasyondaki R4: onay kararı. */
public final class ApprovalPolicy {

    public enum Decision {
        APPROVE, MANUAL_REVIEW, REJECT
    }

    static final int MIN_CREDIT_SCORE = 580;
    static final BigDecimal MIN_MONTHLY_INCOME = new BigDecimal("2000");

    private ApprovalPolicy() {
    }

    public static Decision decide(boolean hasDefault, int creditScore, BigDecimal monthlyIncome) {
        if (hasDefault) {
            return Decision.REJECT;
        }
        if (creditScore < MIN_CREDIT_SCORE) {
            return Decision.REJECT;
        }
        return monthlyIncome.compareTo(MIN_MONTHLY_INCOME) >= 0 ? Decision.APPROVE : Decision.MANUAL_REVIEW;
    }
}