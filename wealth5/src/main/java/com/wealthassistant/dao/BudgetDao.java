package com.wealthassistant.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.wealthassistant.model.AppSettings;
import com.wealthassistant.model.Budget;
import com.wealthassistant.model.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BudgetDao {
    private final AppSettings settings = AppSettings.getInstance();
    private final ObjectMapper mapper;
    private static final String DATA_DIR = "data";

    public BudgetDao() {
        mapper = new ObjectMapper();
        // 确保数据目录存在
        new File(DATA_DIR).mkdirs();
    }

    public List<Budget> getAllBudgets() throws DataAccessException {
        try {
            File file = new File(settings.getBudgetsFilePath());
            if (!file.exists()) {
                return new ArrayList<>();
            }

            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Budget.class);
            return mapper.readValue(file, listType);
        } catch (IOException e) {
            throw new DataAccessException("读取预算数据失败", e);
        }
    }

    public void saveBudget(Budget budget) throws DataAccessException {
        List<Budget> budgets = getAllBudgets();
        // 检查是否已存在同类别的预算
        boolean exists = false;
        for (int i = 0; i < budgets.size(); i++) {
            if (budgets.get(i).getCategory().equals(budget.getCategory())) {
                // 更新已存在的预算
                budgets.set(i, budget);
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            budgets.add(budget);
        }
        
        saveAllBudgets(budgets);
    }

    public void deleteBudget(String category) throws DataAccessException {
        List<Budget> budgets = getAllBudgets();
        boolean removed = budgets.removeIf(b -> b.getCategory().equals(category));
        
        if (removed) {
            saveAllBudgets(budgets);
        } else {
            throw new DataAccessException("类别 '" + category + "' 的预算不存在");
        }
    }

    public Budget getBudgetByCategory(String category) throws DataAccessException {
        return getAllBudgets().stream()
                .filter(b -> b.getCategory().equals(category))
                .findFirst()
                .orElse(null);
    }

    private void saveAllBudgets(List<Budget> budgets) throws DataAccessException {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(settings.getBudgetsFilePath()), budgets);
        } catch (IOException e) {
            throw new DataAccessException("保存预算数据失败", e);
        }
    }
    
    public void initDefaultBudgets() throws DataAccessException {
        // 获取所有支出类别
        CategoryDao categoryDao = new CategoryDao();
        List<Category> expenseCategories = categoryDao.getCategoriesByType(Category.CategoryType.EXPENSE);
        
        List<Budget> defaultBudgets = new ArrayList<>();
        String defaultCurrency = settings.getDefaultCurrency();
        
        // 为每个支出类别创建默认预算
        for (Category category : expenseCategories) {
            Budget budget = new Budget(category.getName(), 2000.0, 24000.0, defaultCurrency);
            defaultBudgets.add(budget);
        }
        
        saveAllBudgets(defaultBudgets);
    }
} 