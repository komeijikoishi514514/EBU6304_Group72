package com.wealthassistant.model;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private String name;
    private CategoryType type;
    private List<String> keywords;

    public enum CategoryType {
        INCOME, EXPENSE, BOTH
    }

    public Category() {
        this.keywords = new ArrayList<>();
    }

    public Category(String name, CategoryType type) {
        this.name = name;
        this.type = type;
        this.keywords = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    public void addKeyword(String keyword) {
        this.keywords.add(keyword);
    }
    
    public void removeKeyword(String keyword) {
        this.keywords.remove(keyword);
    }

    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return name.equals(category.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
} 