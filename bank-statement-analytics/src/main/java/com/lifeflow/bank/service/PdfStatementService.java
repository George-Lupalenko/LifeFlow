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

    /**
     * Расшифровать PDF и распарсить транзакции. Ничего не сохраняем.
     */
    public List<BankTransaction> parseTatraStatementPdf(InputStream pdfStream, String pdfPassword) {
        try (PDDocument doc = load(pdfStream, pdfPassword)) {

            log.info("PDF loaded (encrypted={}, pages={})", doc.isEncrypted(), doc.getNumberOfPages());

            String text = new PDFTextStripper().getText(doc);

            TatraStatementParser.Result parsed = parser.parse(text);
            return parsed.getTransactions();

        } catch (IOException e) {
            log.error("Failed to process PDF", e);
            throw new RuntimeException("PDF parse error", e);
        }
    }

    private PDDocument load(InputStream is, String password) throws IOException {
        if (password == null || password.isBlank()) {
            log.warn("User PDF password is empty → opening without password");
            return PDDocument.load(is);
        }
        log.info("Opening PDF with password of length {}", password.length());
        return PDDocument.load(is, password);
    }
}