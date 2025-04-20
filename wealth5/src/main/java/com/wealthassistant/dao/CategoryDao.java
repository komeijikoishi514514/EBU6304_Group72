package com.wealthassistant.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.wealthassistant.model.AppSettings;
import com.wealthassistant.model.Category;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private final AppSettings settings = AppSettings.getInstance();
    private final ObjectMapper mapper;
    private static final String DATA_DIR = "data";

    public CategoryDao() {
        mapper = new ObjectMapper();
        // 确保数据目录存在
        new File(DATA_DIR).mkdirs();
        
        // 如果类别文件不存在，创建默认类别
        try {
            if (!new File(settings.getCategoriesFilePath()).exists()) {
                createDefaultCategories();
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public List<Category> getAllCategories() throws DataAccessException {
        try {
            File file = new File(settings.getCategoriesFilePath());
            if (!file.exists()) {
                return new ArrayList<>();
            }

            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Category.class);
            return mapper.readValue(file, listType);
        } catch (IOException e) {
            throw new DataAccessException("Read category data failed", e);
        }
    }

    public void saveCategory(Category category) throws DataAccessException {
        List<Category> categories = getAllCategories();
        // 检查是否已存在同名类别
        boolean exists = categories.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(category.getName()));
        
        if (!exists) {
            categories.add(category);
            saveAllCategories(categories);
        } else {
            throw new DataAccessException("Category '" + category.getName() + "' already exists");
        }
    }

    public void updateCategory(String oldName, Category updatedCategory) throws DataAccessException {
        List<Category> categories = getAllCategories();
        boolean updated = false;
        
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getName().equals(oldName)) {
                categories.set(i, updatedCategory);
                updated = true;
                break;
            }
        }
        
        if (updated) {
            saveAllCategories(categories);
        } else {
            throw new DataAccessException("Category '" + oldName + "' does not exist");
        }
    }

    public void deleteCategory(String name) throws DataAccessException {
        List<Category> categories = getAllCategories();
        boolean removed = categories.removeIf(c -> c.getName().equals(name));
        
        if (removed) {
            saveAllCategories(categories);
        } else {
            throw new DataAccessException("Category '" + name + "' does not exist");
        }
    }

    public Category getCategoryByName(String name) throws DataAccessException {
        return getAllCategories().stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<Category> getCategoriesByType(Category.CategoryType type) throws DataAccessException {
        return getAllCategories().stream()
                .filter(c -> c.getType() == type || c.getType() == Category.CategoryType.BOTH)
                .toList();
    }

    private void saveAllCategories(List<Category> categories) throws DataAccessException {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(settings.getCategoriesFilePath()), categories);
        } catch (IOException e) {
            throw new DataAccessException("Save category data failed", e);
        }
    }

    public void createDefaultCategories() throws DataAccessException {
        List<Category> defaultCategories = new ArrayList<>();
        
        // 收入类别
        Category salary = new Category("salary", Category.CategoryType.INCOME);
        salary.addKeyword("salary");
        salary.addKeyword("wage");
        
        Category bonus = new Category("bonus", Category.CategoryType.INCOME);
        bonus.addKeyword("bonus");
        bonus.addKeyword("reward");
        
        Category investment = new Category("investment", Category.CategoryType.INCOME);
        investment.addKeyword("investment");
        investment.addKeyword("dividend");
        investment.addKeyword("interest");
        investment.addKeyword("return");
        
        // 支出类别
        Category food = new Category("food", Category.CategoryType.EXPENSE);
        food.addKeyword("food");
        food.addKeyword("restaurant");
        food.addKeyword("takeout");
        food.addKeyword("meal");
        
        Category transportation = new Category("transportation", Category.CategoryType.EXPENSE);
        transportation.addKeyword("transportation");
        transportation.addKeyword("bus");
        transportation.addKeyword("subway");
        transportation.addKeyword("taxi");
        transportation.addKeyword("gas");
        
        Category shopping = new Category("shopping", Category.CategoryType.EXPENSE);
        shopping.addKeyword("shopping");
        shopping.addKeyword("mall");
        shopping.addKeyword("store");
        shopping.addKeyword("online");
        
        Category housing = new Category("housing", Category.CategoryType.EXPENSE);
        housing.addKeyword("housing");
        housing.addKeyword("rent");
        housing.addKeyword("utility");
        housing.addKeyword("property");
        
        Category entertainment = new Category("entertainment", Category.CategoryType.EXPENSE);
        entertainment.addKeyword("entertainment");
        entertainment.addKeyword("movie");
        entertainment.addKeyword("game");
        entertainment.addKeyword("travel");
        
        defaultCategories.add(salary);
        defaultCategories.add(bonus);
        defaultCategories.add(investment);
        defaultCategories.add(food);
        defaultCategories.add(transportation);
        defaultCategories.add(shopping);
        defaultCategories.add(housing);
        defaultCategories.add(entertainment);
        
        saveAllCategories(defaultCategories);
    }
} 