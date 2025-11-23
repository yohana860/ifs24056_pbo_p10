package org.delcom.app.repositories; 

import org.delcom.app.entities.CashFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // 👈 Import untuk @Param
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CashFlowRepository extends JpaRepository<CashFlow, UUID> {
    
    // Wajib: findByKeyword untuk pencarian (sesuai Test TA)
    // 👈 Modifikasi Query: Tambahkan WHERE c.userId = :userId
    @Query("SELECT c FROM CashFlow c WHERE c.userId = :userId AND (" + 
            "LOWER(c.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.source) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.label) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<CashFlow> findByKeyword(@Param("userId") UUID userId, @Param("keyword") String keyword); // 👈 Tambahkan userId

    // Wajib: findDistinctLabels (sesuai Test TA)
    // 👈 Modifikasi Query: Tambahkan WHERE c.userId = :userId
    @Query("SELECT DISTINCT c.label FROM CashFlow c WHERE c.userId = :userId")
    List<String> findDistinctLabels(@Param("userId") UUID userId); // 👈 Tambahkan userId
    
    // Opsional: Untuk membantu operasi CRUD non-Query, buat method findByUserIdAndId
    // Spring Data JPA akan mengimplementasikannya secara otomatis
    CashFlow findByUserIdAndId(UUID userId, UUID id);
    
    // Opsional: Untuk membantu operasi Delete
    boolean existsByUserIdAndId(UUID userId, UUID id);
}