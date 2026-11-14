package repository;

import model.Menu;
import util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository Akses Data Menu Kopi, Non-Kopi, Makanan & Stok (FR-01, FR-03).
 */
public class MenuRepository {

    public List<Menu> findAll(String kategoriFilter) throws SQLException {
        List<Menu> list = new ArrayList<>();
        String sql = "SELECT * FROM menu";
        if (kategoriFilter != null && !kategoriFilter.equalsIgnoreCase("Semua")) {
            sql += " WHERE kategori = '" + kategoriFilter + "'";
        }
        sql += " ORDER BY id ASC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Menu(
                        rs.getInt("id"),
                        rs.getString("kode"),
                        rs.getString("nama"),
                        rs.getString("kategori"),
                        rs.getDouble("harga"),
                        rs.getInt("stok")
                ));
            }
        }
        return list;
    }

    public boolean save(Menu m) throws SQLException {
        if (m.getId() > 0) {
            String sql = "UPDATE menu SET kode=?, nama=?, kategori=?, harga=?, stok=? WHERE id=?";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, m.getKode());
                ps.setString(2, m.getNama());
                ps.setString(3, m.getKategori());
                ps.setDouble(4, m.getHarga());
                ps.setInt(5, m.getStok());
                ps.setInt(6, m.getId());
                return ps.executeUpdate() > 0;
            }
        } else {
            String sql = "INSERT INTO menu (kode, nama, kategori, harga, stok) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, m.getKode());
                ps.setString(2, m.getNama());
                ps.setString(3, m.getKategori());
                ps.setDouble(4, m.getHarga());
                ps.setInt(5, m.getStok());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean updateStok(int id, int stokBaru) throws SQLException {
        String sql = "UPDATE menu SET stok=? WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stokBaru);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM menu WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Menu findById(int id) throws SQLException {
        String sql = "SELECT * FROM menu WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Menu(
                            rs.getInt("id"),
                            rs.getString("kode"),
                            rs.getString("nama"),
                            rs.getString("kategori"),
                            rs.getDouble("harga"),
                            rs.getInt("stok")
                    );
                }
            }
        }
        return null;
    }
}
