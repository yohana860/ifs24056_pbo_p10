package org.delcom.app.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_flows") 
public class CashFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id; 
    
    // 👈 FIELD BARU: User ID
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID userId; 
    
    @Column(name = "type", nullable = false)
    private String type; 
    
    @Column(name = "source", nullable = false)
    private String source; 
    
    @Column(name = "label", nullable = false)
    private String label; 
    
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "description", nullable = false)
    private String description; 
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    // 1. Constructor Wajib (No-Args)
    public CashFlow(String pemasukan, String gaji, String deskripsi_updated, int par, String string) {}

    // 2. Constructor yang Diperbarui (Termasuk userId)
    public CashFlow(UUID userId, String type, String source, String label, Integer amount, String description) {
        this.userId = userId; // 👈 Inisialisasi userId
        this.type = type;
        this.source = source;
        this.label = label;
        this.amount = amount;
        this.description = description;
    }

    // Lifecycle Callbacks (Sesuai Test TA: onCreate, onUpdate)
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // ------------------- Getters and Setters -------------------
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    // 👈 Getter dan Setter untuk userId
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; } 
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = (int) amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}