package com.tradex.wallet_service.service;

import com.tradex.wallet_service.dto.AmountRequest;
import com.tradex.wallet_service.dto.CreateWalletRequest;
import com.tradex.wallet_service.dto.WalletResponse;
import com.tradex.wallet_service.dto.WalletTransactionResponse;
import com.tradex.wallet_service.entity.Wallet;
import com.tradex.wallet_service.entity.WalletTransaction;
import com.tradex.wallet_service.enums.WalletTransactionType;
import com.tradex.wallet_service.repository.WalletRepository;
import com.tradex.wallet_service.repository.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

// This class contains wallet business logic.
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletResponse createWallet(CreateWalletRequest request){
        if(walletRepository.existsByUserId(request.getUserId())){
            throw new RuntimeException("Wallet already exists for user id: " + request.getUserId());
        }

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .balance(BigDecimal.ZERO)
                .blockedAmount(BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        return mapToWalletResponse(savedWallet);
    }

    //Get wallet details
    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = getWallet(userId);
        return mapToWalletResponse(wallet);
    }

    //Deposit amount into wallet
    @Transactional
    public WalletResponse deposit(AmountRequest request) {

        Wallet wallet = getWallet(request.getUserId());

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        saveTransaction(
                wallet.getUserId(),
                WalletTransactionType.DEPOSIT,
                request.getAmount(),
                updatedWallet.getBalance(),
                "Amount deposited into wallet"
        );

        return mapToWalletResponse(updatedWallet);
    }

    // Withdraw amount from wallet.
    @Transactional
    public WalletResponse withdraw(AmountRequest request){

        Wallet wallet=getWallet(request.getUserId());

        BigDecimal availableBalance=calculateAvailableBalance(wallet);

        if(availableBalance.compareTo(request.getAmount())<0){
            throw new RuntimeException("Insufficient available balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        saveTransaction(
                wallet.getUserId(),
                WalletTransactionType.WITHDRAW,
                request.getAmount(),
                updatedWallet.getBalance(),
                "Amount withdrawn from wallet"
        );

        return mapToWalletResponse(updatedWallet);
    }

    // Block amount when user places buy order.
    @Transactional
    public WalletResponse blockAmount(AmountRequest request) {

        Wallet wallet = getWallet(request.getUserId());

        BigDecimal availableBalance = calculateAvailableBalance(wallet);

        if (availableBalance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient available balance to block amount");
        }

        wallet.setBlockedAmount(wallet.getBlockedAmount().add(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        saveTransaction(
                wallet.getUserId(),
                WalletTransactionType.BLOCK_AMOUNT,
                request.getAmount(),
                updatedWallet.getBalance(),
                "Amount blocked for buy order"
        );

        return mapToWalletResponse(updatedWallet);
    }


    // Release blocked amount if order fails or gets cancelled.
    @Transactional
    public WalletResponse releaseAmount(AmountRequest request) {

        Wallet wallet = getWallet(request.getUserId());

        if (wallet.getBlockedAmount().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Blocked amount is less than release amount");
        }

        wallet.setBlockedAmount(wallet.getBlockedAmount().subtract(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        saveTransaction(
                wallet.getUserId(),
                WalletTransactionType.RELEASE_AMOUNT,
                request.getAmount(),
                updatedWallet.getBalance(),
                "Blocked amount released"
        );

        return mapToWalletResponse(updatedWallet);
    }

    // Debit amount after buy order is successfully executed.
    @Transactional
    public WalletResponse debitAmount(AmountRequest request) {

        Wallet wallet = getWallet(request.getUserId());

        if (wallet.getBlockedAmount().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Blocked amount is less than debit amount");
        }

        wallet.setBlockedAmount(wallet.getBlockedAmount().subtract(request.getAmount()));
        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        saveTransaction(
                wallet.getUserId(),
                WalletTransactionType.DEBIT,
                request.getAmount(),
                updatedWallet.getBalance(),
                "Amount debited after successful buy order"
        );

        return mapToWalletResponse(updatedWallet);
    }


    // Credit amount after sell order is successfully executed.
    @Transactional
    public WalletResponse creditAmount(AmountRequest request) {

        Wallet wallet = getWallet(request.getUserId());

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));

        Wallet updatedWallet = walletRepository.save(wallet);

        saveTransaction(
                wallet.getUserId(),
                WalletTransactionType.CREDIT,
                request.getAmount(),
                updatedWallet.getBalance(),
                "Amount credited after successful sell order"
        );

        return mapToWalletResponse(updatedWallet);
    }

    // Get wallet transaction history.
    public List<WalletTransactionResponse> getTransactions(Long userId) {

        return walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    // Helper method to fetch wallet or throw exception.
    private Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user id: " + userId));
    }

    // Available balance = total balance - blocked amount.
    private BigDecimal calculateAvailableBalance(Wallet wallet) {
        return wallet.getBalance().subtract(wallet.getBlockedAmount());
    }

    // Save transaction history.
    private void saveTransaction(
            Long userId,
            WalletTransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfterTransaction,
            String description
    ) {
        WalletTransaction transaction = WalletTransaction.builder()
                .userId(userId)
                .transactionType(type)
                .amount(amount)
                .balanceAfterTransaction(balanceAfterTransaction)
                .description(description)
                .build();

        walletTransactionRepository.save(transaction);
    }

    // Convert Wallet entity to WalletResponse DTO.
    private WalletResponse mapToWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .blockedAmount(wallet.getBlockedAmount())
                .availableBalance(calculateAvailableBalance(wallet))
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    // Convert WalletTransaction entity to WalletTransactionResponse DTO.
    private WalletTransactionResponse mapToTransactionResponse(WalletTransaction transaction) {
        return WalletTransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

}
