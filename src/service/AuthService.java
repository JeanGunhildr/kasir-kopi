package service;

import model.Admin;
import model.Kasir;
import model.Pengguna;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Layanan Logika Bisnis untuk Autentikasi dan Pengelolaan Akun Kasir (FR-02).
 */
public class AuthService {
    private static AuthService instance;
    private Pengguna currentUser;

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public Pengguna login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'AKTIF'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        currentUser = new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("nama"), rs.getString("status"));
                    } else {
                        currentUser = new Kasir(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("nama"), rs.getString("status"));
                    }
                    return currentUser;
                }
            }
        }
        return null;
    }

    public Pengguna getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    // Manajemen Akun Kasir (FR-02)
    public List<Pengguna> getDaftarKasir() throws SQLException {
        List<Pengguna> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'KASIR' ORDER BY id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kasir(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("nama"), rs.getString("status")));
            }
        }
        return list;
    }

    public boolean simpanKasir(Pengguna u) throws SQLException {
        if (u.getId() > 0) {
            String sql = "UPDATE users SET username=?, password=?, nama=? WHERE id=?";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u.getUsername());
                ps.setString(2, u.getPassword());
                ps.setString(3, u.getNama());
                ps.setInt(4, u.getId());
                return ps.executeUpdate() > 0;
            }
        } else {
            String sql = "INSERT INTO users (username, password, nama, role, status) VALUES (?, ?, ?, 'KASIR', 'AKTIF')";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, u.getUsername());
                ps.setString(2, u.getPassword());
                ps.setString(3, u.getNama());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean ubahStatusKasir(int id, String status) throws SQLException {
        String sql = "UPDATE users SET status=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }
}
