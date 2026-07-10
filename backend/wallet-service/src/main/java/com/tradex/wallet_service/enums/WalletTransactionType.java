package com.tradex.wallet_service.enums;

public enum WalletTransactionType {

    // User adds money to wallet.
    DEPOSIT,

    // User withdraws money from wallet.
    WITHDRAW,

    // Money is blocked when user places a buy order.
    BLOCK_AMOUNT,

    // Blocked money is released if order fails or gets cancelled.
    RELEASE_AMOUNT,

    // Money is finally debited after buy order is executed.
    DEBIT,

    // Money is credited after sell order is executed.
    CREDIT
}
