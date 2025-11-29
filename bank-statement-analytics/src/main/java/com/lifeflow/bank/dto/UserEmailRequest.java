package com.lifeflow.bank.dto;

public record UserEmailRequest(
        String imapHost,
        String username,
        String password,
        int lastCount
) {}