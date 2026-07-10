package com.tradex.wallet_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This userId comes from auth-service user table.
    // For now, we store only userId instead of creating direct relationship.
    @Column(nullable = false, unique = true)
    private Long userId;

    // Total wallet balance.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    // Amount blocked for pending buy orders.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal blockedAmount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }

        if (this.blockedAmount == null) {
            this.blockedAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
