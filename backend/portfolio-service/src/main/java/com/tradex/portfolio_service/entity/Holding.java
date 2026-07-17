package com.tradex.portfolio_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;



@Entity
@Table(name="holdings",uniqueConstraints ={@UniqueConstraint(columnNames = {"userId","stockSymbol"})} )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long userId;

    @Column(nullable = false)
    private String stockSymbol;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal averageBuyPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
