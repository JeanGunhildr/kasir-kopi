package model;

/**
 * Model Menu Kedai Kopi (Enkapsulasi & Polimorfisme).
 * Menampung informasi produk kopi, non-kopi, dan makanan beserta stoknya (FR-01, FR-03).
 */
public class Menu {
    private int id;
    private String kode;
    private String nama;
    private String kategori; // "Kopi", "Non-Kopi", "Makanan"
    private double harga;
    private int stok;

    public Menu(int id, String kode, String nama, String kategori, double harga, int stok) {
        this.id = id;
        this.kode = kode;
        this.nama = nama;
        this.kategori = kategori;
        this.harga = harga;
        this.stok = stok;
    }

    public String getJenisPenyajian() {
        if ("Kopi".equalsIgnoreCase(kategori)) {
            return "Ekstraksi espresso murni biji kopi arabika pilihan.";
        } else if ("Non-Kopi".equalsIgnoreCase(kategori)) {
            return "Minuman teh, cokelat, atau susu dingin/hangat segar.";
        } else {
            return "Makanan kudapan / pastry disajikan hangat dari oven.";
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) { this.harga = harga; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    @Override
    public String toString() {
        return nama + " (" + kode + ") - Rp " + (long) harga;
    }
}
