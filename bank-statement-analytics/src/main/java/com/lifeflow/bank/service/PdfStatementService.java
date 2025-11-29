package com.lifeflow.bank.service;

import com.lifeflow.bank.model.BankTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfStatementService {

    private final TatraStatementParser parser;

    @Value("${tatrabanka.pdf-password:}")
    private String pdfPassword;

    /**
     * Расшифровать PDF и распарсить транзакции. Ничего не сохраняем.
     */
    public List<BankTransaction> parseTatraStatementPdf(InputStream pdfStream) {
        try (PDDocument doc = load(pdfStream)) {
            log.info("PDF document loaded successfully (encrypted={}, allPages={})",
                    doc.isEncrypted(), doc.getNumberOfPages());

            String text = new PDFTextStripper().getText(doc);

            TatraStatementParser.Result parsed = parser.parse(text);
            int txCount = parsed.getTransactions() != null ? parsed.getTransactions().size() : 0;
            log.info("Parsed statement period {} - {}, transactions={}",
                    parsed.getPeriodFrom(), parsed.getPeriodTo(), txCount);

            return parsed.getTransactions();
        } catch (IOException e) {
            log.error("Failed to process PDF statement", e);
            throw new RuntimeException("PDF parse error", e);
        }
    }

    private PDDocument load(InputStream is) throws IOException {
        if (pdfPassword == null || pdfPassword.isBlank()) {
            log.warn("tatrabanka.pdf-password is empty, trying to open PDF without password");
            return PDDocument.load(is);
        }
        log.info("Opening PDF with password from tatrabanka.pdf-password (length={})", pdfPassword.length());
        return PDDocument.load(is, pdfPassword);
    }
}