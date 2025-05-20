package com.wealthassistant.service;

import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.dao.TransactionDao;
import com.wealthassistant.model.Category;
import com.wealthassistant.model.Transaction;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {
    private final TransactionDao transactionDao;
    
    public TransactionService() {
        this.transactionDao = new TransactionDao();
    }
    
    public List<Transaction> getAllTransactions() throws DataAccessException {
        return transactionDao.getAllTransactions();
    }
    
    public void saveTransaction(Transaction transaction) throws DataAccessException {
        transactionDao.saveTransaction(transaction);
    }
    
    public void updateTransaction(Transaction transaction) throws DataAccessException {
        transactionDao.updateTransaction(transaction);
    }
    
    public void deleteTransaction(String id) throws DataAccessException {
        transactionDao.deleteTransaction(id);
    }
    
    public List<Transaction> getTransactionsByMonth(int year, Month month) throws DataAccessException {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        return transactionDao.getTransactionsByDate(startDate, endDate);
    }
    
    public List<Transaction> getTransactionsByYear(int year) throws DataAccessException {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return transactionDao.getTransactionsByDate(startDate, endDate);
    }
    
    public double getTotalIncomeForMonth(int year, Month month) throws DataAccessException {
        return getTransactionsByMonth(year, month).stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    public double getTotalExpenseForMonth(int year, Month month) throws DataAccessException {
        return getTransactionsByMonth(year, month).stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    public double getTotalIncomeForYear(int year) throws DataAccessException {
        return getTransactionsByYear(year).stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    public double getTotalExpenseForYear(int year) throws DataAccessException {
        return getTransactionsByYear(year).stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
    
    public Map<String, Double> getExpensesByCategory(int year, Month month) throws DataAccessException {
        List<Transaction> transactions = getTransactionsByMonth(year, month).stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.toList());
        
        Map<String, Double> result = new HashMap<>();
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory();
            double amount = result.getOrDefault(category, 0.0);
            result.put(category, amount + transaction.getAmount());
        }
        
        return result;
    }
    
    public Map<String, Double> getIncomeByCategory(int year, Month month) throws DataAccessException {
        List<Transaction> transactions = getTransactionsByMonth(year, month).stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .collect(Collectors.toList());
        
        Map<String, Double> result = new HashMap<>();
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory();
            double amount = result.getOrDefault(category, 0.0);
            result.put(category, amount + transaction.getAmount());
        }
        
        return result;
    }
    
    public Map<Month, Double> getMonthlyTotals(int year, Transaction.TransactionType type) throws DataAccessException {
        Map<Month, Double> result = new HashMap<>();
        for (Month month : Month.values()) {
            List<Transaction> monthTransactions = getTransactionsByMonth(year, month);
            double total = monthTransactions.stream()
                    .filter(t -> t.getType() == type)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
            result.put(month, total);
        }
        return result;
    }
    
    public int importTransactionsFromCsv(String csvFilePath) throws DataAccessException {
        return transactionDao.importTransactionsFromCsv(csvFilePath);
    }
    
    public int importTransactionsFromJson(String jsonFilePath) throws DataAccessException {
        return transactionDao.importTransactionsFromJson(jsonFilePath);
    }

    /**
     * Get transactions within a date range
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of transactions within the date range
     * @throws DataAccessException if there is an error accessing the data
     */
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) throws DataAccessException {
        return transactionDao.getTransactionsByDate(startDate, endDate);
    }
} 