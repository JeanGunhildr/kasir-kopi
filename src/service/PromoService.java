package service;

import model.Promo;
import repository.PromoRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Layanan Logika Bisnis untuk Pengelolaan Promo & Validasi Diskon Otomatis (FR-07, FR-08).
 */
public class PromoService {
    private final PromoRepository promoRepository = new PromoRepository();

    public static class PromoValidationResult {
        public final boolean valid;
        public final String pesan;
        public final Promo promo;

        public PromoValidationResult(boolean valid, String pesan, Promo promo) {
            this.valid = valid;
            this.pesan = pesan;
            this.promo = promo;
        }
    }

    public PromoValidationResult validasiPromo(String kode, double subtotal) {
        if (kode == null || kode.trim().isEmpty()) {
            return new PromoValidationResult(false, "Kode promo tidak boleh kosong.", null);
        }
        try {
            Promo p = promoRepository.findByKode(kode.trim());
            if (p == null) {
                return new PromoValidationResult(false, "Kode promo '" + kode + "' tidak ditemukan atau nonaktif!", null);
            }
            if (!p.isValid()) {
                return new PromoValidationResult(false, "Masa berlaku promo '" + p.getNamaPromo() + "' sudah berakhir!", null);
            }
            if (subtotal < p.getMinBelanja()) {
                return new PromoValidationResult(false, "Minimal belanja untuk promo ini adalah Rp " + (long) p.getMinBelanja(), null);
            }
            return new PromoValidationResult(true, "Promo '" + p.getNamaPromo() + "' berhasil diterapkan!", p);
        } catch (SQLException e) {
            return new PromoValidationResult(false, "Kesalahan database: " + e.getMessage(), null);
        }
    }

    public List<Promo> getDaftarPromo() throws SQLException {
        return promoRepository.findAll();
    }

    public boolean simpanPromo(Promo p) throws SQLException {
        return promoRepository.save(p);
    }

    public boolean hapusPromo(int id) throws SQLException {
        return promoRepository.delete(id);
    }
}
