package com.wealthassistant.model;

public class AppSettings {
    private String defaultCurrency;
    private String dateFormat;
    private String fileFormat;
    private String transactionsFilePath;
    private String budgetsFilePath;
    private String categoriesFilePath;
    private boolean enableAutoAssignment;

    // 单例模式
    private static AppSettings instance;

    private AppSettings() {
        // 默认设置
        this.defaultCurrency = "CNY";
        this.dateFormat = "yyyy-MM-dd";
        this.fileFormat = "JSON";
        this.transactionsFilePath = "data/transactions.json";
        this.budgetsFilePath = "data/budgets.json";
        this.categoriesFilePath = "data/categories.json";
        this.enableAutoAssignment = true;
    }

    public static synchronized AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }

    // Getters and Setters
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getTransactionsFilePath() {
        return transactionsFilePath;
    }

    public void setTransactionsFilePath(String transactionsFilePath) {
        this.transactionsFilePath = transactionsFilePath;
    }

    public String getBudgetsFilePath() {
        return budgetsFilePath;
    }

    public void setBudgetsFilePath(String budgetsFilePath) {
        this.budgetsFilePath = budgetsFilePath;
    }

    public String getCategoriesFilePath() {
        return categoriesFilePath;
    }

    public void setCategoriesFilePath(String categoriesFilePath) {
        this.categoriesFilePath = categoriesFilePath;
    }

    public boolean isEnableAutoAssignment() {
        return enableAutoAssignment;
    }

    public void setEnableAutoAssignment(boolean enableAutoAssignment) {
        this.enableAutoAssignment = enableAutoAssignment;
    }
} 