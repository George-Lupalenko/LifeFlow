package com.lifeflow.bank.service;

import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.model.BankTransaction;
import jakarta.mail.search.FromTerm;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailStatementService {

    @Value("${lifeflow.mail.imap-host}")
    private String imapHost;

    @Value("${lifeflow.mail.username}")
    private String username;

    @Value("${lifeflow.mail.password}")
    private String password;

    /**
     * Сколько последних выписок обрабатывать.
     */
    @Value("${lifeflow.autorun.email.lastCount:6}")
    private int lastCount;

    private final PdfStatementService pdfStatementService;
    private final AnalyticsService analyticsService;

    /**
     * Ищем письма по отправителю: "vypisy@tatrabanka.sk".
     * Сначала делаем IMAP SEARCH на сервере (быстро),
     * если не получилось — падаем обратно на ручной проход по письмам.
     */
    public List<AnalyticsSummaryDto> fetchLastStatementsAndLogAnalytics() {
        List<AnalyticsSummaryDto> result = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        // небольшая оптимизация по IMAP:
        props.put("mail.imaps.partialfetch", "true");

        try {
            Session session = Session.getInstance(props);
            try (Store store = session.getStore("imaps")) {
                log.info("EmailStatementService: connecting to IMAP {} as {}", imapHost, username);
                store.connect(imapHost, username, password);

                Folder folder = resolveAllMailFolder(store);
                log.info("EmailStatementService: using folder '{}'", folder.getFullName());
                folder.open(Folder.READ_ONLY);

                int total = folder.getMessageCount();
                if (total == 0) {
                    log.info("EmailStatementService: folder '{}' is empty", folder.getFullName());
                    folder.close(false);
                    return result;
                }

                // ---------- 1) Быстрая попытка: IMAP SEARCH по отправителю ----------
                Message[] candidateMessages;

                try {
                    FromTerm fromTerm = new FromTerm(new InternetAddress("vypisy@tatrabanka.sk"));
                    log.info("EmailStatementService: trying IMAP SEARCH by sender 'vypisy@tatrabanka.sk'");
                    candidateMessages = folder.search(fromTerm);
                    log.info("EmailStatementService: IMAP SEARCH finished, found {} messages", candidateMessages.length);
                } catch (MessagingException searchEx) {
                    // Здесь как раз ловим твой A4 BAD Could not parse command
                    log.warn("EmailStatementService: IMAP SEARCH failed, fallback to manual scan", searchEx);

                    // ---------- 2) Фоллбэк: берём часть писем и фильтруем вручную ----------
                    // чтобы не ходить по всем 16000, можно взять, например, последние 2000
                    int windowSize = Math.min(2000, total);
                    int start = total - windowSize + 1;
                    if (start < 1) start = 1;

                    log.info("EmailStatementService: fallback scan, fetching messages {}..{} (total = {})",
                            start, total, total);
                    Message[] window = folder.getMessages(start, total);

                    candidateMessages = filterBySender(window, "vypisy@tatrabanka.sk");
                    log.info("EmailStatementService: fallback scan finished, found {} messages", candidateMessages.length);
                }

                if (candidateMessages.length == 0) {
                    log.info("EmailStatementService: no messages found from 'vypisy@tatrabanka.sk'");
                    folder.close(false);
                    return result;
                }

                // ---------- отладка: последние 10 найденных ----------
                int debugCount = Math.min(10, candidateMessages.length);
                log.info("EmailStatementService: debug – showing last {} Tatra messages (from / subject)", debugCount);
                for (int i = candidateMessages.length - debugCount; i < candidateMessages.length; i++) {
                    if (i < 0) continue;
                    Message m = candidateMessages[i];
                    log.info("EmailStatementService: debug Tatra msg[{}]: from='{}', subject='{}'",
                            i, safeGetFrom(m), safeGetSubject(m));
                }

                // ---------- сортируем по дате (новые первыми) ----------
                Arrays.sort(candidateMessages, Comparator.comparing((Message m) -> {
                    try {
                        return m.getReceivedDate();
                    } catch (MessagingException e) {
                        return null;
                    }
                }, Comparator.nullsLast(Date::compareTo)).reversed());

                int toProcess = Math.min(lastCount, candidateMessages.length);
                log.info("EmailStatementService: will process {} latest Tatra statements (out of {})",
                        toProcess, candidateMessages.length);

                for (int i = 0; i < toProcess; i++) {
                    Message msg = candidateMessages[i];
                    String subject = safeGetSubject(msg);
                    log.info("EmailStatementService: processing statement message #{}: '{}'", i, subject);

                    List<BankTransaction> txs = extractStatementTransactionsFromMessage(msg);
                    if (txs.isEmpty()) {
                        log.info("EmailStatementService: message '{}' has no PDF statement attachments", subject);
                        continue;
                    }

                    AnalyticsSummaryDto summary = analyticsService.analyze(txs);
                    log.info("EmailStatementService: analytics for '{}': {}", subject, summary);
                    result.add(summary);
                }

                folder.close(false);
            }
        } catch (Exception e) {
            log.error("EmailStatementService: error while fetching statements from email", e);
        }

        return result;
    }

    // ---------- helpers ----------

    /**
     * Выбираем [Gmail]/Вся почта, если есть, иначе INBOX.
     */
    private Folder resolveAllMailFolder(Store store) throws MessagingException {
        Folder defaultFolder = store.getDefaultFolder();
        for (Folder f : defaultFolder.list()) {
            log.info("EmailStatementService: IMAP folder = {}", f.getFullName());
        }

        try {
            Folder ruAll = store.getFolder("[Gmail]/Вся почта");
            if (ruAll != null && ruAll.exists()) {
                return ruAll;
            }
        } catch (MessagingException ignored) {
        }

        try {
            Folder allMail = store.getFolder("[Gmail]/All Mail");
            if (allMail != null && allMail.exists()) {
                return allMail;
            }
        } catch (MessagingException ignored) {
        }

        Folder inbox = store.getFolder("INBOX");
        log.info("EmailStatementService: [Gmail]/All Mail not found, fallback to '{}'", inbox.getFullName());
        return inbox;
    }

    /**
     * Ручная фильтрация по отправителю, если SEARCH не сработал.
     */
    private Message[] filterBySender(Message[] messages, String senderEmailLowercase) {
        log.info("EmailStatementService: filtering {} messages for sender '{}'", messages.length, senderEmailLowercase);
        List<Message> out = new ArrayList<>();

        String needle = senderEmailLowercase.toLowerCase(Locale.ROOT);

        for (int i = 0; i < messages.length; i++) {
            if (i % 500 == 0) {
                log.info("EmailStatementService: filter progress {}/{}", i, messages.length);
            }

            Message msg = messages[i];
            String fromHeader = safeGetFrom(msg);
            if (fromHeader == null) continue;

            if (fromHeader.toLowerCase(Locale.ROOT).contains(needle)) {
                log.info("EmailStatementService: FOUND Tatra message [{}]: from='{}', subject='{}'",
                        i, fromHeader, safeGetSubject(msg));
                out.add(msg);
            }
        }

        log.info("EmailStatementService: filter finished, found {} candidate messages", out.size());
        return out.toArray(new Message[0]);
    }

    private String safeGetSubject(Message msg) {
        try {
            return msg.getSubject();
        } catch (MessagingException e) {
            return "(no subject)";
        }
    }

    private String safeGetFrom(Message msg) {
        try {
            Address[] from = msg.getFrom();
            if (from == null || from.length == 0) {
                return null;
            }
            return from[0].toString();
        } catch (MessagingException e) {
            return null;
        }
    }

    /**
     * Достаём все PDF-вложения из письма и парсим их как Tatra-выписки.
     */
    private List<BankTransaction> extractStatementTransactionsFromMessage(Message msg) throws Exception {
        List<BankTransaction> allTxs = new ArrayList<>();

        Object content = msg.getContent();
        if (content instanceof Multipart multipart) {
            int count = multipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bp = multipart.getBodyPart(i);

                String fileName = bp.getFileName();
                if (fileName == null) {
                    continue;
                }

                String decodedName = MimeUtility.decodeText(fileName);
                String lowerName = decodedName.toLowerCase(Locale.ROOT);

                if (!lowerName.endsWith(".pdf")) {
                    continue;
                }

                log.info("EmailStatementService: found PDF attachment '{}'", decodedName);
                try (InputStream is = bp.getInputStream()) {
                    List<BankTransaction> txs = pdfStatementService.parseTatraStatementPdf(is);
                    log.info("EmailStatementService: parsed {} transactions from '{}'",
                            txs.size(), decodedName);
                    allTxs.addAll(txs);
                }
            }
        } else {
            log.debug("EmailStatementService: message content is not Multipart, skipping attachments");
        }

        return allTxs;
    }
}