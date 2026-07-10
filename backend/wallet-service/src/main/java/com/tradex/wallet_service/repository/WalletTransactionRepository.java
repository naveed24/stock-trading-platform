package com.tradex.wallet_service.repository;



import com.tradex.wallet_service.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction,Long> {
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
