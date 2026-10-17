package com.servet.loan;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.servet.loan.ApprovalPolicy.Decision;

@DisplayName("ApprovalPolicy — R4 Decision Table")
class ApprovalPolicyTest {

    /**
     * Tam karar tablosu: 3 koşul → 2^3 = 8 kural.
     * Not: E = 700 / H = 500 · Gelir: E = 2500 / H = 1500
     */
    @ParameterizedTest(name = "{0}: temerrüt={1}, not={2}, gelir={3} → {4}")
    @CsvSource({
            "K1, true,  700, 2500, REJECT",
            "K2, true,  700, 1500, REJECT",
            "K3, true,  500, 2500, REJECT",
            "K4, true,  500, 1500, REJECT",
            "K5, false, 700, 2500, APPROVE",
            "K6, false, 700, 1500, MANUAL_REVIEW",
            "K7, false, 500, 2500, REJECT",
            "K8, false, 500, 1500, REJECT"
    })
    void fullDecisionTable(String rule, boolean hasDefault, int creditScore, BigDecimal income, Decision expected) {
        assertThat(ApprovalPolicy.decide(hasDefault, creditScore, income)).isEqualTo(expected);
    }

    /** Karar tablosu + BVA: koşulların tam sınırlarında karar doğru mu? */
    @ParameterizedTest(name = "not={0}, gelir={1} → {2}")
    @CsvSource({
            "579, 2000,    REJECT",
            "580, 2000,    APPROVE",
            "580, 1999.99, MANUAL_REVIEW"
    })
    void conditionBoundaries(int creditScore, BigDecimal income, Decision expected) {
        assertThat(ApprovalPolicy.decide(false, creditScore, income)).isEqualTo(expected);
    }
}