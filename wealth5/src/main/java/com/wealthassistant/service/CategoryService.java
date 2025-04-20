package com.wealthassistant.service;

import com.wealthassistant.dao.CategoryDao;
import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.model.Category;
import com.wealthassistant.model.Transaction;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryService {
    private final CategoryDao categoryDao;
    
    public CategoryService() {
        this.categoryDao = new CategoryDao();
    }
    
    public List<Category> getAllCategories() throws DataAccessException {
        return categoryDao.getAllCategories();
    }
    
    public List<Category> getCategoriesByType(Category.CategoryType type) throws DataAccessException {
        return categoryDao.getCategoriesByType(type);
    }
    
    public Category getCategoryByName(String name) throws DataAccessException {
        return categoryDao.getCategoryByName(name);
    }
    
    public void saveCategory(Category category) throws DataAccessException {
        categoryDao.saveCategory(category);
    }
    
    public void updateCategory(String oldName, Category updatedCategory) throws DataAccessException {
        categoryDao.updateCategory(oldName, updatedCategory);
    }
    
    public void deleteCategory(String name) throws DataAccessException {
        categoryDao.deleteCategory(name);
    }
    
    public List<String> getCategoryNames() throws DataAccessException {
        return getAllCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
    
    public List<String> getCategoryNamesByType(Category.CategoryType type) throws DataAccessException {
        return getCategoriesByType(type).stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
    
    public String findCategoryByKeywords(Transaction.TransactionType transactionType, String description) {
        try {
            List<Category> categories;
            
            if (transactionType == Transaction.TransactionType.INCOME) {
                categories = getCategoriesByType(Category.CategoryType.INCOME);
            } else {
                categories = getCategoriesByType(Category.CategoryType.EXPENSE);
            }
            
            for (Category category : categories) {
                for (String keyword : category.getKeywords()) {
                    if (description.toLowerCase().contains(keyword.toLowerCase())) {
                        return category.getName();
                    }
                }
            }
            
            // 如果没有匹配的类别，返回默认类别
            return transactionType == Transaction.TransactionType.INCOME ? "其他收入" : "其他支出";
        } catch (DataAccessException e) {
            e.printStackTrace();
            return transactionType == Transaction.TransactionType.INCOME ? "其他收入" : "其他支出";
        }
    }
} 