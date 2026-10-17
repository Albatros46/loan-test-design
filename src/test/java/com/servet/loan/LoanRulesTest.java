package com.servet.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("LoanRules — R1, R2, R3")
class LoanRulesTest {

    @Nested
    @DisplayName("R1 Yaş — Equivalence Partitioning")
    class AgePartitions {

        @ParameterizedTest(name = "{0}: yaş {1} → uygun mu = {2}")
        @CsvSource({
                "EP2 reşit değil,  10, false",
                "EP3 geçerli,      40, true",
                "EP4 sınır üstü,   90, false"
        })
        void validInputPartitions(String partition, int age, boolean expected) {
            assertThat(LoanRules.isEligibleAge(age)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "yaş {0} → geçersiz girdi")
        @ValueSource(ints = { -1, 150 }) // EP1 ve EP5
        void invalidInputPartitions(int age) {
            assertThatIllegalArgumentException().isThrownBy(() -> LoanRules.isEligibleAge(age));
        }
    }

    @Nested
    @DisplayName("R1 Yaş — 3-value Boundary Value Analysis")
    class AgeBoundaries {

        @ParameterizedTest(name = "yaş {0} → uygun mu = {1}")
        @CsvSource({
                "17, false", "18, true", "19, true",   // alt sınır 18
                "69, true",  "70, true", "71, false",  // üst sınır 70
                "0,  false", "120, false"              // geçerli girdi aralığının uçları
        })
        void ageBoundaries(int age, boolean expected) {
            assertThat(LoanRules.isEligibleAge(age)).isEqualTo(expected);
        }

        @ParameterizedTest(name = "yaş {0} → geçersiz girdi")
        @ValueSource(ints = { -1, 121 })
        void justOutsideInputRange(int age) {
            assertThatIllegalArgumentException().isThrownBy(() -> LoanRules.isEligibleAge(age));
        }
    }

    @Nested
    @DisplayName("R2 Tutar — 3-value Boundary Value Analysis")
    class AmountBoundaries {

        @ParameterizedTest(name = "{0} € → geçerli mi = {1}")
        @CsvSource({
                "999,      false", "1000,  true", "1001,  true",
                "49999,    true",  "50000, true", "50001, false",
                "999.99,   false", "50000.01, false"   // kuruşlu sınırlar
        })
        void amountBoundaries(BigDecimal amount, boolean expected) {
            assertThat(LoanRules.isValidAmount(amount)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Boş tutar reddedilir")
        void nullAmount() {
            assertThatNullPointerException().isThrownBy(() -> LoanRules.isValidAmount(null));
        }
    }

    @Nested
    @DisplayName("R3 Faiz — EP + 2-value BVA")
    class InterestRate {

        @ParameterizedTest(name = "not {0} → faiz %{1}")
        @CsvSource({
                "300, ", "579, ",       // kredi verilmez (boş = Optional.empty)
                "580, 12", "669, 12",
                "670, 9",  "739, 9",
                "740, 6",  "850, 6"
        })
        void rateByCreditScore(int creditScore, BigDecimal expectedRate) {
            if (expectedRate == null) {
                assertThat(LoanRules.interestRate(creditScore)).isEmpty();
            } else {
                assertThat(LoanRules.interestRate(creditScore)).hasValueSatisfying(
                        rate -> assertThat(rate).isEqualByComparingTo(expectedRate));
            }
        }

        @ParameterizedTest(name = "not {0} → geçersiz")
        @ValueSource(ints = { 299, 851 })
        void outOfRangeScore(int creditScore) {
            assertThatIllegalArgumentException().isThrownBy(() -> LoanRules.interestRate(creditScore));
        }
    }
}