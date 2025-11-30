package com.lifeflow.bank.dto;

public record UserEmailRequest(
        String username,     // Gmail
        String password,     // app password
        int lastCount,       // сколько выписок брать
        String pdfPassword   // пароль от PDF-выписок
) {}