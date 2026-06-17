package com.sandistay.backend.modules.payment.dto;

public record SenderInfo(
        String name,
        String documentId,
        String phone,
        String bank
) {}