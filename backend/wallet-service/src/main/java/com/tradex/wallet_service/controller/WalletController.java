package com.tradex.wallet_service.controller;


import com.tradex.wallet_service.dto.AmountRequest;
import com.tradex.wallet_service.dto.CreateWalletRequest;
import com.tradex.wallet_service.dto.WalletResponse;
import com.tradex.wallet_service.dto.WalletTransactionResponse;
import com.tradex.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/status")
    public ResponseEntity<String> status(){
        return ResponseEntity.ok("Wallet service is running successfully");
    }

    @PostMapping("/create")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(walletService.createWallet(request));
    }

    // Get wallet by user id.
    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWalletByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    // Deposit money.
    @PostMapping("/deposit")
    public ResponseEntity<WalletResponse> deposit(@Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.deposit(request));
    }

    // Withdraw money.
    @PostMapping("/withdraw")
    public ResponseEntity<WalletResponse> withdraw(@Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.withdraw(request));
    }

    // Block amount for buy order.
    @PostMapping("/block")
    public ResponseEntity<WalletResponse> blockAmount(@Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.blockAmount(request));
    }

    // Release blocked amount if order fails/cancelled.
    @PostMapping("/release")
    public ResponseEntity<WalletResponse> releaseAmount(@Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.releaseAmount(request));
    }

    // Debit amount after buy order is executed.
    @PostMapping("/debit")
    public ResponseEntity<WalletResponse> debitAmount(@Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.debitAmount(request));
    }

    @PostMapping("/credit")
    public ResponseEntity<WalletResponse> creditAmount(@Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(walletService.creditAmount(request));
    }

    // Get wallet transaction history.
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getTransactions(userId));
    }

}
