# Aplikasi Mesin Kasir Kedai Kopi Berbasis OOP Java

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
Aplikasi desktop berbasis **Java Swing** dengan arsitektur **OOP (Object-Oriented Programming)** terpadu dan terhubung ke database **MySQL**. Seluruh fitur dibuat fokus dan tepat sasaran sesuai dengan Use Case Diagram dan Functional Requirements (FR-01 s/d FR-08) pada dokumen proyek.

---

## 📂 Struktur File Projek yang Simpel

Projek ini disederhanakan hanya menjadi **1 package (`com.kasirkopi`)** dengan file yang terfokus:

```
kasir-kopi/
├── database/
│   └── database.sql      # Skrip pembuatan database, tabel & data default MySQL
├── lib/
│   ├── mysql-connector-j-8.3.0.jar  # Driver MySQL JDBC
│   └── itextpdf-5.5.13.3.jar        # Library struk PDF
├── receipts/             # Folder otomatis penyimpanan struk PDF dan TXT
├── bin/                  # Hasil kompilasi (.class)
├── src/com/kasirkopi/
│   ├── User.java         # [OOP Abstraksi & Enkapsulasi] Kelas dasar pengguna
│   ├── Admin.java        # [OOP Pewarisan / Inheritance] Turunan User untuk Admin
│   ├── Kasir.java        # [OOP Pewarisan / Inheritance] Turunan User untuk Kasir
│   ├── Menu.java         # [OOP Polimorfisme] Menu, MenuKopi, MenuNonKopi, MenuMakanan
│   ├── Promo.java        # Model Diskon & Validasi Kode Promo (FR-07, FR-08)
│   ├── Transaksi.java    # Model Transaksi & Detail Pesanan (FR-05)
│   ├── Database.java     # Koneksi MySQL & Fungsi CRUD
│   ├── StrukUtil.java    # Pencetakan Struk PDF & TXT (FR-06)
│   └── KasirApp.java     # GUI Java Swing Terpadu (Login, Admin, Kasir POS)
├── compile.bat           # Sekali klik untuk kompilasi
├── run.bat               # Sekali klik untuk menjalankan aplikasi
└── README.md             # Dokumentasi
```

---

## 📌 Kesesuaian Requirement Dokumen (FR-01 s/d FR-08)

| Kode FR | Requirement Dokumen | Implementasi pada Program |
| :--- | :--- | :--- |
| **FR-01** | Admin mengelola daftar menu kopi, non-kopi, dan makanan beserta harganya | Tab **"Kelola Menu & Stok"**: Tambah, Edit, Hapus, Filter Kategori |
| **FR-02** | Admin mengelola dan menonaktifkan akun kasir | Tab **"Kelola Akun Kasir"**: Tambah, Aktifkan/Nonaktifkan akun kasir |
| **FR-03** | Mencatat, memperbarui, dan menampilkan stok kopi secara real-time | Tombol **"Tambah/Kurang Stok"** dan pemotongan stok otomatis saat transaksi |
| **FR-04** | Laporan ringkasan penjualan (harian, mingguan, bulanan), pendapatan & menu terlaris | Tab **"Laporan Penjualan"**: Filter Harian/Mingguan/Bulanan, Omset, Menu Paling Laku |
| **FR-05** | Kasir memilih menu, hitung subtotal, pajak (10%), total tagihan & proses bayar | Tab **"Transaksi Kasir (POS)"**: Tombol `+1` / `-1` Qty, Template Uang Bayar (`Uang Pas`, `50k`, `100k`, `200k`), Kembalian Real-Time, Bayar & Konfirmasi Langsung |
| **FR-06** | Menyimpan struk pembayaran dalam format PDF dan TXT secara lokal | File PDF & TXT otomatis disimpan di folder `receipts/` dan dapat langsung dibuka |
| **FR-07** | Admin membuat, melihat, mengubah, dan menghapus kode promo & masa berlaku | Tab **"Kelola Promo"**: Form tambah/hapus kode promo, batas tanggal, min belanja |
| **FR-08** | Kasir memasukkan promo, validasi otomatis tanggal & min belanja, potongan harga otomatis | Tombol **"Terapkan Promo"** pada kasir: validasi real-time dan potong total belanja |

---

## 🏛️ Penerapan Konsep OOP (Pemrograman Berbasis Objek)

1. **Abstraksi (Abstraction):**
   - Kelas abstrak `User` dan `Menu` mendefinisikan kerangka data dan method abstrak `getDeskripsiAkses()` dan `getJenisPenyajian()`.
2. **Enkapsulasi (Encapsulation):**
   - Semua atribut bersifat `private`, diakses melalui method `getter` dan `setter`.
3. **Pewarisan (Inheritance):**
   - `Admin` dan `Kasir` mewarisi kelas `User`.
   - `MenuKopi`, `MenuNonKopi`, dan `MenuMakanan` mewarisi kelas `Menu`.
4. **Polimorfisme (Polymorphism):**
   - Overriding method `getJenisPenyajian()` pada masing-masing jenis menu.
   - Perhitungan diskon fleksibel (persen vs nominal) pada `Promo.hitungPotongan()`.
   - Polimorfisme Factory Method `Menu.buatMenu()`.

---

## 🚀 Cara Menjalankan Proyek

1. **Pastikan MySQL aktif** (klik *Start* pada Laragon / XAMPP).
2. **Kompilasi program:** Dobel klik `compile.bat`.
3. **Jalankan aplikasi:** Dobel klik `run.bat`.

> **Catatan Database:** Database `db_kasir_kopi` dan tabel-tabelnya akan **otomatis dibuat** saat aplikasi pertama kali dijalankan. Anda juga bisa mengimpor `database/database.sql` secara manual melalui phpMyAdmin jika diinginkan.

---

## 🔑 Akun Bawaan (Default):
- **Admin:** `admin` / `admin123`
- **Kasir:** `kasir` / `kasir123`

## 🏷️ Kode Promo Bawaan:
- `NGOPIHEMAT` : Diskon 10% (Min. Belanja Rp 30.000)
- `DISKON5RB` : Potongan Rp 5.000 (Min. Belanja Rp 25.000)
