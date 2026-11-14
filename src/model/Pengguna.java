package model;

/**
 * Kelas abstrak Pengguna (Penerapan Abstraksi & Enkapsulasi OOP).
 * Mewakili entitas pengguna sistem kasir (Admin atau Kasir).
 */
public abstract class Pengguna {
    private int id;
    private String username;
    private String password;
    private String nama;
    private String role;   // "ADMIN" atau "KASIR"
    private String status; // "AKTIF" atau "NONAKTIF"

    public Pengguna(int id, String username, String password, String nama, String role, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.role = role;
        this.status = status;
    }

    // Polimorfisme: method abstrak yang diimplementasikan oleh Admin dan Kasir
    public abstract String getDeskripsiAkses();

    public boolean isAktif() {
        return "AKTIF".equalsIgnoreCase(this.status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return nama + " (" + role + ")";
    }
}
