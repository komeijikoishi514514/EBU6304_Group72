package com.wealthassistant.model;

public class Budget {
    private String category;
    private double monthlyLimit;
    private double annualLimit;
    private String currencyUnit;

    public Budget() {
    }

    public Budget(String category, double monthlyLimit, double annualLimit, String currencyUnit) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
        this.annualLimit = annualLimit;
        this.currencyUnit = currencyUnit;
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getAnnualLimit() {
        return annualLimit;
    }

    public void setAnnualLimit(double annualLimit) {
        this.annualLimit = annualLimit;
    }

    public String getCurrencyUnit() {
        return currencyUnit;
    }

    public void setCurrencyUnit(String currencyUnit) {
        this.currencyUnit = currencyUnit;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "category='" + category + '\'' +
                ", monthlyLimit=" + monthlyLimit +
                ", annualLimit=" + annualLimit +
                ", currencyUnit='" + currencyUnit + '\'' +
                '}';
    }
} 