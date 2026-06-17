package com.sandistay.backend.modules.payment.dto;

import java.math.BigDecimal;

public record PaymentVerificationResponse(
        boolean found,
        String receiptId,
        String transferDate,
        BigDecimal amount,
        String currency,
        SenderInfo sender,
        String destinationAccount,
        String message
) {}