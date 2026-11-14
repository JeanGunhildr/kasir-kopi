package repository;

import model.Promo;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository Akses Data Kode Promo & Ketentuan Diskon (FR-07, FR-08).
 */
public class PromoRepository {

    public List<Promo> findAll() throws SQLException {
        List<Promo> list = new ArrayList<>();
        String sql = "SELECT * FROM promo ORDER BY id DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Promo(
                        rs.getInt("id"),
                        rs.getString("kode"),
                        rs.getString("nama_promo"),
                        rs.getString("tipe_diskon"),
                        rs.getDouble("nilai_diskon"),
                        rs.getDouble("min_belanja"),
                        rs.getDate("tgl_mulai").toLocalDate(),
                        rs.getDate("tgl_selesai").toLocalDate(),
                        rs.getString("status")
                ));
            }
        }
        return list;
    }

    public Promo findByKode(String kode) throws SQLException {
        String sql = "SELECT * FROM promo WHERE UPPER(kode) = UPPER(?) AND status = 'AKTIF'";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Promo(
                            rs.getInt("id"),
                            rs.getString("kode"),
                            rs.getString("nama_promo"),
                            rs.getString("tipe_diskon"),
                            rs.getDouble("nilai_diskon"),
                            rs.getDouble("min_belanja"),
                            rs.getDate("tgl_mulai").toLocalDate(),
                            rs.getDate("tgl_selesai").toLocalDate(),
                            rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    public boolean save(Promo p) throws SQLException {
        if (p.getId() > 0) {
            String sql = "UPDATE promo SET kode=?, nama_promo=?, tipe_diskon=?, nilai_diskon=?, min_belanja=?, tgl_mulai=?, tgl_selesai=?, status=? WHERE id=?";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.getKode());
                ps.setString(2, p.getNamaPromo());
                ps.setString(3, p.getTipeDiskon());
                ps.setDouble(4, p.getNilaiDiskon());
                ps.setDouble(5, p.getMinBelanja());
                ps.setDate(6, Date.valueOf(p.getTglMulai()));
                ps.setDate(7, Date.valueOf(p.getTglSelesai()));
                ps.setString(8, p.getStatus());
                ps.setInt(9, p.getId());
                return ps.executeUpdate() > 0;
            }
        } else {
            String sql = "INSERT INTO promo (kode, nama_promo, tipe_diskon, nilai_diskon, min_belanja, tgl_mulai, tgl_selesai, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.getKode());
                ps.setString(2, p.getNamaPromo());
                ps.setString(3, p.getTipeDiskon());
                ps.setDouble(4, p.getNilaiDiskon());
                ps.setDouble(5, p.getMinBelanja());
                ps.setDate(6, Date.valueOf(p.getTglMulai()));
                ps.setDate(7, Date.valueOf(p.getTglSelesai()));
                ps.setString(8, p.getStatus());
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM promo WHERE id=?";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
