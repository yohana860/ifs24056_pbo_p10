package org.delcom.app.entities;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CashFlowTests {
    @Test
    @DisplayName("Membuat instance dari kelas CashFlow")
    void testMembuatInstanceCashFlow() throws Exception {
        UUID USER_ID = UUID.randomUUID(); // 👈 Definisikan ID pengguna untuk testing

        // CashFlow telah selesai
        {
            // 👈 Konstruktor sekarang memerlukan userId (UUID)
            // Constructor: (UUID userId, String type, String source, String label, Integer amount, String description)
            CashFlow cashFlow = new CashFlow(USER_ID, "Inflow", "Gaji", "gaji-bulanan", 400000,
                    "Menerima gaji bulanan dari perusahaan.");

            // 👈 Uji field userId
            assert (cashFlow.getUserId().equals(USER_ID)); 
            
            assert (cashFlow.getType().equals("Inflow"));
            assert (cashFlow.getSource().equals("Gaji"));
            assert (cashFlow.getLabel().equals("gaji-bulanan"));
            assert (cashFlow.getAmount().equals(400000));
            assert (cashFlow.getDescription().equals("Menerima gaji bulanan dari perusahaan."));
        }

        // CashFlow dengan nilai default (No-Args Constructor)
        {
            CashFlow cashFlow = new CashFlow("Pemasukan", "Gaji", "Deskripsi updated", 5000, "2023-01-01");

            assert (cashFlow.getId() == null);
            assert (cashFlow.getUserId() == null); // 👈 Uji field userId
            assert (cashFlow.getType() == null);
            assert (cashFlow.getSource() == null);
            assert (cashFlow.getLabel() == null);
            assert (cashFlow.getAmount() == null);
            assert (cashFlow.getDescription() == null);
            assert (cashFlow.getCreatedAt() == null);
            assert (cashFlow.getUpdatedAt() == null);
        }

        // CashFlow dengan setNilai
        {
            CashFlow cashFlow = new CashFlow("Pemasukan", "Gaji", "Deskripsi updated", 5000, "2023-01-01");
            UUID generatedId = UUID.randomUUID();
            UUID anotherUserId = UUID.randomUUID(); // ID pengguna lain

            cashFlow.setId(generatedId);
            cashFlow.setUserId(anotherUserId); // 👈 Set userId
            cashFlow.setType("Set Type");
            cashFlow.setSource("Set Source");
            cashFlow.setLabel("Set Label");
            cashFlow.setAmount(500000);
            cashFlow.setDescription("Set Description");
            cashFlow.onCreate();
            cashFlow.onUpdate();

            assert (cashFlow.getId().equals(generatedId));
            assert (cashFlow.getUserId().equals(anotherUserId)); // 👈 Uji nilai yang diset
            assert (cashFlow.getType().equals("Set Type"));
            assert (cashFlow.getSource().equals("Set Source"));
            assert (cashFlow.getLabel().equals("Set Label"));
            assert (cashFlow.getAmount().equals(500000));
            assert (cashFlow.getDescription().equals("Set Description"));
            assert (cashFlow.getCreatedAt() != null);
            assert (cashFlow.getUpdatedAt() != null);
        }
    }
}