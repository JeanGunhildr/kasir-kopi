package service;

import util.DatabaseUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Layanan Logika Bisnis untuk Laporan Penjualan (Harian, Mingguan, Bulanan) & Menu Terlaris (FR-04).
 */
public class LaporanService {

    public static class LaporanData {
        public double totalPendapatan = 0;
        public int totalTransaksi = 0;
        public List<Object[]> menuTerlaris = new ArrayList<>(); // [Nama, Qty, Total]
    }

    public LaporanData getLaporan(String periode) throws SQLException {
        LaporanData data = new LaporanData();
        String kondisiWaktu = switch (periode.toLowerCase()) {
            case "harian" -> "DATE(waktu) = CURRENT_DATE";
            case "mingguan" -> "YEARWEEK(waktu, 1) = YEARWEEK(CURRENT_DATE, 1)";
            default -> "MONTH(waktu) = MONTH(CURRENT_DATE) AND YEAR(waktu) = YEAR(CURRENT_DATE)";
        };

        try (Connection conn = DatabaseUtil.getConnection()) {
            // 1. Total Pendapatan & Total Transaksi
            String sqlTotal = "SELECT COUNT(*) AS total_trx, COALESCE(SUM(total), 0) AS omset FROM transaksi WHERE " + kondisiWaktu;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlTotal)) {
                if (rs.next()) {
                    data.totalTransaksi = rs.getInt("total_trx");
                    data.totalPendapatan = rs.getDouble("omset");
                }
            }

            // 2. Menu Paling Laku
            String sqlTop = "SELECT dt.nama_menu, SUM(dt.qty) AS total_qty, SUM(dt.subtotal) AS total_uang " +
                    "FROM detail_transaksi dt JOIN transaksi t ON dt.transaksi_id = t.id " +
                    "WHERE " + kondisiWaktu + " " +
                    "GROUP BY dt.nama_menu ORDER BY total_qty DESC LIMIT 5";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlTop)) {
                while (rs.next()) {
                    data.menuTerlaris.add(new Object[]{
                            rs.getString("nama_menu"),
                            rs.getInt("total_qty"),
                            rs.getDouble("total_uang")
                    });
                }
            }
        }
        return data;
    }
}
