# Aplikasi Mesin Kasir Kedai Kopi (Pacbuck POS) Berbasis JavaFX & OOP Java

**Mata Kuliah:** Pemrograman Berbasis Objek (PBO)  
**Program Studi:** Informatika  
**Universitas Jenderal Soedirman (UNSOED) - 2026**  
**Kelompok 4:**
1. Muhammad Rifqi Agus S (H1D025113)
2. Felix Ogen Gelael (H1D025123)
3. Al Triaza Muhammad Ramadhan (H1D025138)
4. Muhammad Firdzan Sanjaya (H1D025140)
5. Adha Japari Munggaran (H1D025143)

---

## ☕ Deskripsi Sistem
Aplikasi desktop sistem kasir (Point of Sale) modern berbasis **JavaFX 21** dengan arsitektur **OOP (Object-Oriented Programming)** berlapis (*Model, View, Controller/Service, Repository, Utility*) dan terhubung ke database **MySQL**.

Antarmuka telah ditingkatkan ke **JavaFX** dengan desain modern bertema **Deep Emerald & Slate Fintech**:
- **High-DPI Ultra Sharp Display:** Bebas dari tampilan buram/ketinggalan zaman warisan Swing.
- **JavaFX CSS Engine:** Desain antarmuka modern dengan rounded cards, subtle shadows, dan transisi hover responsif.
- **Visualisasi Grafik Interaktif (JavaFX BarChart):** Grafik batang analitik 5 menu terlaris secara visual pada tab Laporan Penjualan.
- **Katalog Menu & Live Stock Status:** Badge stok dinamis (Aman, Menipis, Habis) dan pemotongan stok otomatis.
- **Pencetakan Struk Otomatis (PDF & TXT):** Integrasi cetak struk resmi dan pembukaan dokumen PDF langsung di sistem.

---

## 📂 Struktur File Projek

```
kasir-kopi/
├── database/
│   └── database.sql                  # Skrip pembuatan database, tabel & data default MySQL
├── lib/
│   ├── mysql-connector-j-8.3.0.jar   # Driver MySQL JDBC
│   ├── itextpdf-5.5.13.3.jar         # Library cetak struk PDF resmi
│   └── javafx-sdk/                   # OpenJFX 21 SDK (Windows x64 Jars & DLLs)
├── receipts/                         # Folder otomatis penyimpanan struk PDF dan TXT
├── bin/                              # Folder output kompilasi bytecode (.class)
├── src/
│   ├── model/                        # Lapisan Model OOP
│   │   ├── Pengguna.java             # [Abstraksi & Enkapsulasi] Kelas dasar pengguna
│   │   ├── Admin.java                # [Pewarisan / Inheritance] Turunan Pengguna untuk Admin
│   │   ├── Kasir.java                # [Pewarisan / Inheritance] Turunan Pengguna untuk Kasir
│   │   ├── Menu.java                 # [Polimorfisme] Menu & metode penyajian kopi/non-kopi/makanan
│   │   ├── Promo.java                # Model Diskon & Validasi Kode Promo (FR-07, FR-08)
│   │   ├── Transaksi.java            # Model Transaksi Penjualan (FR-05)
│   │   └── DetailTransaksi.java      # Model Relasi Komposisi Item Penjualan
│   ├── repository/                   # Lapisan Akses Database (CRUD SQL)
│   │   ├── MenuRepository.java
│   │   ├── PromoRepository.java
│   │   └── TransaksiRepository.java
│   ├── service/                      # Lapisan Logika Bisnis
│   │   ├── AuthService.java          # Otentikasi & Pengelolaan Akun Kasir (FR-02)
│   │   ├── MenuService.java          # Pengelolaan Menu & Stok Real-Time (FR-01, FR-03)
│   │   ├── PromoService.java         # Validasi Kode Promo & Diskon (FR-07, FR-08)
│   │   ├── TransaksiService.java     # Pemrosesan Transaksi Kasir & Cetak Struk (FR-05, FR-06)
│   │   └── LaporanService.java       # Analitik Omset Harian/Mingguan/Bulanan (FR-04)
│   ├── ui/                           # Lapisan GUI Modern JavaFX
│   │   ├── AppLauncher.java          # Titik Masuk Utama (Main Entry Point)
│   │   ├── MainApp.java              # JavaFX Application & Scene Stage Manager
│   │   ├── LoginView.java            # Layar Login Card Modern & Pengecekan DB
│   │   ├── AdminDashboardView.java   # Portal Eksekutif Administrator (6 Tab Fitur)
│   │   ├── KasirDashboardView.java   # Terminal Kasir Cepat (POS & Riwayat)
│   │   ├── UITheme.java              # Helper Tema, Dialog Modal, dan Format Rupiah
│   │   └── styles.css                # Stylesheet Modern CSS JavaFX
│   └── util/
│       ├── DatabaseUtil.java         # Koneksi JDBC & Inisialisasi Otomatis Database
│       └── PDFUtil.java              # Generator Dokumen Struk PDF (iText) & TXT
├── compile.bat                       # Sekali klik untuk kompilasi JavaFX
├── run.bat                           # Sekali klik untuk menjalankan aplikasi JavaFX
└── README.md                         # Dokumentasi Proyek
```

---

## 📌 Kesesuaian Requirement Dokumen (FR-01 s/d FR-08)

| Kode FR | Requirement Dokumen | Implementasi pada Program JavaFX |
| :--- | :--- | :--- |
| **FR-01** | Admin mengelola daftar menu kopi, non-kopi, dan makanan beserta harganya | Tab **"Kelola Menu & Stok"**: Tambah Menu, Edit Harga/Kategori, Hapus, Filter Kategori Interaktif |
| **FR-02** | Admin mengelola dan menonaktifkan akun kasir | Tab **"Kelola Akun Kasir"**: Tambah Kasir Baru, Ubah Status (Aktif/Nonaktif), Reset Password |
| **FR-03** | Mencatat, memperbarui, dan menampilkan stok kopi secara real-time | Tombol **"Update Stok"**, badge warna stok (Aman / Menipis / Habis), dan pemotongan stok otomatis saat transaksi |
| **FR-04** | Laporan ringkasan penjualan (harian, mingguan, bulanan), pendapatan & menu terlaris | Tab **"Laporan Penjualan"**: Filter Harian/Mingguan/Bulanan, Kartu KPI Omset & Volume, Grafik Batang (**JavaFX BarChart**) & Tabel Peringkat Best Sellers |
| **FR-05** | Kasir memilih menu, hitung subtotal, pajak (10%), total tagihan & proses bayar | Tab **"Transaksi Kasir (POS)"**: Tombol `+1` / `-1` Qty, Template Cepat Uang Tunai (`Pas`, `20k`, `50k`, `100k`, `200k`), Perhitungan Kembalian Real-Time, Konfirmasi Bayar |
| **FR-06** | Menyimpan struk pembayaran dalam format PDF dan TXT secara lokal | File PDF & TXT otomatis disimpan di folder `receipts/` dan langsung dibuka secara otomatis di penampil PDF sistem |
| **FR-07** | Admin membuat, melihat, mengubah, dan menghapus kode promo & masa berlaku | Tab **"Kode Promo & Diskon"**: Form tambah promo (Persen / Nominal), batas masa berlaku, minimum belanja |
| **FR-08** | Kasir memasukkan promo, validasi otomatis tanggal & min belanja, potongan harga otomatis | Tombol **"Terapkan"** pada POS: validasi kode, cek tanggal & minimum belanja, serta otomatis memotong total belanja |

---

## 🏛️ Penerapan Konsep OOP (Pemrograman Berbasis Objek)

1. **Abstraksi (Abstraction):**
   - Kelas abstrak `Pengguna` mendefinisikan method abstrak `getDeskripsiAkses()`.
2. **Enkapsulasi (Encapsulation):**
   - Atribut seluruh model bersifat `private` dan diakses melalui getter/setter yang tervalidasi.
3. **Pewarisan (Inheritance):**
   - `Admin` dan `Kasir` mewarisi kelas abstrak `Pengguna`.
4. **Polimorfisme (Polymorphism):**
   - Method `getDeskripsiAkses()` menghasilkan output spesifik pada masing-masing peran pengguna (`Admin` vs `Kasir`).
   - Method `getJenisPenyajian()` pada kelas `Menu` memberikan deskripsi penyajian yang dinamis berdasarkan jenis kategori produk.
   - Perhitungan diskon fleksibel (persen vs nominal potongan langsung) pada `Promo.hitungPotongan()`.

---

## 🚀 Cara Menjalankan Proyek

1. **Pastikan MySQL aktif** (klik *Start* pada MySQL di Laragon / XAMPP).
2. **Kompilasi program:** Dobel klik `compile.bat`.
3. **Jalankan aplikasi:** Dobel klik `run.bat`.

> **Catatan Database:** Database `db_kasir_kopi` dan tabel-tabelnya akan **otomatis dibuat dan diisi data awal** saat aplikasi pertama kali dijalankan. Anda juga dapat mengimpor `database/database.sql` secara manual melalui phpMyAdmin jika diinginkan.

---

## 🔑 Akun Bawaan (Default):
- **Admin:** `admin` / `admin123`
- **Kasir:** `kasir` / `kasir123`

## 🏷️ Kode Promo Bawaan:
- `NGOPIHEMAT` : Diskon 10% (Min. Belanja Rp 30.000)
- `DISKON5RB` : Potongan Rp 5.000 (Min. Belanja Rp 25.000)
