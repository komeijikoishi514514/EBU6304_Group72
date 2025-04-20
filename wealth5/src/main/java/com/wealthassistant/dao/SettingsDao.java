package com.wealthassistant.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthassistant.model.AppSettings;

import java.io.File;
import java.io.IOException;

public class SettingsDao {
    private static final String SETTINGS_FILE = "data/settings.json";
    private static final String DATA_DIR = "data";
    private final ObjectMapper mapper;

    public SettingsDao() {
        mapper = new ObjectMapper();
        // 确保数据目录存在
        new File(DATA_DIR).mkdirs();
    }

    public void saveSettings(AppSettings settings) throws DataAccessException {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            throw new DataAccessException("保存设置失败", e);
        }
    }

    public AppSettings loadSettings() throws DataAccessException {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            // 如果设置文件不存在，返回默认设置
            return AppSettings.getInstance();
        }

        try {
            return mapper.readValue(file, AppSettings.class);
        } catch (IOException e) {
            throw new DataAccessException("加载设置失败", e);
        }
    }
} 