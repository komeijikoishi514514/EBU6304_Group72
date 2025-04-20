package com.wealthassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.dao.SettingsDao;
import com.wealthassistant.model.AppSettings;
import com.wealthassistant.dao.CategoryDao;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SettingsService {
    private final SettingsDao settingsDao;
    private AppSettings settings;
    
    public SettingsService() {
        this.settingsDao = new SettingsDao();
        try {
            this.settings = settingsDao.loadSettings();
        } catch (DataAccessException e) {
            // 如果加载失败，使用默认设置
            this.settings = AppSettings.getInstance();
        }
    }
    
    public AppSettings getSettings() {
        return settings;
    }
    
    public void saveSettings() throws DataAccessException {
        settingsDao.saveSettings(settings);
    }
    
    public void setDefaultCurrency(String currency) {
        settings.setDefaultCurrency(currency);
    }
    
    public String getDefaultCurrency() {
        return settings.getDefaultCurrency();
    }
    
    public void setDateFormat(String dateFormat) {
        settings.setDateFormat(dateFormat);
    }
    
    public String getDateFormat() {
        return settings.getDateFormat();
    }
    
    public void setFileFormat(String fileFormat) {
        settings.setFileFormat(fileFormat);
    }
    
    public String getFileFormat() {
        return settings.getFileFormat();
    }
    
    public void setTransactionsFilePath(String path) {
        settings.setTransactionsFilePath(path);
    }
    
    public String getTransactionsFilePath() {
        return settings.getTransactionsFilePath();
    }
    
    public void setBudgetsFilePath(String path) {
        settings.setBudgetsFilePath(path);
    }
    
    public String getBudgetsFilePath() {
        return settings.getBudgetsFilePath();
    }
    
    public void setCategoriesFilePath(String path) {
        settings.setCategoriesFilePath(path);
    }
    
    public String getCategoriesFilePath() {
        return settings.getCategoriesFilePath();
    }
    
    public void setEnableAutoAssignment(boolean enable) {
        settings.setEnableAutoAssignment(enable);
    }
    
    public boolean isEnableAutoAssignment() {
        return settings.isEnableAutoAssignment();
    }
    
    /**
     * 清除所有用户数据
     * 包括交易、预算和类别数据
     */
    public void clearAllData() throws DataAccessException {
        try {
            // 清空交易数据
            File transactionsFile = new File(settings.getTransactionsFilePath());
            if (transactionsFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(transactionsFile, new ArrayList<>());
            }
            
            // 清空预算数据
            File budgetsFile = new File(settings.getBudgetsFilePath());
            if (budgetsFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(budgetsFile, new ArrayList<>());
            }
            
            // 清空类别数据
            File categoriesFile = new File(settings.getCategoriesFilePath());
            if (categoriesFile.exists()) {
                // 将使用默认类别替换当前类别
                new CategoryDao().createDefaultCategories();
            }
            
        } catch (IOException e) {
            throw new DataAccessException("清除数据失败: " + e.getMessage(), e);
        }
    }
} 