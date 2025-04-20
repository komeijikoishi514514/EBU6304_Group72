package com.wealthassistant.model;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {
    private String id;
    private double amount;
    private String currencyUnit;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;

    public enum TransactionType {
        INCOME, EXPENSE
    }

    public Transaction() {
        this.id = UUID.randomUUID().toString();
    }
    
    public Transaction(double amount, String currencyUnit, TransactionType type, 
                      String category, LocalDate date, String notes) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.currencyUnit = currencyUnit;
        this.type = type;
        this.category = category;
        this.date = date;
        this.notes = notes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrencyUnit() {
        return currencyUnit;
    }

    public void setCurrencyUnit(String currencyUnit) {
        this.currencyUnit = currencyUnit;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", currencyUnit='" + currencyUnit + '\'' +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", notes='" + notes + '\'' +
                '}';
    }
} 