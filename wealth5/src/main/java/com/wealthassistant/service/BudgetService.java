package com.wealthassistant.service;

import com.wealthassistant.dao.BudgetDao;
import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.model.Budget;
import com.wealthassistant.model.Transaction;

import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private final BudgetDao budgetDao;
    private final TransactionService transactionService;
    
    public BudgetService() {
        this.budgetDao = new BudgetDao();
        this.transactionService = new TransactionService();
    }
    
    public List<Budget> getAllBudgets() throws DataAccessException {
        return budgetDao.getAllBudgets();
    }
    
    public Budget getBudgetByCategory(String category) throws DataAccessException {
        return budgetDao.getBudgetByCategory(category);
    }
    
    public void saveBudget(Budget budget) throws DataAccessException {
        budgetDao.saveBudget(budget);
    }
    
    public void deleteBudget(String category) throws DataAccessException {
        budgetDao.deleteBudget(category);
    }
    
    public void initDefaultBudgets() throws DataAccessException {
        budgetDao.initDefaultBudgets();
    }
    
    public Map<String, BudgetStatus> checkBudgetStatus(int year, Month month) throws DataAccessException {
        Map<String, BudgetStatus> result = new HashMap<>();
        Map<String, Double> expenses = transactionService.getExpensesByCategory(year, month);
        List<Budget> budgets = getAllBudgets();
        
        for (Budget budget : budgets) {
            String category = budget.getCategory();
            double spent = expenses.getOrDefault(category, 0.0);
            double limit = budget.getMonthlyLimit();
            double percentage = spent / limit * 100;
            BudgetStatus status;
            
            if (percentage >= 100) {
                status = new BudgetStatus(spent, limit, BudgetStatus.Status.OVER_BUDGET);
            } else if (percentage >= 80) {
                status = new BudgetStatus(spent, limit, BudgetStatus.Status.WARNING);
            } else {
                status = new BudgetStatus(spent, limit, BudgetStatus.Status.NORMAL);
            }
            
            result.put(category, status);
        }
        
        return result;
    }
    
    public static class BudgetStatus {
        public enum Status {
            NORMAL, WARNING, OVER_BUDGET
        }
        
        private final double spent;
        private final double limit;
        private final Status status;
        
        public BudgetStatus(double spent, double limit, Status status) {
            this.spent = spent;
            this.limit = limit;
            this.status = status;
        }
        
        public double getSpent() {
            return spent;
        }
        
        public double getLimit() {
            return limit;
        }
        
        public Status getStatus() {
            return status;
        }
        
        public double getPercentage() {
            return (spent / limit) * 100;
        }
        
        public double getRemaining() {
            return limit - spent;
        }
    }
} 