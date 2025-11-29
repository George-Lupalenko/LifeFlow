package com.lifeflow.bank.model;

public enum CategoryResult {

    // ДОХОДЫ
    INCOME_SALARY("INCOME_SALARY", "Зарплата"),
    INCOME_FREELANCE("INCOME_FREELANCE", "Подработка / фриланс"),
    INCOME_PASSIVE("INCOME_PASSIVE", "Пассивный доход"),
    INCOME_OTHER("INCOME_OTHER", "Прочие доходы"),
    INCOME_REFUND("INCOME_REFUND", "Возвраты / рефанд"),

    // ЖИЛЬЁ
    HOUSING_RENT("HOUSING_RENT", "Аренда / ипотека"),
    HOUSING_UTILS("HOUSING_UTILS", "Коммунальные услуги"),
    HOUSING_INTERNET_TV("HOUSING_INTERNET_TV", "Интернет / ТВ / телефон"),
    HOUSING_MAINTENANCE("HOUSING_MAINTENANCE", "Ремонт и обслуживание"),

    // ЕДА
    FOOD_GROCERIES("FOOD_GROCERIES", "Продукты"),
    FOOD_RESTAURANT("FOOD_RESTAURANT", "Рестораны / кафе"),
    FOOD_DELIVERY("FOOD_DELIVERY", "Доставка еды"),
    FOOD_COFFEE_SNACKS("FOOD_COFFEE_SNACKS", "Кофе / снеки"),

    // ТРАНСПОРТ / АВТО
    TRANSPORT_PUBLIC("TRANSPORT_PUBLIC", "Общественный транспорт"),
    TRANSPORT_TAXI("TRANSPORT_TAXI", "Такси / каршеринг"),
    TRANSPORT_FUEL("TRANSPORT_FUEL", "Топливо"),
    TRANSPORT_PARKING("TRANSPORT_PARKING", "Парковка / платные дороги"),
    CAR_SERVICE("CAR_SERVICE", "Сервис / ремонт авто"),
    CAR_INSURANCE("CAR_INSURANCE", "Страховка авто"),

    // ЗДОРОВЬЕ И СПОРТ
    HEALTH_MEDICINE("HEALTH_MEDICINE", "Аптеки / лекарства"),
    HEALTH_DOCTOR("HEALTH_DOCTOR", "Врачи / клиники"),
    HEALTH_FITNESS("HEALTH_FITNESS", "Спортзал / фитнес"),
    HEALTH_SPA("HEALTH_SPA", "SPA / массаж"),

    // ШОПИНГ / ЛИЧНОЕ
    SHOPPING_CLOTHES("SHOPPING_CLOTHES", "Одежда / обувь"),
    SHOPPING_ELECTRONICS("SHOPPING_ELECTRONICS", "Электроника / гаджеты"),
    SHOPPING_BEAUTY("SHOPPING_BEAUTY", "Косметика / уход"),
    SHOPPING_HOME("SHOPPING_HOME", "Домашние товары"),
    SHOPPING_HOBBY("SHOPPING_HOBBY", "Хобби / DIY"),

    // СЕМЬЯ / ДЕТИ
    FAMILY_KIDS("FAMILY_KIDS", "Дети / школа / кружки"),
    FAMILY_GENERAL("FAMILY_GENERAL", "Семейные расходы"),

    // ОБУЧЕНИЕ
    EDUCATION("EDUCATION", "Обучение / курсы / книги"),

    // РАЗВЛЕЧЕНИЯ
    ENTERTAINMENT("ENTERTAINMENT", "Развлечения / кино / игры"),
    BARS_NIGHTLIFE("BARS_NIGHTLIFE", "Бары / ночная жизнь"),

    // ПУТЕШЕСТВИЯ
    TRAVEL_TRANSPORT("TRAVEL_TRANSPORT", "Путешествия: транспорт"),
    TRAVEL_STAY("TRAVEL_STAY", "Путешествия: жильё"),
    TRAVEL_OTHER("TRAVEL_OTHER", "Путешествия: прочее"),

    // ПОДПИСКИ / ЦИФРА
    SUBSCRIPTION_MEDIA("SUBSCRIPTION_MEDIA", "Подписки: медиа (Netflix, Spotify)"),
    SUBSCRIPTION_SOFTWARE("SUBSCRIPTION_SOFTWARE", "Подписки: софт / сервисы"),
    SUBSCRIPTION_MOBILE("SUBSCRIPTION_MOBILE", "Подписки: мобильная связь / интернет"),
    SUBSCRIPTION_OTHER("SUBSCRIPTION_OTHER", "Подписки: прочее"),

    // ФИНАНСЫ / ГОСУДАРСТВО
    FEES_BANK("FEES_BANK", "Банковские комиссии"),
    FEES_TAXES("FEES_TAXES", "Налоги / гос.сборы"),
    FEES_FINANCIAL("FEES_FINANCIAL", "Финансовые услуги"),

    // ПОДАРКИ / ДОНАТЫ
    GIFTS("GIFTS", "Подарки"),
    DONATIONS("DONATIONS", "Благотворительность / донаты"),

    // ПИТОМЦЫ
    PETS("PETS", "Питомцы"),

    // БИЗНЕС / РАБОТА
    BUSINESS_EXPENSE("BUSINESS_EXPENSE", "Рабочие расходы"),
    BUSINESS_SUBSCRIPTION("BUSINESS_SUBSCRIPTION", "Подписки для работы"),

    // ОБЩЕЕ / ПО УМОЛЧАНИЮ
    TRANSFER("TRANSFER", "Переводы"),
    OTHER("OTHER", "Прочее");

    private final String code;
    private final String displayName;

    CategoryResult(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}