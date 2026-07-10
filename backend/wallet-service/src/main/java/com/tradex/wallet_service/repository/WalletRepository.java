package com.tradex.wallet_service.repository;

import com.tradex.wallet_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet,Long> {

    boolean existsByUserId(Long userId);

    Optional<Wallet> findByUserId(Long UserId);
}
