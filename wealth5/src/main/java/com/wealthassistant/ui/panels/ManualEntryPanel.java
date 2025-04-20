package com.wealthassistant.ui.panels;

import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.model.AppSettings;
import com.wealthassistant.model.Category;
import com.wealthassistant.model.Transaction;
import com.wealthassistant.service.CategoryService;
import com.wealthassistant.service.TransactionService;
import com.wealthassistant.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ManualEntryPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final AppSettings settings;
    
    private JTextField amountField;
    private JComboBox<String> currencyComboBox;
    private JToggleButton incomeButton;
    private JToggleButton expenseButton;
    private JComboBox<String> categoryComboBox;
    private JTextField dateField;
    private JTextArea notesArea;
    
    private Transaction currentTransaction;
    private boolean isEditing = false;
    
    public ManualEntryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = new TransactionService();
        this.categoryService = new CategoryService();
        this.settings = AppSettings.getInstance();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 金额输入
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        amountField = new JTextField(15);
        formPanel.add(amountField, gbc);
        
        // 货币选择
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Currency:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        currencyComboBox = new JComboBox<>(new String[]{"CNY", "USD", "EUR", "JPY", "GBP"});
        currencyComboBox.setSelectedItem(settings.getDefaultCurrency());
        formPanel.add(currencyComboBox, gbc);
        
        // 交易类型选择
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        ButtonGroup typeGroup = new ButtonGroup();
        
        incomeButton = new JToggleButton("Income");
        expenseButton = new JToggleButton("Expense");
        
        incomeButton.addActionListener(e -> updateCategoryOptions(Category.CategoryType.INCOME));
        expenseButton.addActionListener(e -> updateCategoryOptions(Category.CategoryType.EXPENSE));
        
        typeGroup.add(incomeButton);
        typeGroup.add(expenseButton);
        
        typePanel.add(incomeButton);
        typePanel.add(expenseButton);
        
        formPanel.add(typePanel, gbc);
        
        // 默认选择支出
        expenseButton.setSelected(true);
        
        // 类别选择
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        categoryComboBox = new JComboBox<>();
        formPanel.add(categoryComboBox, gbc);
        
        // 初始加载支出类别
        updateCategoryOptions(Category.CategoryType.EXPENSE);
        
        JButton addCategoryButton = new JButton("Add New Category...");
        addCategoryButton.addActionListener(e -> showAddCategoryDialog());
        
        gbc.gridx = 2;
        formPanel.add(addCategoryButton, gbc);
        
        // 日期输入
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        dateField = new JTextField(10);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern(settings.getDateFormat())));
        formPanel.add(dateField, gbc);
        
        gbc.gridx = 2;
        JLabel dateFormatLabel = new JLabel("Format: " + settings.getDateFormat());
        dateFormatLabel.setFont(new Font(dateFormatLabel.getFont().getName(), Font.ITALIC, 12));
        formPanel.add(dateFormatLabel, gbc);
        
        // 备注输入
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        formPanel.add(new JLabel("Notes:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 2;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);
        formPanel.add(notesScrollPane, gbc);
        
        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveTransaction());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            resetForm();
            mainFrame.navigateTo(MainFrame.DASHBOARD);
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel, gbc);
        
        // 将表单添加到面板中心
        add(formPanel, BorderLayout.CENTER);
    }
    
    private void updateCategoryOptions(Category.CategoryType type) {
        categoryComboBox.removeAllItems();
        
        try {
            List<String> categories = categoryService.getCategoryNamesByType(type);
            for (String category : categories) {
                categoryComboBox.addItem(category);
            }
            
            if (categoryComboBox.getItemCount() > 0) {
                categoryComboBox.setSelectedIndex(0);
            }
            
            // 设置字体以确保正确显示
            categoryComboBox.setFont(MainFrame.NORMAL_FONT);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Failed to load categories: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAddCategoryDialog() {
        String categoryName = JOptionPane.showInputDialog(this, 
                "Enter new category name:", 
                "Add Category", 
                JOptionPane.PLAIN_MESSAGE);
        
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            try {
                Category.CategoryType type = incomeButton.isSelected() ? 
                        Category.CategoryType.INCOME : Category.CategoryType.EXPENSE;
                
                Category newCategory = new Category(categoryName, type);
                categoryService.saveCategory(newCategory);
                
                // 更新类别下拉框
                updateCategoryOptions(type);
                categoryComboBox.setSelectedItem(categoryName);
                
            } catch (DataAccessException e) {
                JOptionPane.showMessageDialog(this, 
                        "Failed to add category: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveTransaction() {
        // 验证表单
        if (!validateForm()) {
            return;
        }
        
        try {
            // 收集表单数据
            double amount = Double.parseDouble(amountField.getText());
            String currency = (String) currencyComboBox.getSelectedItem();
            Transaction.TransactionType type = incomeButton.isSelected() ? 
                    Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;
            String category = (String) categoryComboBox.getSelectedItem();
            LocalDate date = LocalDate.parse(dateField.getText(), 
                    DateTimeFormatter.ofPattern(settings.getDateFormat()));
            String notes = notesArea.getText();
            
            if (isEditing && currentTransaction != null) {
                // 更新现有交易
                currentTransaction.setAmount(amount);
                currentTransaction.setCurrencyUnit(currency);
                currentTransaction.setType(type);
                currentTransaction.setCategory(category);
                currentTransaction.setDate(date);
                currentTransaction.setNotes(notes);
                
                transactionService.updateTransaction(currentTransaction);
            } else {
                // 创建新交易
                Transaction transaction = new Transaction(amount, currency, type, category, date, notes);
                transactionService.saveTransaction(transaction);
            }
            
            // 通知MainFrame更新数据
            mainFrame.notifyTransactionDataChanged();
            
            // 重置表单并返回仪表盘
            resetForm();
            mainFrame.navigateTo(MainFrame.DASHBOARD);
            
            JOptionPane.showMessageDialog(this, 
                    "Transaction saved successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Failed to save transaction: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateForm() {
        // 验证金额
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, 
                        "Amount must be greater than zero", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid amount", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // 验证类别
        if (categoryComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a category", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // 验证日期
        try {
            LocalDate.parse(dateField.getText(), 
                    DateTimeFormatter.ofPattern(settings.getDateFormat()));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid date in the format " + settings.getDateFormat(), 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public void setTransactionForEdit(Transaction transaction) {
        this.currentTransaction = transaction;
        this.isEditing = true;
        
        // 填充表单
        amountField.setText(String.valueOf(transaction.getAmount()));
        currencyComboBox.setSelectedItem(transaction.getCurrencyUnit());
        
        if (transaction.getType() == Transaction.TransactionType.INCOME) {
            incomeButton.setSelected(true);
            updateCategoryOptions(Category.CategoryType.INCOME);
        } else {
            expenseButton.setSelected(true);
            updateCategoryOptions(Category.CategoryType.EXPENSE);
        }
        
        categoryComboBox.setSelectedItem(transaction.getCategory());
        dateField.setText(transaction.getDate().format(
                DateTimeFormatter.ofPattern(settings.getDateFormat())));
        notesArea.setText(transaction.getNotes());
    }
    
    public void resetForm() {
        amountField.setText("");
        currencyComboBox.setSelectedItem(settings.getDefaultCurrency());
        expenseButton.setSelected(true);
        updateCategoryOptions(Category.CategoryType.EXPENSE);
        dateField.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern(settings.getDateFormat())));
        notesArea.setText("");
        
        currentTransaction = null;
        isEditing = false;
    }
} 