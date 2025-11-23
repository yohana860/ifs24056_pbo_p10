package org.delcom.app.services;

import org.delcom.app.entities.CashFlow;
import org.delcom.app.repositories.CashFlowRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Service
public class CashFlowService {

    private final CashFlowRepository cashFlowRepository;

    // Constructor Injection
    public CashFlowService(CashFlowRepository cashFlowRepository) {
        this.cashFlowRepository = cashFlowRepository;
    }

    // 1. CREATE
    /**
     * Membuat CashFlow baru untuk user tertentu.
     * @param userId ID pengguna yang membuat transaksi.
     * @return CashFlow yang baru disimpan.
     */
    public CashFlow createCashFlow(UUID userId, String type, String source, String label, Integer amount, String description) {
        // Gunakan konstruktor baru yang menerima userId
        CashFlow newFlow = new CashFlow(userId, type, source, label, amount, description);
        
        // Simpan objek baru ke database
        return cashFlowRepository.save(newFlow);
    }

    // 2. READ ALL / SEARCH
    /**
     * Mengambil semua CashFlow milik user, opsional difilter berdasarkan keyword.
     * @param userId ID pengguna.
     * @param keyword Kata kunci untuk pencarian (dapat berupa null).
     * @return Daftar CashFlow.
     */
    public List<CashFlow> getAllCashFlows(UUID userId, String keyword) {
        // Jika keyword valid, gunakan pencarian dengan filter userId
        if (keyword != null && !keyword.trim().isEmpty()) {
            return cashFlowRepository.findByKeyword(userId, keyword.trim()); 
        }
        
        // Jika keyword kosong, ambil semua CashFlow yang dimiliki oleh user tersebut.
        // Asumsi: findByKeyword dapat menangani string kosong, atau Anda harus menambahkan
        // method 'findByUserId(UUID userId)' di CashFlowRepository.
        return cashFlowRepository.findByKeyword(userId, "");
    }

    // 3. READ BY ID
    /**
     * Mengambil CashFlow berdasarkan ID, memastikan transaksi milik user.
     * @param id ID transaksi CashFlow.
     * @param userId ID pengguna untuk otorisasi.
     * @return CashFlow jika ditemukan dan dimiliki user, atau null.
     */
    public CashFlow getCashFlowById(UUID id, UUID userId) {
        // Menggunakan method yang memfilter berdasarkan userId DAN id transaksi
        return cashFlowRepository.findByUserIdAndId(userId, id); 
    }

    // 4. GET LABELS
    /**
     * Mengambil daftar label unik yang digunakan oleh user.
     * @param userId ID pengguna.
     * @return Daftar String label unik.
     */
    public List<String> getCashFlowLabels(UUID userId) {
        // Menggunakan method untuk menemukan label unik milik user
        return cashFlowRepository.findDistinctLabels(userId); 
    }

    // 5. UPDATE
    /**
     * Memperbarui CashFlow yang ada, memastikan transaksi milik user.
     * @param id ID transaksi CashFlow yang akan diperbarui.
     * @param userId ID pengguna untuk otorisasi.
     * @return CashFlow yang telah diperbarui, atau null jika tidak ditemukan/bukan milik user.
     */
    public CashFlow updateCashFlow(UUID id, UUID userId, String type, String source, String label, Integer amount, String description) {
        // 1. Cari data lama dan verifikasi kepemilikan
        CashFlow existingFlow = cashFlowRepository.findByUserIdAndId(userId, id);

        if (existingFlow != null) {
            // 2. Perbarui field-field
            existingFlow.setType(type);
            existingFlow.setSource(source);
            existingFlow.setLabel(label);
            existingFlow.setAmount(amount);
            existingFlow.setDescription(description);
            
            // 3. Simpan perubahan (onUpdate() ditangani oleh @PreUpdate di Entity)
            return cashFlowRepository.save(existingFlow);
        }
        return null;
    }

    // 6. DELETE
    /**
     * Menghapus CashFlow berdasarkan ID, memastikan transaksi milik user.
     * @param id ID transaksi CashFlow yang akan dihapus.
     * @param userId ID pengguna untuk otorisasi.
     * @return true jika berhasil dihapus, false jika transaksi tidak ditemukan/bukan milik user.
     */
    public boolean deleteCashFlow(UUID id, UUID userId) {
        // Cek apakah data ada dan milik user
        if (cashFlowRepository.existsByUserIdAndId(userId, id)) {
            cashFlowRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // --- METODE STUB DIHAPUS ---
    // Metode 'createFlow', 'updateFlow', dan 'getFlowsByUserId' telah dihapus
    // karena sudah diwakili oleh implementasi di atas.
}