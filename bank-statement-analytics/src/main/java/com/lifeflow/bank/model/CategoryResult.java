package com.lifeflow.bank.model;

public enum CategoryResult {

    // INCOME
    INCOME_SALARY("INCOME_SALARY", "Salary"),
    INCOME_FREELANCE("INCOME_FREELANCE", "Freelance / Side Jobs"),
    INCOME_PASSIVE("INCOME_PASSIVE", "Passive Income"),
    INCOME_OTHER("INCOME_OTHER", "Other Income"),
    INCOME_REFUND("INCOME_REFUND", "Refunds"),

    // HOUSING
    HOUSING_RENT("HOUSING_RENT", "Rent / Mortgage"),
    HOUSING_UTILS("HOUSING_UTILS", "Utilities"),
    HOUSING_INTERNET_TV("HOUSING_INTERNET_TV", "Internet / TV / Phone"),
    HOUSING_MAINTENANCE("HOUSING_MAINTENANCE", "Maintenance / Repairs"),

    // FOOD
    FOOD_GROCERIES("FOOD_GROCERIES", "Groceries"),
    FOOD_RESTAURANT("FOOD_RESTAURANT", "Restaurants / Cafes"),
    FOOD_DELIVERY("FOOD_DELIVERY", "Food Delivery"),
    FOOD_COFFEE_SNACKS("FOOD_COFFEE_SNACKS", "Coffee / Snacks"),

    // TRANSPORT
    TRANSPORT_PUBLIC("TRANSPORT_PUBLIC", "Public Transport"),
    TRANSPORT_TAXI("TRANSPORT_TAXI", "Taxi / Carsharing"),
    TRANSPORT_FUEL("TRANSPORT_FUEL", "Fuel"),
    TRANSPORT_PARKING("TRANSPORT_PARKING", "Parking / Toll Roads"),
    CAR_SERVICE("CAR_SERVICE", "Car Service / Repairs"),
    CAR_INSURANCE("CAR_INSURANCE", "Car Insurance"),

    // HEALTH & SPORTS
    HEALTH_MEDICINE("HEALTH_MEDICINE", "Medicine / Pharmacy"),
    HEALTH_DOCTOR("HEALTH_DOCTOR", "Doctors / Clinics"),
    HEALTH_FITNESS("HEALTH_FITNESS", "Gym / Fitness"),
    HEALTH_SPA("HEALTH_SPA", "SPA / Massage"),

    // SHOPPING
    SHOPPING_CLOTHES("SHOPPING_CLOTHES", "Clothes / Shoes"),
    SHOPPING_ELECTRONICS("SHOPPING_ELECTRONICS", "Electronics / Gadgets"),
    SHOPPING_BEAUTY("SHOPPING_BEAUTY", "Beauty / Care"),
    SHOPPING_HOME("SHOPPING_HOME", "Home Goods"),
    SHOPPING_HOBBY("SHOPPING_HOBBY", "Hobby / DIY"),

    // FAMILY
    FAMILY_KIDS("FAMILY_KIDS", "Kids / School / Activities"),
    FAMILY_GENERAL("FAMILY_GENERAL", "Family Expenses"),

    // EDUCATION
    EDUCATION("EDUCATION", "Education / Courses / Books"),

    // ENTERTAINMENT
    ENTERTAINMENT("ENTERTAINMENT", "Entertainment / Movies / Games"),
    BARS_NIGHTLIFE("BARS_NIGHTLIFE", "Bars / Nightlife"),

    // TRAVEL
    TRAVEL_TRANSPORT("TRAVEL_TRANSPORT", "Travel: Transport"),
    TRAVEL_STAY("TRAVEL_STAY", "Travel: Stay"),
    TRAVEL_OTHER("TRAVEL_OTHER", "Travel: Other"),

    // SUBSCRIPTIONS
    SUBSCRIPTION_MEDIA("SUBSCRIPTION_MEDIA", "Subscriptions: Media"),
    SUBSCRIPTION_SOFTWARE("SUBSCRIPTION_SOFTWARE", "Subscriptions: Software"),
    SUBSCRIPTION_MOBILE("SUBSCRIPTION_MOBILE", "Subscriptions: Mobile / Internet"),
    SUBSCRIPTION_OTHER("SUBSCRIPTION_OTHER", "Subscriptions: Other"),

    // FEES / FINANCE / GOVERNMENT
    FEES_BANK("FEES_BANK", "Bank Fees"),
    FEES_TAXES("FEES_TAXES", "Taxes / Government Fees"),
    FEES_FINANCIAL("FEES_FINANCIAL", "Financial Services"),

    // GIFTS / DONATIONS
    GIFTS("GIFTS", "Gifts"),
    DONATIONS("DONATIONS", "Donations"),

    // PETS
    PETS("PETS", "Pets"),

    // BUSINESS
    BUSINESS_EXPENSE("BUSINESS_EXPENSE", "Business Expenses"),
    BUSINESS_SUBSCRIPTION("BUSINESS_SUBSCRIPTION", "Business Subscriptions"),

    // GENERAL
    TRANSFER("TRANSFER", "Transfers"),
    OTHER("OTHER", "Other");

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