package model;

import java.time.LocalDate;

/**
 * Model Kode Promo dan Ketentuan Diskon (FR-07, FR-08).
 */
public class Promo {
    private int id;
    private String kode;
    private String namaPromo;
    private String tipeDiskon; // "PERSEN" atau "NOMINAL"
    private double nilaiDiskon;
    private double minBelanja;
    private LocalDate tglMulai;
    private LocalDate tglSelesai;
    private String status; // "AKTIF" atau "NONAKTIF"

    public Promo(int id, String kode, String namaPromo, String tipeDiskon, double nilaiDiskon,
                 double minBelanja, LocalDate tglMulai, LocalDate tglSelesai, String status) {
        this.id = id;
        this.kode = kode;
        this.namaPromo = namaPromo;
        this.tipeDiskon = tipeDiskon;
        this.nilaiDiskon = nilaiDiskon;
        this.minBelanja = minBelanja;
        this.tglMulai = tglMulai;
        this.tglSelesai = tglSelesai;
        this.status = status;
    }

    public boolean isValid() {
        if (!"AKTIF".equalsIgnoreCase(status)) return false;
        LocalDate today = LocalDate.now();
        if (tglMulai != null && today.isBefore(tglMulai)) return false;
        if (tglSelesai != null && today.isAfter(tglSelesai)) return false;
        return true;
    }

    public double hitungPotongan(double subtotal) {
        if (!isValid() || subtotal < minBelanja) {
            return 0.0;
        }
        if ("PERSEN".equalsIgnoreCase(tipeDiskon)) {
            return subtotal * (nilaiDiskon / 100.0);
        } else {
            return Math.min(nilaiDiskon, subtotal);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }

    public String getNamaPromo() { return namaPromo; }
    public void setNamaPromo(String namaPromo) { this.namaPromo = namaPromo; }

    public String getTipeDiskon() { return tipeDiskon; }
    public void setTipeDiskon(String tipeDiskon) { this.tipeDiskon = tipeDiskon; }

    public double getNilaiDiskon() { return nilaiDiskon; }
    public void setNilaiDiskon(double nilaiDiskon) { this.nilaiDiskon = nilaiDiskon; }

    public double getMinBelanja() { return minBelanja; }
    public void setMinBelanja(double minBelanja) { this.minBelanja = minBelanja; }

    public LocalDate getTglMulai() { return tglMulai; }
    public void setTglMulai(LocalDate tglMulai) { this.tglMulai = tglMulai; }

    public LocalDate getTglSelesai() { return tglSelesai; }
    public void setTglSelesai(LocalDate tglSelesai) { this.tglSelesai = tglSelesai; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return kode + " - " + namaPromo;
    }
}
