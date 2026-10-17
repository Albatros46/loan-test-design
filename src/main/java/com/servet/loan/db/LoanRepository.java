package com.servet.loan.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Veritabanı doğrulamasında tester'ların sık yazdığı sorgular (düz JDBC). */
public class LoanRepository {

    private final Connection connection;

    public LoanRepository(Connection connection) {
        this.connection = connection;
    }

    /** WHERE + ORDER BY */
    public List<Integer> findIdsByStatus(String status) throws SQLException {
        String sql = "SELECT id FROM loan_application WHERE status = ? ORDER BY id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            return readIds(ps);
        }
    }

    /** GROUP BY + COUNT */
    public Map<String, Integer> countByStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS cnt FROM loan_application GROUP BY status ORDER BY status";
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("cnt"));
            }
        }
        return counts;
    }

    /** SUM + COALESCE (hiç kayıt yoksa NULL yerine 0) */
    public BigDecimal totalAmountByStatus(String status) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM loan_application WHERE status = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    /** JOIN + GROUP BY + HAVING */
    public List<String> customersWithMoreThan(int applicationCount) throws SQLException {
        String sql = """
                SELECT c.name
                FROM customer c
                JOIN loan_application a ON a.customer_id = c.id
                GROUP BY c.id, c.name
                HAVING COUNT(*) > ?
                ORDER BY c.name
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, applicationCount);
            List<String> names = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
            }
            return names;
        }
    }

    /** ORDER BY ... DESC + LIMIT */
    public List<Integer> topAmountIds(int limit) throws SQLException {
        String sql = "SELECT id FROM loan_application ORDER BY amount DESC, id LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit);
            return readIds(ps);
        }
    }

    /** Veri tutarlılığı (R4): onaylanmış ama kredi notu 580'in altında olan başvurular. */
    public List<Integer> findApprovedWithLowCreditScore() throws SQLException {
        String sql = """
                SELECT a.id
                FROM loan_application a
                JOIN customer c ON c.id = a.customer_id
                WHERE a.status IN ('APPROVED', 'PAID_OUT') AND c.credit_score < 580
                ORDER BY a.id
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            return readIds(ps);
        }
    }

    /** Veri tutarlılığı (R2): tutar aralığı dışındaki başvurular. */
    public List<Integer> findAmountsOutOfRange() throws SQLException {
        String sql = "SELECT id FROM loan_application WHERE amount NOT BETWEEN 1000 AND 50000 ORDER BY id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            return readIds(ps);
        }
    }

    private static List<Integer> readIds(PreparedStatement ps) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
        }
        return ids;
    }
}