package model;

/**
 * Subclass Kasir mewarisi Pengguna (Penerapan Inheritance & Polymorphism).
 * Bertanggung jawab melayani transaksi penjualan, penerapan diskon, dan cetak struk.
 */
public class Kasir extends Pengguna {
    public Kasir(int id, String username, String password, String nama, String status) {
        super(id, username, password, nama, "KASIR", status);
    }

    @Override
    public String getDeskripsiAkses() {
        return "Kasir Operasional: Melayani Pemesanan Menu, Penerapan Promo, dan Cetak Struk Bukti Bayar.";
    }
}
