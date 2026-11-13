package repository;

import model.DetailTransaksi;
import model.Transaksi;
import util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository Akses Data Transaksi Penjualan & Pemotongan Stok (FR-05).
 */
public class TransaksiRepository {

    public String generateNoFaktur() {
        LocalDate now = LocalDate.now();
        String prefix = String.format("INV%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        long rand = (long) (Math.random() * 9000L + 1000L);
        return prefix + rand;
    }

    public boolean save(Transaksi t) throws SQLException {
        String sqlTrans = "INSERT INTO transaksi (no_faktur, kasir_nama, kode_promo, subtotal, diskon, pajak, total, bayar, kembalian) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO detail_transaksi (transaksi_id, menu_id, nama_menu, harga, qty, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlStock = "UPDATE menu SET stok = stok - ? WHERE id = ? AND stok >= ?";

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            int transId;
            try (PreparedStatement ps = conn.prepareStatement(sqlTrans, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getNoFaktur());
                ps.setString(2, t.getKasirNama());
                ps.setString(3, t.getKodePromo());
                ps.setDouble(4, t.getSubtotal());
                ps.setDouble(5, t.getDiskon());
                ps.setDouble(6, t.getPajak());
                ps.setDouble(7, t.getTotal());
                ps.setDouble(8, t.getBayar());
                ps.setDouble(9, t.getKembalian());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        transId = keys.getInt(1);
                        t.setId(transId);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            for (DetailTransaksi item : t.getItems()) {
                // Potong stok real-time (FR-03)
                try (PreparedStatement psStock = conn.prepareStatement(sqlStock)) {
                    psStock.setInt(1, item.getQty());
                    psStock.setInt(2, item.getMenuId());
                    psStock.setInt(3, item.getQty());
                    int updated = psStock.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        throw new SQLException("Stok untuk " + item.getNamaMenu() + " tidak mencukupi!");
                    }
                }

                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    psDetail.setInt(1, transId);
                    psDetail.setInt(2, item.getMenuId());
                    psDetail.setString(3, item.getNamaMenu());
                    psDetail.setDouble(4, item.getHarga());
                    psDetail.setInt(5, item.getQty());
                    psDetail.setDouble(6, item.getSubtotal());
                    psDetail.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    public List<DetailTransaksi> findDetailsByTransaksiId(int transaksiId) throws SQLException {
        List<DetailTransaksi> items = new ArrayList<>();
        String sql = "SELECT * FROM detail_transaksi WHERE transaksi_id = ? ORDER BY id ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transaksiId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new DetailTransaksi(
                            rs.getInt("id"),
                            rs.getInt("transaksi_id"),
                            rs.getInt("menu_id"),
                            rs.getString("nama_menu"),
                            rs.getDouble("harga"),
                            rs.getInt("qty"),
                            rs.getDouble("subtotal")
                    ));
                }
            }
        }
        return items;
    }

    public List<Transaksi> findAll() throws SQLException {
        List<Transaksi> list = new ArrayList<>();
        String sql = "SELECT * FROM transaksi ORDER BY id DESC LIMIT 100";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaksi t = new Transaksi(rs.getString("no_faktur"), rs.getString("kasir_nama"));
                int transId = rs.getInt("id");
                t.setId(transId);
                t.setKodePromo(rs.getString("kode_promo"));
                t.setSubtotal(rs.getDouble("subtotal"));
                t.setDiskon(rs.getDouble("diskon"));
                t.setPajak(rs.getDouble("pajak"));
                t.setTotal(rs.getDouble("total"));
                t.setBayar(rs.getDouble("bayar"));
                t.setKembalian(rs.getDouble("kembalian"));
                Timestamp ts = rs.getTimestamp("waktu");
                if (ts != null) t.setWaktu(ts.toLocalDateTime());
                list.add(t);
            }
        }

        // Ambil rincian detail barang terjual untuk masing-masing transaksi
        for (Transaksi t : list) {
            t.getItems().clear();
            t.getItems().addAll(findDetailsByTransaksiId(t.getId()));
        }

        return list;
    }
}
