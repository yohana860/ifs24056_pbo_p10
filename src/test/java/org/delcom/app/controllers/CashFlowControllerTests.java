package org.delcom.app.controllers;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.CashFlow;
import org.delcom.app.entities.User;
import org.delcom.app.services.CashFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class CashFlowControllerTests {

    // Mock dependencies
    @Mock
    private CashFlowService cashFlowService;
    @Mock
    private AuthContext authContext;

    // Inject mocks into the controller instance
    @InjectMocks
    private CashFlowController cashFlowController;

    private final UUID USER_ID = UUID.fromString("1a2b3c4d-5e6f-7080-9a0b-1c2d3e4f5a6b");
    private final UUID FLOW_ID = UUID.fromString("f00dcafe-c0de-feed-dead-beef00000000");
    private User testUser;
    private CashFlow testFlow;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Inisialisasi User
        testUser = new User("Test User", "test@example.com", "hashedpassword");
        testUser.setId(USER_ID);

        // Inisialisasi CashFlow (Entity)
        testFlow = new CashFlow(null, null, null, 0, null);
        testFlow.setId(FLOW_ID);
        testFlow.setUserId(USER_ID);
        testFlow.setType("INCOME");
        testFlow.setAmount(100000.0); // Gunakan Double untuk RequestBody
        testFlow.setDescription("Gaji Bulanan");

        // Setup default mock behavior: User is authenticated
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(testUser);
    }

    // =========================================================================
    //                            A. GET TESTS
    // =========================================================================

    @Test
    @DisplayName("GET /api/cashflows: Sukses, Mengambil semua aliran dana pengguna (200 OK)")
    void getFlows_success() {
        // Setup: Service mengembalikan daftar aliran dana. Controller memanggil dengan keyword=null.
        List<CashFlow> flows = Arrays.asList(testFlow, new CashFlow(null, null, null, 0, null));
        when(cashFlowService.getAllCashFlows(eq(USER_ID), eq(null))).thenReturn(flows);

        ResponseEntity<?> response = cashFlowController.getFlows();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(flows, response.getBody());
        // Verifikasi service dipanggil dengan keyword null
        verify(cashFlowService).getAllCashFlows(eq(USER_ID), eq(null));
    }

    @Test
    @DisplayName("GET /api/cashflows: Gagal - Tidak Terautentikasi (401 Unauthorized)")
    void getFlows_unauthorized() {
        // Setup: User tidak terautentikasi
        when(authContext.isAuthenticated()).thenReturn(false);

        ResponseEntity<?> response = cashFlowController.getFlows();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Unauthorized"));
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).getAllCashFlows(any(), any());
    }

    // =========================================================================
    //                            B. CREATE (POST) TESTS
    // =========================================================================

    @Test
    @DisplayName("POST /api/cashflows: Sukses, Membuat catatan baru (201 Created)")
    void createFlow_success() {
        // Data yang dikirim oleh client
        CashFlow newFlowData = new CashFlow(null, null, null, 0, null);
        newFlowData.setType("EXPENSE");
        newFlowData.setAmount(50000.0);
        newFlowData.setDescription("Beli Kopi");
        
        // Setup: Service mengembalikan CashFlow yang sudah disimpan
        // Amount di controller dikonversi ke Integer: 50000.0 -> 50000
        when(cashFlowService.createCashFlow(
            eq(USER_ID), 
            eq("EXPENSE"), 
            any(), // source
            any(), // label
            eq(50000), // amount (HARUS INTEGER di service)
            eq("Beli Kopi")
        )).thenReturn(testFlow);

        ResponseEntity<?> response = cashFlowController.createFlow(newFlowData);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testFlow, response.getBody());
        
        // Verifikasi bahwa service dipanggil dengan parameter yang benar, termasuk amount INTEGER
        verify(cashFlowService).createCashFlow(
            eq(USER_ID), 
            eq("EXPENSE"), 
            any(), 
            any(), 
            eq(50000), 
            eq("Beli Kopi")
        );
    }
    
    @Test
    @DisplayName("POST /api/cashflows: Gagal - Tidak Terautentikasi (401 Unauthorized)")
    void createFlow_unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = cashFlowController.createFlow(testFlow);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).createCashFlow(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/cashflows: Gagal - Data Tidak Valid (Amount <= 0) (400 Bad Request)")
    void createFlow_invalidAmount() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType("INCOME");
        invalidFlow.setAmount(0.0); // Invalid amount: amount <= 0
        invalidFlow.setDescription("Gaji");

        ResponseEntity<?> response = cashFlowController.createFlow(invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).createCashFlow(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/cashflows: Gagal - Data Tidak Valid (Type null) (400 Bad Request)")
    void createFlow_invalidType() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType(null); // Invalid type: null
        invalidFlow.setAmount(100.0);
        invalidFlow.setDescription("Gaji");

        ResponseEntity<?> response = cashFlowController.createFlow(invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).createCashFlow(any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("POST /api/cashflows: Gagal - Data Tidak Valid (Description null) (400 Bad Request)")
    void createFlow_invalidDescriptionNull() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType("INCOME");
        invalidFlow.setAmount(100.0);
        invalidFlow.setDescription(null); // Invalid description: null

        ResponseEntity<?> response = cashFlowController.createFlow(invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).createCashFlow(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/cashflows: Gagal - Data Tidak Valid (Description kosong) (400 Bad Request)")
    void createFlow_invalidDescriptionEmpty() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType("INCOME");
        invalidFlow.setAmount(100.0);
        invalidFlow.setDescription(""); // Invalid description: empty string

        ResponseEntity<?> response = cashFlowController.createFlow(invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).createCashFlow(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/cashflows: Gagal - Service gagal menyimpan (500 Internal Server Error)")
    void createFlow_serviceFailure() {
        CashFlow newFlowData = new CashFlow(null, null, null, 0, null);
        newFlowData.setType("INCOME");
        newFlowData.setAmount(100.0);
        newFlowData.setDescription("Dana");

        // Setup mock agar service mengembalikan null
        when(cashFlowService.createCashFlow(any(), any(), any(), any(), any(), any())).thenReturn(null);

        ResponseEntity<?> response = cashFlowController.createFlow(newFlowData);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // =========================================================================
    //                            C. UPDATE (PUT) TESTS
    // =========================================================================

    @Test
    @DisplayName("PUT /api/cashflows/{id}: Sukses, Mengupdate catatan (200 OK)")
    void updateFlow_success() {
        // Data update
        CashFlow updateData = new CashFlow(null, null, null, 0, null);
        updateData.setType("EXPENSE");
        updateData.setAmount(50000.0);
        updateData.setDescription("Beli Jajanan");
        
        // Setup: Service mengembalikan CashFlow yang telah diupdate
        // Amount di controller dikonversi ke Integer: 50000.0 -> 50000
        when(cashFlowService.updateCashFlow(
            eq(FLOW_ID), 
            eq(USER_ID), 
            eq("EXPENSE"), 
            any(), // source
            any(), // label
            eq(50000), // amount (HARUS INTEGER di service)
            eq("Beli Jajanan")
        )).thenReturn(updateData);

        ResponseEntity<?> response = cashFlowController.updateFlow(FLOW_ID, updateData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updateData, response.getBody());
        
        // Verifikasi service dipanggil dengan ID, User ID, dan amount INTEGER yang benar
        verify(cashFlowService).updateCashFlow(
            eq(FLOW_ID), 
            eq(USER_ID), 
            eq("EXPENSE"), 
            any(), 
            any(), 
            eq(50000), 
            eq("Beli Jajanan")
        );
    }

    @Test
    @DisplayName("PUT /api/cashflows/{id}: Gagal - Tidak Terautentikasi (401 Unauthorized)")
    void updateFlow_unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = cashFlowController.updateFlow(FLOW_ID, testFlow);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).updateCashFlow(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/cashflows/{id}: Gagal - Data Tidak Valid (Amount <= 0) (400 Bad Request)")
    void updateFlow_invalidAmount() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType("EXPENSE");
        invalidFlow.setAmount(0.0); // Invalid amount: amount <= 0
        invalidFlow.setDescription("Belanja");

        ResponseEntity<?> response = cashFlowController.updateFlow(FLOW_ID, invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).updateCashFlow(any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("PUT /api/cashflows/{id}: Gagal - Data Tidak Valid (Type null) (400 Bad Request)")
    void updateFlow_invalidType() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType(null); // Invalid type: null
        invalidFlow.setAmount(100.0);
        invalidFlow.setDescription("Belanja");

        ResponseEntity<?> response = cashFlowController.updateFlow(FLOW_ID, invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).updateCashFlow(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/cashflows/{id}: Gagal - Data Tidak Valid (Description null) (400 Bad Request)")
    void updateFlow_invalidDescriptionNull() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType("EXPENSE");
        invalidFlow.setAmount(100.0);
        invalidFlow.setDescription(null); // Invalid description: null

        ResponseEntity<?> response = cashFlowController.updateFlow(FLOW_ID, invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).updateCashFlow(any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("PUT /api/cashflows/{id}: Gagal - Data Tidak Valid (Description kosong) (400 Bad Request)")
    void updateFlow_invalidDescriptionEmpty() {
        CashFlow invalidFlow = new CashFlow(null, null, null, 0, null);
        invalidFlow.setType("EXPENSE");
        invalidFlow.setAmount(100.0);
        invalidFlow.setDescription(""); // Invalid description: empty string

        ResponseEntity<?> response = cashFlowController.updateFlow(FLOW_ID, invalidFlow);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).updateCashFlow(any(), any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("PUT /api/cashflows/{id}: Gagal - Catatan tidak ditemukan atau akses ditolak (404 Not Found)")
    void updateFlow_notFoundOrAccessDenied() {
        // Data update valid
        CashFlow updateData = new CashFlow(null, null, null, 0, null);
        updateData.setType("EXPENSE");
        updateData.setAmount(100.0);
        updateData.setDescription("Update");

        // Setup: Service mengembalikan null (tidak ditemukan/akses ditolak)
        when(cashFlowService.updateCashFlow(any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        ResponseEntity<?> response = cashFlowController.updateFlow(FLOW_ID, updateData);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("not found or access denied"));
    }

    // =========================================================================
    //                            D. DELETE TESTS
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/cashflows/{id}: Sukses, Menghapus catatan (204 No Content)")
    void deleteFlow_success() {
        // Setup: Service mengembalikan true (berhasil dihapus)
        when(cashFlowService.deleteCashFlow(FLOW_ID, USER_ID)).thenReturn(true);

        ResponseEntity<?> response = cashFlowController.deleteFlow(FLOW_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        // Verifikasi service dipanggil dengan ID dan User ID yang benar
        verify(cashFlowService).deleteCashFlow(FLOW_ID, USER_ID);
    }

    @Test
    @DisplayName("DELETE /api/cashflows/{id}: Gagal - Tidak Terautentikasi (401 Unauthorized)")
    void deleteFlow_unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = cashFlowController.deleteFlow(FLOW_ID);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        // Verifikasi service tidak dipanggil
        verify(cashFlowService, never()).deleteCashFlow(any(), any());
    }
    
    @Test
    @DisplayName("DELETE /api/cashflows/{id}: Gagal - Catatan tidak ditemukan atau akses ditolak (404 Not Found)")
    void deleteFlow_notFoundOrAccessDenied() {
        // Setup: Service mengembalikan false (gagal dihapus/tidak ditemukan)
        when(cashFlowService.deleteCashFlow(FLOW_ID, USER_ID)).thenReturn(false);

        ResponseEntity<?> response = cashFlowController.deleteFlow(FLOW_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("not found or access denied"));
    }
}