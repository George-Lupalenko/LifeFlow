package com.lifeflow.bank.dto;

import lombok.Data;

@Data
public class EmailCredentialsRequest {
    // Можно захардкодить imapHost на бэке как "imap.gmail.com"
    private String email;        // user@gmail.com
    private String appPassword;  // Gmail App Password (16 символов)
    private Integer lastCount;   // опционально, сколько выписок брать (по умолчанию 6)
}