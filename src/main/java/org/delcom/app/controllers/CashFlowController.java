package org.delcom.app.controllers;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.CashFlow;
import org.delcom.app.entities.User;
import org.delcom.app.services.CashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Kontroler REST untuk mengelola operasi Cash Flow (Pemasukan/Pengeluaran).
 * Semua operasi dicakup berdasarkan pengguna yang sedang terautentikasi.
 */
@RestController
@RequestMapping("/api/cashflows")
public class CashFlowController {

    private final CashFlowService cashFlowService;
    private final AuthContext authContext;

    @Autowired
    public CashFlowController(CashFlowService cashFlowService, AuthContext authContext) {
        this.cashFlowService = cashFlowService;
        this.authContext = authContext;
    }

    /**
     * Mengambil semua catatan Cash Flow (Pemasukan/Pengeluaran) untuk pengguna yang sedang login.
     * @return ResponseEntity dengan list CashFlow atau 401 Unauthorized jika tidak terautentikasi.
     */
    @GetMapping
    public ResponseEntity<?> getFlows() {
        if (!authContext.isAuthenticated()) {
            return new ResponseEntity<>("Unauthorized. User must be logged in.", HttpStatus.UNAUTHORIZED);
        }

        User user = authContext.getAuthUser();
        // Memanggil service untuk mengambil semua cash flow berdasarkan ID pengguna
        // PERBAIKAN: Menggunakan getAllCashFlows(userId, keyword)
        List<CashFlow> flows = cashFlowService.getAllCashFlows(user.getId(), null); // keyword null untuk ambil semua
        return new ResponseEntity<>(flows, HttpStatus.OK);
    }

    /**
     * Membuat catatan Cash Flow baru.
     * @param flow Data CashFlow baru (tanpa ID/User ID).
     * @return ResponseEntity dengan CashFlow yang telah disimpan atau error.
     */
    @PostMapping
    public ResponseEntity<?> createFlow(@RequestBody CashFlow flow) {
        if (!authContext.isAuthenticated()) {
            return new ResponseEntity<>("Unauthorized. User must be logged in.", HttpStatus.UNAUTHORIZED);
        }

        // Validasi dasar
        // PERHATIAN: Asumsi CashFlow di Entity dan Service menggunakan Integer untuk amount
        // Jika di DTO dari RequestBody menggunakan Double, perlu konversi.
        if (flow.getType() == null || flow.getAmount() <= 0 || flow.getDescription() == null || flow.getDescription().isEmpty()) {
             return new ResponseEntity<>("Invalid cash flow data. Type, amount, and description are required.", HttpStatus.BAD_REQUEST);
        }

        User user = authContext.getAuthUser();
        
        // PERBAIKAN: Menggunakan createCashFlow() dengan parameter eksplisit dari Service
        // Pastikan tipe data 'amount' sesuai antara CashFlow (dari @RequestBody) dan service (Integer)
        CashFlow createdFlow = cashFlowService.createCashFlow(
            user.getId(),
            flow.getType(),
            flow.getSource(),
            flow.getLabel(),
            flow.getAmount().intValue(), // Mengonversi Double ke Integer, asumsikan jumlah tidak ada desimal
            flow.getDescription()
        );

        if (createdFlow == null) {
            return new ResponseEntity<>("Failed to create cash flow record.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(createdFlow, HttpStatus.CREATED);
    }

    /**
     * Mengubah catatan Cash Flow yang sudah ada.
     * @param id ID dari catatan Cash Flow yang akan diubah.
     * @param updatedFlow Data CashFlow yang diupdate.
     * @return ResponseEntity dengan CashFlow yang diupdate atau error.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlow(@PathVariable UUID id, @RequestBody CashFlow updatedFlow) {
        if (!authContext.isAuthenticated()) {
            return new ResponseEntity<>("Unauthorized. User must be logged in.", HttpStatus.UNAUTHORIZED);
        }

        // Validasi dasar
        // PERHATIAN: Asumsi CashFlow di Entity dan Service menggunakan Integer untuk amount
        if (updatedFlow.getType() == null || updatedFlow.getAmount() <= 0 || updatedFlow.getDescription() == null || updatedFlow.getDescription().isEmpty()) {
             return new ResponseEntity<>("Invalid cash flow data. Type, amount, and description are required.", HttpStatus.BAD_REQUEST);
        }

        User user = authContext.getAuthUser();

        // Panggil service untuk mengupdate, pastikan ID pengguna cocok untuk keamanan
        // PERBAIKAN: Menggunakan updateCashFlow() dengan parameter eksplisit dari Service
        CashFlow result = cashFlowService.updateCashFlow(
            id,
            user.getId(),
            updatedFlow.getType(),
            updatedFlow.getSource(),
            updatedFlow.getLabel(),
            updatedFlow.getAmount().intValue(), // Mengonversi Double ke Integer, asumsikan jumlah tidak ada desimal
            updatedFlow.getDescription()
        );

        if (result == null) {
            // Asumsi service mengembalikan null jika tidak ditemukan atau user ID tidak cocok
            return new ResponseEntity<>("Cash Flow record not found or access denied.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Menghapus catatan Cash Flow.
     * @param id ID dari catatan Cash Flow yang akan dihapus.
     * @return ResponseEntity 204 No Content atau error.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFlow(@PathVariable UUID id) {
        if (!authContext.isAuthenticated()) {
            return new ResponseEntity<>("Unauthorized. User must be logged in.", HttpStatus.UNAUTHORIZED);
        }

        User user = authContext.getAuthUser();

        // Panggil service untuk menghapus, pastikan ID pengguna cocok untuk keamanan
        boolean deleted = cashFlowService.deleteCashFlow(id, user.getId());

        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content
        } else {
            return new ResponseEntity<>("Cash Flow record not found or access denied.", HttpStatus.NOT_FOUND);
        }
    }
}