package com.servet.loan.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LoanRepository — SQL ile veri doğrulama (H2 in-memory)")
class LoanRepositoryTest {

    private Connection connection;
    private LoanRepository repository;

    @BeforeEach
    void setUpDatabase() throws SQLException {
        // Her test için ayrı, boş bir veritabanı → testler birbirini etkilemez
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        try (Statement st = connection.createStatement()) {
            st.execute("RUNSCRIPT FROM 'classpath:schema.sql' CHARSET 'UTF-8'");
            st.execute("RUNSCRIPT FROM 'classpath:data.sql' CHARSET 'UTF-8'");
        }
        repository = new LoanRepository(connection);
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    @Nested
    @DisplayName("Sorgular")
    class Queries {

        @Test
        @DisplayName("WHERE: duruma göre başvurular")
        void findByStatus() throws SQLException {
            assertThat(repository.findIdsByStatus("SUBMITTED")).containsExactly(106, 109);
            assertThat(repository.findIdsByStatus("UNKNOWN")).isEmpty();
        }

        @Test
        @DisplayName("GROUP BY: duruma göre başvuru sayısı")
        void countByStatus() throws SQLException {
            assertThat(repository.countByStatus()).containsOnly(
                    entry("APPROVED", 2), entry("CANCELLED", 1), entry("DRAFT", 1), entry("PAID_OUT", 1),
                    entry("REJECTED", 1), entry("SUBMITTED", 2), entry("UNDER_REVIEW", 1));
        }

        @Test
        @DisplayName("SUM: onaylanan toplam tutar; kayıt yoksa 0")
        void totalAmount() throws SQLException {
            assertThat(repository.totalAmountByStatus("APPROVED")).isEqualByComparingTo("27000.00");
            assertThat(repository.totalAmountByStatus("UNKNOWN")).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("JOIN + HAVING: birden fazla başvurusu olan müşteriler")
        void customersWithMultipleApplications() throws SQLException {
            assertThat(repository.customersWithMoreThan(1))
                    .containsExactly("Ayşe Yılmaz", "Mehmet Demir", "Zeynep Arslan");
        }

        @Test
        @DisplayName("ORDER BY + LIMIT: en yüksek 3 tutar")
        void topAmounts() throws SQLException {
            assertThat(repository.topAmountIds(3)).containsExactly(107, 109, 105);
        }
    }

    @Nested
    @DisplayName("Veri tutarlılığı — veritabanındaki hatalı kayıtları yakala")
    class DataIntegrity {

        @Test
        @DisplayName("R4 ihlali: notu 580'in altında olup onaylanan başvuru")
        void approvedWithLowCreditScore() throws SQLException {
            assertThat(repository.findApprovedWithLowCreditScore()).containsExactly(104);
        }

        @Test
        @DisplayName("R2 ihlali: 1.000–50.000 aralığı dışındaki tutar")
        void amountOutOfRange() throws SQLException {
            assertThat(repository.findAmountsOutOfRange()).containsExactly(107);
        }
    }
}