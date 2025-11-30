package com.lifeflow.bank.service;

import com.lifeflow.bank.model.BankTransaction;
import com.lifeflow.bank.model.CategoryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@Slf4j
public class CategoryClassifier {

    public CategoryResult classify(BankTransaction tx) {
        // Собираем весь текст, по которому будем искать ключевые слова
        String text = ((tx.getDescription() == null ? "" : tx.getDescription()) + " " +
                (tx.getCounterparty() == null ? "" : tx.getCounterparty()))
                .toLowerCase(Locale.ROOT);

        BigDecimal amount = tx.getAmount() == null ? BigDecimal.ZERO : tx.getAmount();

        // 1. Простейшая эвристика: если amount > 0 → доход
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return classifyIncome(text);
        }

        // 2. Если <= 0 → расход → смотрим по категориям

        // ======================
        // ЕДА / ПРОДУКТЫ / РЕСТОРАНЫ
        // ======================

        // Продукты
        if (containsAny(text,
                "lidl", "tesco", "billa", "kaufland", "jednota", "coop",
                "potraviny", "grocery", "supermarket",
                // Из выписок
                "pb kosice 01", "pb kosice")) {
            return CategoryResult.FOOD_GROCERIES;
        }

        // Рестораны / кафе / кофейни
        if (containsAny(text,
                "pizzeria", "pizza", "restauracia", "restaurant", "bistro", "kebab", "kfc",
                "mcdonald", "mc donald", "burger king", "subway", "caffe", "cafe", "coffee",
                // Из выписок
                "koshi cafe", "koshice koshi cafe and restaur",
                "koshice koshi cafe", "koshice koshi", "zvon", "pizzeria zvon",
                "zatoka", "art food", "saint coffee", "sbx kosice aup")) {
            return CategoryResult.FOOD_RESTAURANT;
        }

        // Доставка еды
        if (containsAny(text,
                "wolt", "bolt food", "glovo", "ubereats", "uber eats", "foodora")) {
            return CategoryResult.FOOD_DELIVERY;
        }

        // ======================
        // ЖИЛЬЁ
        // ======================

        if (containsAny(text, "rent", "nájom", "podnájom", "hypoteka", "mortgage")) {
            return CategoryResult.HOUSING_RENT;
        }

        if (containsAny(text, "electricity", "elektrina", "gas", "voda", "water",
                "heating", "teplo", "energie", "utility")) {
            return CategoryResult.HOUSING_UTILS;
        }

        // Мобильная связь / интернет → считаем подпиской мобайл
        if (containsAny(text,
                "internet", "wifi", "telekom", "o2", "orange", "4ka", "4ka.sk",
                "isp", "tv", "cable",
                // из выписок
                "lifecell")) {
            return CategoryResult.SUBSCRIPTION_MOBILE;
        }

        // ======================
        // ТРАНСПОРТ
        // ======================

        if (containsAny(text,
                "mhd", "dopravny podnik", "public transport", "bus", "tram", "metro",
                "bus station", "eurobus")) {
            return CategoryResult.TRANSPORT_PUBLIC;
        }

        if (containsAny(text, "uber", "bolt", "lyft", "taxi", "taxisluzba")) {
            return CategoryResult.TRANSPORT_TAXI;
        }

        if (containsAny(text,
                "shell", "omv", "slovnaft", "gas station", "benzinka",
                "fuel", "diesel", "benzin")) {
            return CategoryResult.TRANSPORT_FUEL;
        }

        if (containsAny(text, "parking", "parkov", "parkovisko", "parkovné")) {
            return CategoryResult.TRANSPORT_PARKING;
        }

        // ======================
        // ЗДОРОВЬЕ / ФИТНЕС
        // ======================

        if (containsAny(text, "lekaren", "lekáreň", "pharmacy", "apotheke")) {
            return CategoryResult.HEALTH_MEDICINE;
        }

        if (containsAny(text, "klinika", "doctor", "ambulancia", "poliklinika", "hospital")) {
            return CategoryResult.HEALTH_DOCTOR;
        }

        if (containsAny(text,
                "gym", "fitness", "fitko", "workout", "sportcenter",
                // из выписок
                "astoria fit&gym", "gymbeam", "biotech usa")) {
            return CategoryResult.HEALTH_FITNESS;
        }

        // ======================
        // ШОПИНГ / КРАСОТА / ОДЕЖДА / ЭЛЕКТРОНИКА
        // ======================

        // Красота / уход
        if (containsAny(text,
                "notino", "sephora", "douglas", "dm drogerie", "rossmann",
                // из выписок
                "101 drogerie", " dm 272")) {
            return CategoryResult.SHOPPING_BEAUTY;
        }

        // Одежда
        if (containsAny(text,
                "h&m", "zara", "pull&bear", "bershka", "new yorker",
                "ccc", "footshop",
                // из выписок
                "mango.com", "mango", "lara bags")) {
            return CategoryResult.SHOPPING_CLOTHES;
        }

        // Электроника / техника
        if (containsAny(text,
                "alza", "datart", "okay elektro", "nay", "electronic", "imedia",
                // из выписок
                "mobil online")) {
            return CategoryResult.SHOPPING_ELECTRONICS;
        }

        // ======================
        // ОБУЧЕНИЕ
        // ======================

        if (containsAny(text,
                "udemy", "coursera", "linkedin learning", "duolingo",
                "skillshare", "lingoda")) {
            return CategoryResult.EDUCATION;
        }

        // ======================
        // РАЗВЛЕЧЕНИЯ / НОЧНАЯ ЖИЗНЬ
        // ======================

        if (containsAny(text,
                "cinema", "cinemax", "kino", "multikino", "cinestar")) {
            return CategoryResult.ENTERTAINMENT;
        }

        if (containsAny(text,
                "steam", "playstation", "xbox", "epic games", "gog.com", "nintendo",
                // из выписок
                "steamgames.com")) {
            return CategoryResult.ENTERTAINMENT;
        }

        if (containsAny(text, "bar", "pub", "nightclub", "club", "cocktail")) {
            return CategoryResult.BARS_NIGHTLIFE;
        }

        // ======================
        // ПУТЕШЕСТВИЯ
        // ======================

        if (containsAny(text,
                "booking.com", "bkg*booking.com", "airbnb", "hotel", "hostel", "pension")) {
            return CategoryResult.TRAVEL_STAY;
        }

        if (containsAny(text,
                "ryanair", "wizzair", "lufthansa", "austrian airlines", "airlines",
                "train", "vlak", "regiojet", "flixbus")) {
            return CategoryResult.TRAVEL_TRANSPORT;
        }

        // ======================
        // ПОДПИСКИ: медиа / софт
        // ======================

        // Медиа
        if (containsAny(text,
                "netflix", "spotify", "youtube premium", "hbo", "disney+",
                "apple tv", "deezer", "tidal",
                // из выписок
                "itunes.com apple.com/bill", "sony psn", "playstation network")) {
            return CategoryResult.SUBSCRIPTION_MEDIA;
        }

        // Софт / сервисы / облака / проездные
        if (containsAny(text,
                "apple.com/bill", "icloud", "google one", "dropbox", "onedrive",
                "microsoft 365", "office 365", "adobe", "canva", "notion", "figma",
                "slack", "github",
                // из выписок
                "ubian.sk")) {
            return CategoryResult.SUBSCRIPTION_SOFTWARE;
        }

        // ======================
        // ПОДАРКИ / ДОНАТЫ / ПИТОМЦЫ
        // ======================

        if (containsAny(text,
                "charity", "donation", "unicef", "červený kríž",
                "red cross", "fund", "foundation")) {
            return CategoryResult.DONATIONS;
        }

        if (containsAny(text,
                "gift", "darček", "flowers", "kvetinárstvo")) {
            return CategoryResult.GIFTS;
        }

        if (containsAny(text,
                "zverimex", "pet center", "petshop",
                "krmivo", "pet food", "veterinary")) {
            return CategoryResult.PETS;
        }

        // ======================
        // БАНКОВСКИЕ КОМИССИИ / НАЛОГИ
        // ======================

        if (containsAny(text,
                "fee", "poplatok", "bank fee", "vedenie uctu", "maintenance fee")) {
            return CategoryResult.FEES_BANK;
        }

        if (containsAny(text,
                "tax", "dane", "social insurance", "health insurance")) {
            return CategoryResult.FEES_TAXES;
        }

        // ======================
        // ПЕРЕВОДЫ
        // ======================

        if (containsAny(text,
                "prevod",
                "prijata platba", "prijatá platba",
                "odoslana platba", "odoslaná platba",
                "transfer", "sepa")) {
            return CategoryResult.TRANSFER;
        }

        // Если ничего не сработало — OTHER
        return CategoryResult.OTHER;
    }

    private CategoryResult classifyIncome(String text) {
        if (containsAny(text,
                "salary", "mzda", "vyplata", "výplata", "wage", "payroll")) {
            return CategoryResult.INCOME_SALARY;
        }

        if (containsAny(text,
                "invoice", "faktura", "faktúra", "freelance", "contractor", "odmena")) {
            return CategoryResult.INCOME_FREELANCE;
        }

        if (containsAny(text,
                "dividend", "dividenda", "interest", "úrok", "urok", "yield")) {
            return CategoryResult.INCOME_PASSIVE;
        }

        if (containsAny(text,
                "refund", "reklamacia", "reklamácia",
                "vratka", "cashback", "chargeback")) {
            return CategoryResult.INCOME_REFUND;
        }

        // Остальное — просто прочий доход
        return CategoryResult.INCOME_OTHER;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}