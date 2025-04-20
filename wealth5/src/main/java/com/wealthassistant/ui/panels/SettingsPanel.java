package com.wealthassistant.ui.panels;

import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.model.AppSettings;
import com.wealthassistant.model.Budget;
import com.wealthassistant.model.Category;
import com.wealthassistant.service.BudgetService;
import com.wealthassistant.service.CategoryService;
import com.wealthassistant.service.SettingsService;
import com.wealthassistant.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class SettingsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final SettingsService settingsService;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    
    // 类别管理组件
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;
    private JTextField categoryNameField;
    private JComboBox<String> categoryTypeComboBox;
    private JButton addCategoryButton;
    private JButton editCategoryButton;
    private JButton deleteCategoryButton;
    
    // 预算管理组件
    private JTable budgetTable;
    private DefaultTableModel budgetTableModel;
    private JComboBox<String> budgetCategoryComboBox;
    private JTextField monthlyLimitField;
    private JTextField annualLimitField;
    private JButton saveBudgetButton;
    private JButton deleteBudgetButton;
    
    // 应用设置组件
    private JComboBox<String> currencyComboBox;
    private JComboBox<String> dateFormatComboBox;
    private JComboBox<String> fileFormatComboBox;
    private JTextField transactionsPathField;
    private JTextField budgetsPathField;
    private JTextField categoriesPathField;
    private JCheckBox enableAutoAssignmentCheckBox;
    
    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.settingsService = new SettingsService();
        this.categoryService = new CategoryService();
        this.budgetService = new BudgetService();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadSettings();
    }
    
    private void initComponents() {
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 类别管理面板
        JPanel categoriesPanel = createCategoriesPanel();
        tabbedPane.addTab("Category Management", categoriesPanel);
        
        // 预算管理面板
        JPanel budgetsPanel = createBudgetsPanel();
        tabbedPane.addTab("Budget Management", budgetsPanel);
        
        // 应用设置面板
        JPanel appSettingsPanel = createAppSettingsPanel();
        tabbedPane.addTab("Application Settings", appSettingsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save All Settings");
        saveButton.addActionListener(e -> saveAllSettings());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> mainFrame.navigateTo(MainFrame.DASHBOARD));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createCategoriesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 类别表格
        String[] categoryColumns = {"Name", "Type"};
        categoryTableModel = new DefaultTableModel(categoryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止直接编辑表格
            }
        };
        
        categoryTable = new JTable(categoryTableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && categoryTable.getSelectedRow() != -1) {
                    int selectedRow = categoryTable.getSelectedRow();
                    String categoryName = (String) categoryTableModel.getValueAt(selectedRow, 0);
                    String categoryType = (String) categoryTableModel.getValueAt(selectedRow, 1);
                    
                    categoryNameField.setText(categoryName);
                    categoryTypeComboBox.setSelectedItem(categoryType);
                    
                    editCategoryButton.setEnabled(true);
                    deleteCategoryButton.setEnabled(true);
                } else {
                    editCategoryButton.setEnabled(false);
                    deleteCategoryButton.setEnabled(false);
                }
            }
        });
        
        JScrollPane categoryScrollPane = new JScrollPane(categoryTable);
        panel.add(categoryScrollPane, BorderLayout.CENTER);
        
        // 类别编辑面板
        JPanel categoryEditPanel = new JPanel(new GridBagLayout());
        categoryEditPanel.setBorder(new TitledBorder("Add/Edit Category"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 类别名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        categoryEditPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        categoryNameField = new JTextField(20);
        categoryEditPanel.add(categoryNameField, gbc);
        
        // 类别类型
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        categoryEditPanel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryTypeComboBox = new JComboBox<>(new String[]{"Income", "Expense", "Both"});
        categoryEditPanel.add(categoryTypeComboBox, gbc);
        
        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel categoryButtonPanel = new JPanel(new FlowLayout());
        
        addCategoryButton = new JButton("Add");
        addCategoryButton.addActionListener(e -> addCategory());
        
        editCategoryButton = new JButton("Update");
        editCategoryButton.addActionListener(e -> updateCategory());
        editCategoryButton.setEnabled(false);
        
        deleteCategoryButton = new JButton("Delete");
        deleteCategoryButton.addActionListener(e -> deleteCategory());
        deleteCategoryButton.setEnabled(false);
        
        JButton clearButton = new JButton("Clear Input");
        clearButton.addActionListener(e -> {
            categoryNameField.setText("");
            categoryTypeComboBox.setSelectedIndex(0);
            categoryTable.clearSelection();
        });
        
        categoryButtonPanel.add(addCategoryButton);
        categoryButtonPanel.add(editCategoryButton);
        categoryButtonPanel.add(deleteCategoryButton);
        categoryButtonPanel.add(clearButton);
        
        categoryEditPanel.add(categoryButtonPanel, gbc);
        
        panel.add(categoryEditPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBudgetsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 预算表格
        String[] budgetColumns = {"Category", "Monthly Limit", "Annual Limit"};
        budgetTableModel = new DefaultTableModel(budgetColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 禁止直接编辑表格
            }
        };
        
        budgetTable = new JTable(budgetTableModel);
        budgetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        budgetTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && budgetTable.getSelectedRow() != -1) {
                    int selectedRow = budgetTable.getSelectedRow();
                    String category = (String) budgetTableModel.getValueAt(selectedRow, 0);
                    String monthlyLimit = budgetTableModel.getValueAt(selectedRow, 1).toString();
                    String annualLimit = budgetTableModel.getValueAt(selectedRow, 2).toString();
                    
                    budgetCategoryComboBox.setSelectedItem(category);
                    monthlyLimitField.setText(monthlyLimit);
                    annualLimitField.setText(annualLimit);
                    
                    deleteBudgetButton.setEnabled(true);
                } else {
                    deleteBudgetButton.setEnabled(false);
                }
            }
        });
        
        JScrollPane budgetScrollPane = new JScrollPane(budgetTable);
        panel.add(budgetScrollPane, BorderLayout.CENTER);
        
        // 预算编辑面板
        JPanel budgetEditPanel = new JPanel(new GridBagLayout());
        budgetEditPanel.setBorder(new TitledBorder("Set Budget"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 类别选择
        gbc.gridx = 0;
        gbc.gridy = 0;
        budgetEditPanel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        budgetCategoryComboBox = new JComboBox<>();
        budgetEditPanel.add(budgetCategoryComboBox, gbc);
        
        // 月度限额
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        budgetEditPanel.add(new JLabel("Monthly Limit:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        monthlyLimitField = new JTextField(10);
        budgetEditPanel.add(monthlyLimitField, gbc);
        
        // 年度限额
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        budgetEditPanel.add(new JLabel("Annual Limit:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        annualLimitField = new JTextField(10);
        budgetEditPanel.add(annualLimitField, gbc);
        
        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel budgetButtonPanel = new JPanel(new FlowLayout());
        
        saveBudgetButton = new JButton("Save Budget");
        saveBudgetButton.addActionListener(e -> saveBudget());
        
        deleteBudgetButton = new JButton("Delete Budget");
        deleteBudgetButton.addActionListener(e -> deleteBudget());
        deleteBudgetButton.setEnabled(false);
        
        JButton clearBudgetButton = new JButton("Clear Input");
        clearBudgetButton.addActionListener(e -> {
            if (budgetCategoryComboBox.getItemCount() > 0) {
                budgetCategoryComboBox.setSelectedIndex(0);
            }
            monthlyLimitField.setText("");
            annualLimitField.setText("");
            budgetTable.clearSelection();
        });
        
        JButton initDefaultBudgetsButton = new JButton("Initialize Default Budgets");
        initDefaultBudgetsButton.addActionListener(e -> initDefaultBudgets());
        
        budgetButtonPanel.add(saveBudgetButton);
        budgetButtonPanel.add(deleteBudgetButton);
        budgetButtonPanel.add(clearBudgetButton);
        budgetButtonPanel.add(initDefaultBudgetsButton);
        
        budgetEditPanel.add(budgetButtonPanel, gbc);
        
        panel.add(budgetEditPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAppSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 货币设置
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("Default Currency:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        currencyComboBox = new JComboBox<>(new String[]{"CNY", "USD", "EUR", "JPY", "GBP"});
        settingsPanel.add(currencyComboBox, gbc);
        
        // 日期格式
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Date Format:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateFormatComboBox = new JComboBox<>(new String[]{
                "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd"
        });
        settingsPanel.add(dateFormatComboBox, gbc);
        
        // 文件格式
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("File Format:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fileFormatComboBox = new JComboBox<>(new String[]{"JSON", "CSV"});
        settingsPanel.add(fileFormatComboBox, gbc);
        
        // 交易文件路径
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Transactions File Path:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        transactionsPathField = new JTextField(30);
        settingsPanel.add(transactionsPathField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseTransactionsButton = new JButton("Browse...");
        browseTransactionsButton.addActionListener(e -> browseFile(transactionsPathField));
        settingsPanel.add(browseTransactionsButton, gbc);
        
        // 预算文件路径
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Budget File Path:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        budgetsPathField = new JTextField(30);
        settingsPanel.add(budgetsPathField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseBudgetsButton = new JButton("Browse...");
        browseBudgetsButton.addActionListener(e -> browseFile(budgetsPathField));
        settingsPanel.add(browseBudgetsButton, gbc);
        
        // 类别文件路径
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Categories File Path:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoriesPathField = new JTextField(30);
        settingsPanel.add(categoriesPathField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseCategoriesButton = new JButton("Browse...");
        browseCategoriesButton.addActionListener(e -> browseFile(categoriesPathField));
        settingsPanel.add(browseCategoriesButton, gbc);
        
        // 自动分配类别
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        enableAutoAssignmentCheckBox = new JCheckBox("Enable Auto Category Assignment");
        settingsPanel.add(enableAutoAssignmentCheckBox, gbc);
        
        // 添加清除所有数据的按钮
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton clearDataButton = new JButton("Clear All Data");
        clearDataButton.setForeground(Color.RED);
        clearDataButton.addActionListener(e -> clearAllData());
        settingsPanel.add(clearDataButton, gbc);
        
        panel.add(settingsPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private void loadSettings() {
        // 加载类别
        loadCategories();
        
        // 加载预算
        loadBudgets();
        
        // 加载应用设置
        AppSettings settings = settingsService.getSettings();
        
        currencyComboBox.setSelectedItem(settings.getDefaultCurrency());
        dateFormatComboBox.setSelectedItem(settings.getDateFormat());
        fileFormatComboBox.setSelectedItem(settings.getFileFormat());
        transactionsPathField.setText(settings.getTransactionsFilePath());
        budgetsPathField.setText(settings.getBudgetsFilePath());
        categoriesPathField.setText(settings.getCategoriesFilePath());
        enableAutoAssignmentCheckBox.setSelected(settings.isEnableAutoAssignment());
    }
    
    private void loadCategories() {
        try {
            // 清空表格
            categoryTableModel.setRowCount(0);
            
            // 加载类别
            List<Category> categories = categoryService.getAllCategories();
            
            for (Category category : categories) {
                String typeText;
                
                switch (category.getType()) {
                    case INCOME:
                        typeText = "Income";
                        break;
                    case EXPENSE:
                        typeText = "Expense";
                        break;
                    case BOTH:
                        typeText = "Both";
                        break;
                    default:
                        typeText = "";
                }
                
                categoryTableModel.addRow(new Object[]{category.getName(), typeText});
            }
            
            // 确保表格使用正确的字体
            categoryTable.setFont(MainFrame.NORMAL_FONT);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Load category failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadBudgets() {
        try {
            // 清空表格
            budgetTableModel.setRowCount(0);
            
            // 清空类别下拉框
            budgetCategoryComboBox.removeAllItems();
            
            // 加载支出类别
            List<String> expenseCategories = categoryService.getCategoryNamesByType(Category.CategoryType.EXPENSE);
            for (String category : expenseCategories) {
                budgetCategoryComboBox.addItem(category);
            }
            
            // 确保下拉框使用正确的字体
            budgetCategoryComboBox.setFont(MainFrame.NORMAL_FONT);
            
            // 加载预算
            List<Budget> budgets = budgetService.getAllBudgets();
            
            for (Budget budget : budgets) {
                budgetTableModel.addRow(new Object[]{
                        budget.getCategory(),
                        String.format("%.2f", budget.getMonthlyLimit()),
                        String.format("%.2f", budget.getAnnualLimit())
                });
            }
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Load budget failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addCategory() {
        String name = categoryNameField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please entering category name", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String typeText = (String) categoryTypeComboBox.getSelectedItem();
            Category.CategoryType type;
            
            switch (typeText) {
                case "Income":
                    type = Category.CategoryType.INCOME;
                    break;
                case "Expense":
                    type = Category.CategoryType.EXPENSE;
                    break;
                case "Both":
                    type = Category.CategoryType.BOTH;
                    break;
                default:
                    type = Category.CategoryType.EXPENSE;
            }
            
            Category category = new Category(name, type);
            categoryService.saveCategory(category);
            
            // 重新加载数据
            loadCategories();
            loadBudgets(); // 重新加载预算类别下拉框
            
            // 清空输入
            categoryNameField.setText("");
            
            JOptionPane.showMessageDialog(this, 
                    "Category added successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Add category failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        String oldName = (String) categoryTableModel.getValueAt(selectedRow, 0);
        String name = categoryNameField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please entering category name", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String typeText = (String) categoryTypeComboBox.getSelectedItem();
            Category.CategoryType type;
            
            switch (typeText) {
                case "Income":
                    type = Category.CategoryType.INCOME;
                    break;
                case "Expense":
                    type = Category.CategoryType.EXPENSE;
                    break;
                case "Both":
                    type = Category.CategoryType.BOTH;
                    break;
                default:
                    type = Category.CategoryType.EXPENSE;
            }
            
            Category category = new Category(name, type);
            categoryService.updateCategory(oldName, category);
            
            // 重新加载数据
            loadCategories();
            loadBudgets(); // 重新加载预算表和预算类别下拉框
            
            // 清空输入
            categoryNameField.setText("");
            categoryTable.clearSelection();
            
            JOptionPane.showMessageDialog(this, 
                    "Category updated successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Update category failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        String name = (String) categoryTableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete category '" + name + "'?\nThis will also delete the budget settings for this category.", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            categoryService.deleteCategory(name);
            
            // 删除该类别的预算
            try {
                budgetService.deleteBudget(name);
            } catch (DataAccessException e) {
                // 忽略预算不存在的错误
            }
            
            // 重新加载数据
            loadCategories();
            loadBudgets(); // 重新加载预算表和预算类别下拉框
            
            // 清空输入
            categoryNameField.setText("");
            categoryTable.clearSelection();
            
            JOptionPane.showMessageDialog(this, 
                    "Category deleted successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Delete category failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveBudget() {
        if (budgetCategoryComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a category", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String category = (String) budgetCategoryComboBox.getSelectedItem();
        
        // 验证月度限额
        double monthlyLimit;
        try {
            monthlyLimit = Double.parseDouble(monthlyLimitField.getText().trim());
            if (monthlyLimit <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid monthly limit (a number greater than zero)", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 验证年度限额
        double annualLimit;
        try {
            annualLimit = Double.parseDouble(annualLimitField.getText().trim());
            if (annualLimit <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid annual limit (a number greater than zero)", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Budget budget = new Budget(category, monthlyLimit, annualLimit, settingsService.getDefaultCurrency());
            budgetService.saveBudget(budget);
            
            // 重新加载预算
            loadBudgets();
            
            // 清空输入
            if (budgetCategoryComboBox.getItemCount() > 0) {
                budgetCategoryComboBox.setSelectedIndex(0);
            }
            monthlyLimitField.setText("");
            annualLimitField.setText("");
            budgetTable.clearSelection();
            
            JOptionPane.showMessageDialog(this, 
                    "Budget saved successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Save budget failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteBudget() {
        int selectedRow = budgetTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        String category = (String) budgetTableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete the budget for category '" + category + "'?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            budgetService.deleteBudget(category);
            
            // 重新加载预算
            loadBudgets();
            
            // 清空输入
            if (budgetCategoryComboBox.getItemCount() > 0) {
                budgetCategoryComboBox.setSelectedIndex(0);
            }
            monthlyLimitField.setText("");
            annualLimitField.setText("");
            budgetTable.clearSelection();
            
            JOptionPane.showMessageDialog(this, 
                    "Budget deleted successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Delete budget failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initDefaultBudgets() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to initialize the default budgets for all expense categories?\nThis will overwrite existing budget settings.", 
                "Confirm Initialization", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            budgetService.initDefaultBudgets();
            
            // 重新加载预算
            loadBudgets();
            
            JOptionPane.showMessageDialog(this, 
                    "Default budgets initialized successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Initialize default budgets failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void browseFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select a file");
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    private void saveAllSettings() {
        // 保存应用设置
        try {
            settingsService.setDefaultCurrency((String) currencyComboBox.getSelectedItem());
            settingsService.setDateFormat((String) dateFormatComboBox.getSelectedItem());
            settingsService.setFileFormat((String) fileFormatComboBox.getSelectedItem());
            settingsService.setTransactionsFilePath(transactionsPathField.getText());
            settingsService.setBudgetsFilePath(budgetsPathField.getText());
            settingsService.setCategoriesFilePath(categoriesPathField.getText());
            settingsService.setEnableAutoAssignment(enableAutoAssignmentCheckBox.isSelected());
            
            settingsService.saveSettings();
            
            JOptionPane.showMessageDialog(this, 
                    "Settings saved successfully", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            mainFrame.navigateTo(MainFrame.DASHBOARD);
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Save settings failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 清除所有数据
     */
    private void clearAllData() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all data?\nThis will delete all transactions, budgets, and custom categories.\nThis action is irreversible!",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 二次确认
        String confirmText = JOptionPane.showInputDialog(this,
                "Please enter \"confirm delete\" to continue:",
                "Confirmation",
                JOptionPane.WARNING_MESSAGE);
                
        if (confirmText == null || !confirmText.equals("confirm delete")) {
            return;
        }
        
        try {
            settingsService.clearAllData();
            
            // 通知MainFrame更新数据
            mainFrame.notifyTransactionDataChanged();
            
            // 重新加载设置面板的数据
            loadCategories();
            loadBudgets();
            
            JOptionPane.showMessageDialog(this,
                    "All data has been cleared! The application will return to the initial state.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                    
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this,
                    "Clear data failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 