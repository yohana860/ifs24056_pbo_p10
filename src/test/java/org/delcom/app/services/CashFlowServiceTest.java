package org.delcom.app.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq; // 👈 Import eq
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.delcom.app.entities.CashFlow;
import org.delcom.app.repositories.CashFlowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CashFlowServiceTest {
    @Test
    @DisplayName("Pengujian untuk service CashFlow")
    void testCashFlowService() throws Exception {
        // Buat random UUID
        UUID cashFlowId = UUID.randomUUID();
        UUID nonexistentCashFlowId = UUID.randomUUID();
        UUID userId = UUID.randomUUID(); // 👈 ID Pengguna

        // Membuat dummy data (menggunakan konstruktor baru dengan userId)
        CashFlow cashFlow = new CashFlow(userId, "IN", "BANK", "Salary", 1000, "Monthly Salary");
        cashFlow.setId(cashFlowId);

        // Membuat mock CashFlowRepository
        CashFlowRepository cashFlowRepository = Mockito.mock(CashFlowRepository.class);

        // --- Atur perilaku mock (SEMUA method sekarang menerima userId) ---
        
        // 1. CREATE
        when(cashFlowRepository.save(any(CashFlow.class))).thenReturn(cashFlow);

        // 2. READ ALL / SEARCH
        // Untuk pencarian dengan keyword
        when(cashFlowRepository.findByKeyword(eq(userId), eq("BANK"))).thenReturn(java.util.List.of(cashFlow));
        // Untuk findAll (sekarang diimplementasikan sebagai findByKeyword dengan keyword kosong di Service)
        when(cashFlowRepository.findByKeyword(eq(userId), eq(""))).thenReturn(java.util.List.of(cashFlow));

        // 3. READ BY ID (Menggunakan findByUserIdAndId)
        when(cashFlowRepository.findByUserIdAndId(eq(userId), eq(cashFlowId))).thenReturn(cashFlow);
        when(cashFlowRepository.findByUserIdAndId(eq(userId), eq(nonexistentCashFlowId))).thenReturn(null);

        // 4. GET LABELS
        when(cashFlowRepository.findDistinctLabels(eq(userId))).thenReturn(java.util.List.of("Salary"));

        // 5. UPDATE
        // Untuk update berhasil
        when(cashFlowRepository.findByUserIdAndId(eq(userId), eq(cashFlowId))).thenReturn(cashFlow); 
        // Untuk update gagal
        when(cashFlowRepository.findByUserIdAndId(eq(userId), eq(nonexistentCashFlowId))).thenReturn(null);

        // 6. DELETE (Menggunakan existsByUserIdAndId)
        when(cashFlowRepository.existsByUserIdAndId(eq(userId), eq(cashFlowId))).thenReturn(true);
        when(cashFlowRepository.existsByUserIdAndId(eq(userId), eq(nonexistentCashFlowId))).thenReturn(false);
        doNothing().when(cashFlowRepository).deleteById(any(UUID.class));

        // Membuat instance service
        CashFlowService cashFlowService = new CashFlowService(cashFlowRepository);
        assert (cashFlowService != null);

        // ------------------------------------------------------------------------------------------

        // Menguji create cash flow
        {
            // 👈 Tambahkan userId
            CashFlow createdCashFlow = cashFlowService.createCashFlow(userId, "IN", "BANK", "Salary", 1000, "Monthly Salary");
            assert (createdCashFlow != null);
            assert (createdCashFlow.getId().equals(cashFlowId));
            assert (createdCashFlow.getUserId().equals(userId)); // 👈 Uji userId
            assert (createdCashFlow.getType().equals("IN"));
            // ... assertion lain tetap sama
        }

        // Menguji getAllCashFlows
        {
            // 👈 Tambahkan userId, keyword null
            var cashFlows = cashFlowService.getAllCashFlows(userId, null);
            assert (cashFlows.size() == 1);
        }

        // Menguji getAllCashFlows dengan pencarian
        {
            // 👈 Tambahkan userId, keyword "BANK"
            var cashFlows = cashFlowService.getAllCashFlows(userId, "BANK");
            assert (cashFlows.size() == 1);

            // 👈 Tambahkan userId, keyword kosong
            cashFlows = cashFlowService.getAllCashFlows(userId, "    ");
            assert (cashFlows.size() == 1);
        }

        // Menguji getCashFlowById
        {
            // 👈 Tambahkan userId
            CashFlow fetchedCashFlow = cashFlowService.getCashFlowById(cashFlowId, userId);
            assert (fetchedCashFlow != null);
            assert (fetchedCashFlow.getId().equals(cashFlowId));
            // ...
        }

        // Menguji getCashFlowById dengan ID yang tidak ada
        {
            // 👈 Tambahkan userId
            CashFlow fetchedCashFlow = cashFlowService.getCashFlowById(nonexistentCashFlowId, userId);
            assert (fetchedCashFlow == null);
        }

        // Menguji getCashFlowLabels
        {
            // 👈 Tambahkan userId
            var labels = cashFlowService.getCashFlowLabels(userId);
            assert (labels.size() == 1);
            assert (labels.get(0).equals("Salary"));
        }

        // Menguji updateCashFlow
        {
            String updatedType = "OUT";
            String updatedSource = "ATM";
            String updatedLabel = "Withdraw";
            Integer updatedAmount = 500;
            String updatedDescription = "Monthly Withdraw";

            // 👈 Tambahkan userId
            CashFlow updatedCashFlow = cashFlowService.updateCashFlow(cashFlowId, userId, updatedType, updatedSource,
                    updatedLabel, updatedAmount, updatedDescription);
            assert (updatedCashFlow != null);
            assert (updatedCashFlow.getUserId().equals(userId)); // 👈 Uji userId
            // ...
        }

        // Menguji update CashFlow dengan ID yang tidak ada
        {
            String updatedType = "OUT";
            String updatedSource = "ATM";
            // ...
            
            // 👈 Tambahkan userId
            CashFlow updatedCashFlow = cashFlowService.updateCashFlow(nonexistentCashFlowId, userId, updatedType, updatedSource,
                    "Withdraw", 500, "Monthly Withdraw");
            assert (updatedCashFlow == null);
        }

        // Menguji deleteCashFlow
        {
            // 👈 Tambahkan userId
            boolean deleted = cashFlowService.deleteCashFlow(cashFlowId, userId);
            assert (deleted == true);
        }

        // Menguji deleteCashFlow dengan ID yang tidak ada
        {
            // 👈 Tambahkan userId
            boolean deleted = cashFlowService.deleteCashFlow(nonexistentCashFlowId, userId);
            assert (deleted == false);
        }
    }
}