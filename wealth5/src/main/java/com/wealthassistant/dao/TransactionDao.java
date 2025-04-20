package com.wealthassistant.dao;

import com.wealthassistant.model.AppSettings;
import com.wealthassistant.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TransactionDao {
    private static final String DATA_DIR = "data";
    private final AppSettings settings = AppSettings.getInstance();
    private final ObjectMapper mapper;
    
    public TransactionDao() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 确保数据目录存在
        new File(DATA_DIR).mkdirs();
    }
    
    public List<Transaction> getAllTransactions() throws DataAccessException {
        try {
            File file = new File(settings.getTransactionsFilePath());
            if (!file.exists()) {
                return new ArrayList<>();
            }
            
            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Transaction.class);
            return mapper.readValue(file, listType);
        } catch (IOException e) {
            throw new DataAccessException("读取交易数据失败", e);
        }
    }
    
    public void saveTransaction(Transaction transaction) throws DataAccessException {
        List<Transaction> transactions = getAllTransactions();
        transactions.add(transaction);
        saveAllTransactions(transactions);
    }
    
    public void updateTransaction(Transaction transaction) throws DataAccessException {
        List<Transaction> transactions = getAllTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(transaction.getId())) {
                transactions.set(i, transaction);
                break;
            }
        }
        saveAllTransactions(transactions);
    }
    
    public void deleteTransaction(String id) throws DataAccessException {
        List<Transaction> transactions = getAllTransactions();
        transactions.removeIf(t -> t.getId().equals(id));
        saveAllTransactions(transactions);
    }
    
    public void saveAllTransactions(List<Transaction> transactions) throws DataAccessException {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(settings.getTransactionsFilePath()), transactions);
        } catch (IOException e) {
            throw new DataAccessException("保存交易数据失败", e);
        }
    }
    
    public List<Transaction> getTransactionsByDate(LocalDate startDate, LocalDate endDate) throws DataAccessException {
        List<Transaction> allTransactions = getAllTransactions();
        return allTransactions.stream()
                .filter(t -> !t.getDate().isBefore(startDate) && !t.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
    
    public List<Transaction> getTransactionsByCategory(String category) throws DataAccessException {
        List<Transaction> allTransactions = getAllTransactions();
        return allTransactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
    
    public List<Transaction> getTransactionsByType(Transaction.TransactionType type) throws DataAccessException {
        List<Transaction> allTransactions = getAllTransactions();
        return allTransactions.stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.toList());
    }
    
    public List<Transaction> searchTransactionsByNotes(String searchText) throws DataAccessException {
        List<Transaction> allTransactions = getAllTransactions();
        return allTransactions.stream()
                .filter(t -> t.getNotes() != null && t.getNotes().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    // 导入交易数据
    public int importTransactionsFromCsv(String csvFilePath) throws DataAccessException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
            List<Transaction> existingTransactions = getAllTransactions();
            List<Transaction> newTransactions = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            // 跳过标题行
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length < 5) {
                    continue; // 跳过格式不正确的行
                }
                
                try {
                    Transaction transaction = new Transaction();
                    transaction.setAmount(Double.parseDouble(parts[0]));
                    transaction.setCurrencyUnit(parts[1]);
                    transaction.setType("INCOME".equalsIgnoreCase(parts[2]) ? 
                            Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE);
                    transaction.setCategory(parts[3]);
                    transaction.setDate(LocalDate.parse(parts[4], formatter));
                    
                    if (parts.length > 5) {
                        transaction.setNotes(parts[5]);
                    }
                    
                    newTransactions.add(transaction);
                } catch (Exception e) {
                    // 记录错误，但继续处理其他行
                    System.err.println("导入第" + i + "行时出错: " + e.getMessage());
                }
            }
            
            existingTransactions.addAll(newTransactions);
            saveAllTransactions(existingTransactions);
            
            return newTransactions.size();
        } catch (IOException e) {
            throw new DataAccessException("导入CSV文件失败", e);
        }
    }
    
    public int importTransactionsFromJson(String jsonFilePath) throws DataAccessException {
        try {
            File file = new File(jsonFilePath);
            if (!file.exists()) {
                throw new DataAccessException("文件不存在: " + jsonFilePath);
            }
            
            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, Transaction.class);
            List<Transaction> importedTransactions = mapper.readValue(file, listType);
            
            List<Transaction> existingTransactions = getAllTransactions();
            existingTransactions.addAll(importedTransactions);
            saveAllTransactions(existingTransactions);
            
            return importedTransactions.size();
        } catch (IOException e) {
            throw new DataAccessException("导入JSON文件失败", e);
        }
    }
} 