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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionHistoryPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final AppSettings settings;
    
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    
    private JComboBox<String> categoryFilterComboBox;
    private JTextField searchField;
    private JDateChooser fromDateChooser;
    private JDateChooser toDateChooser;
    
    private List<Transaction> allTransactions;
    private List<Transaction> filteredTransactions;
    
    public TransactionHistoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = new TransactionService();
        this.categoryService = new CategoryService();
        this.settings = AppSettings.getInstance();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }
    
    private void initComponents() {
        // 创建过滤面板
        JPanel filterPanel = createFilterPanel();
        
        // 创建表格
        createTransactionTable();
        JScrollPane tableScrollPane = new JScrollPane(transactionsTable);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 布局
        add(filterPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载交易数据
        loadTransactions();
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JPanel filtersPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 日期范围过滤
        gbc.gridx = 0;
        gbc.gridy = 0;
        filtersPanel.add(new JLabel("From:"), gbc);
        
        gbc.gridx = 1;
        fromDateChooser = new JDateChooser();
        fromDateChooser.setDate(new Date()); // 默认为今天
        filtersPanel.add(fromDateChooser, gbc);
        
        gbc.gridx = 2;
        filtersPanel.add(new JLabel("To:"), gbc);
        
        gbc.gridx = 3;
        toDateChooser = new JDateChooser();
        toDateChooser.setDate(new Date()); // 默认为今天
        filtersPanel.add(toDateChooser, gbc);
        
        // 类别过滤
        gbc.gridx = 0;
        gbc.gridy = 1;
        filtersPanel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        categoryFilterComboBox = new JComboBox<>();
        categoryFilterComboBox.addItem("All");
        fillCategoryComboBox();
        filtersPanel.add(categoryFilterComboBox, gbc);
        
        // 搜索框
        gbc.gridx = 2;
        filtersPanel.add(new JLabel("Search:"), gbc);
        
        gbc.gridx = 3;
        searchField = new JTextField(20);
        filtersPanel.add(searchField, gbc);
        
        // 应用过滤按钮
        JButton applyFilterButton = new JButton("Apply Filter");
        applyFilterButton.addActionListener(e -> applyFilters());
        
        // 重置过滤按钮
        JButton resetFilterButton = new JButton("Reset Filter");
        resetFilterButton.addActionListener(e -> resetFilters());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(applyFilterButton);
        buttonPanel.add(resetFilterButton);
        
        panel.add(filtersPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void fillCategoryComboBox() {
        try {
            List<String> categories = categoryService.getCategoryNames();
            for (String category : categories) {
                categoryFilterComboBox.addItem(category);
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
    
    private void createTransactionTable() {
        // 创建表格模型
        String[] columnNames = {"ID", "Date", "Type", "Category", "Amount", "Currency", "Notes", "Actions"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // 只有"操作"列可编辑
            }
        };
        
        transactionsTable = new JTable(tableModel);
        transactionsTable.getColumnModel().getColumn(0).setMinWidth(0);
        transactionsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        transactionsTable.getColumnModel().getColumn(0).setWidth(0);
        
        // 设置排序器
        sorter = new TableRowSorter<>(tableModel);
        transactionsTable.setRowSorter(sorter);
        
        // 设置操作列的渲染器和编辑器
        transactionsTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        transactionsTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox(), this));
        
        // 添加鼠标点击事件处理器，确保单元格编辑器正常工作
        transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = transactionsTable.rowAtPoint(e.getPoint());
                int col = transactionsTable.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 7) {
                    transactionsTable.editCellAt(row, col);
                }
            }
        });
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton exportButton = new JButton("Export to CSV");
        exportButton.addActionListener(e -> exportToCSV());
        
        panel.add(exportButton);
        return panel;
    }
    
    public void loadTransactions() {
        try {
            allTransactions = transactionService.getAllTransactions();
            filteredTransactions = new ArrayList<>(allTransactions);
            updateTransactionTable(filteredTransactions);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Failed to load transaction data: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTransactionTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        
        for (Transaction transaction : transactions) {
            Object[] rowData = {
                transaction.getId(),
                transaction.getDate().format(DateTimeFormatter.ofPattern(settings.getDateFormat())),
                transaction.getType() == Transaction.TransactionType.INCOME ? "Income" : "Expense",
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getCurrencyUnit(),
                transaction.getNotes(),
                "Edit/Delete"
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void applyFilters() {
        try {
            // 获取过滤条件
            final LocalDate fromDate;
            if (fromDateChooser.getDate() != null) {
                fromDate = fromDateChooser.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else {
                fromDate = null;
            }
            
            final LocalDate toDate;
            if (toDateChooser.getDate() != null) {
                toDate = toDateChooser.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else {
                toDate = null;
            }
            
            final String selectedCategory = (String) categoryFilterComboBox.getSelectedItem();
            final String searchText = searchField.getText().trim().toLowerCase();
            
            // 应用过滤
            filteredTransactions = allTransactions.stream()
                    .filter(t -> {
                        boolean matches = true;
                        
                        // 日期过滤
                        if (fromDate != null && t.getDate().isBefore(fromDate)) {
                            matches = false;
                        }
                        
                        if (toDate != null && t.getDate().isAfter(toDate)) {
                            matches = false;
                        }
                        
                        // 类别过滤
                        if (selectedCategory != null && !selectedCategory.equals("All") 
                                && !t.getCategory().equals(selectedCategory)) {
                            matches = false;
                        }
                        
                        // 搜索文本过滤
                        if (!searchText.isEmpty()) {
                            boolean textMatches = false;
                            
                            if (t.getNotes() != null && t.getNotes().toLowerCase().contains(searchText)) {
                                textMatches = true;
                            }
                            
                            if (t.getCategory().toLowerCase().contains(searchText)) {
                                textMatches = true;
                            }
                            
                            if (!textMatches) {
                                matches = false;
                            }
                        }
                        
                        return matches;
                    })
                    .collect(Collectors.toList());
            
            updateTransactionTable(filteredTransactions);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Failed to apply filter: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetFilters() {
        fromDateChooser.setDate(null);
        toDateChooser.setDate(null);
        categoryFilterComboBox.setSelectedItem("All");
        searchField.setText("");
        
        filteredTransactions = new ArrayList<>(allTransactions);
        updateTransactionTable(filteredTransactions);
    }
    
    private void editTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            // 尝试获取正在编辑的行
            selectedRow = transactionsTable.getEditingRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction first", "Hint", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        // 获取选中的交易记录
        int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < filteredTransactions.size()) {
            Transaction selectedTransaction = filteredTransactions.get(modelRow);
            
            // 显示手动录入面板并设置为编辑模式
            mainFrame.navigateTo(MainFrame.MANUAL_ENTRY);
            
            // 将交易记录传递给手动录入面板
            ManualEntryPanel manualEntryPanel = mainFrame.getManualEntryPanel();
            manualEntryPanel.setTransactionForEdit(selectedTransaction);
        } else {
            JOptionPane.showMessageDialog(this, "Cannot find the selected transaction", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void deleteTransaction(int row) {
        try {
            if (row >= 0 && row < filteredTransactions.size()) {
                Transaction transaction = filteredTransactions.get(row);
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to delete this transaction?", 
                        "Confirm Deletion", 
                        JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        transactionService.deleteTransaction(transaction.getId());
                        
                        // 通知MainFrame更新仪表盘数据
                        mainFrame.notifyTransactionDataChanged();
                        
                        // 重新加载数据
                        loadTransactions();
                    } catch (DataAccessException e) {
                        JOptionPane.showMessageDialog(this, 
                                "Failed to delete transaction: " + e.getMessage(), 
                                "Error", 
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Cannot find the transaction to delete", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                    "Operation failed: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportToCSV() {
        if (filteredTransactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No data to export", 
                    "Warning", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try (FileWriter writer = new FileWriter(filePath)) {
                // 写入标题行 - 按照导入功能期望的顺序
                writer.write("Amount,Currency,Type,Category,Date,Notes\n");
                
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                // 写入数据行 - 按照导入功能期望的顺序
                for (Transaction transaction : filteredTransactions) {
                    StringBuilder line = new StringBuilder();
                    // 金额
                    line.append(String.format("%.2f", transaction.getAmount())).append(",");
                    // 货币
                    line.append(transaction.getCurrencyUnit()).append(",");
                    // 类型 (确保为大写的INCOME或EXPENSE)
                    line.append(transaction.getType().toString()).append(",");
                    // 类别
                    if (transaction.getCategory().contains(",")) {
                        line.append("\"").append(transaction.getCategory()).append("\"");
                    } else {
                        line.append(transaction.getCategory());
                    }
                    line.append(",");
                    // 日期 (确保格式为yyyy-MM-dd)
                    line.append(transaction.getDate().format(dateFormatter)).append(",");
                    
                    // 备注
                    String notes = transaction.getNotes();
                    if (notes != null && !notes.isEmpty()) {
                        // 处理备注中的逗号
                        if (notes.contains(",")) {
                            notes = "\"" + notes + "\"";
                        }
                        line.append(notes);
                    }
                    
                    line.append("\n");
                    writer.write(line.toString());
                }
                
                JOptionPane.showMessageDialog(this, 
                        "Data successfully exported to " + filePath, 
                        "Export Successful", 
                        JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Export failed: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // 按钮渲染器
    static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    // 按钮编辑器
    static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private final TransactionHistoryPanel panel;
        
        public ButtonEditor(JCheckBox checkBox, TransactionHistoryPanel panel) {
            super(checkBox);
            this.panel = panel;
            button = new JButton();
            button.setOpaque(true);
            // 修改按钮的事件监听器
            button.addActionListener(e -> {
                fireEditingStopped();
                processButtonClick();
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
        
        // 单独处理按钮点击事件
        private void processButtonClick() {
            try {
                // 获取模型行索引
                int selectedRow = panel.transactionsTable.getSelectedRow();
                if (selectedRow == -1) {
                    // 如果没有选中行，尝试使用当前行
                    selectedRow = panel.transactionsTable.getEditingRow();
                }
                
                if (selectedRow != -1) {
                    int modelRow = panel.transactionsTable.convertRowIndexToModel(selectedRow);
                    
                    // 直接执行编辑操作，而不是显示弹出菜单
                    // 因为弹出菜单在某些环境下可能不显示
                    int option = JOptionPane.showOptionDialog(
                        panel,
                        "Please select an action",
                        "Transaction Actions",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Edit", "Delete"},
                        "Edit"
                    );
                    
                    if (option == 0) {
                        panel.editTransaction();
                    } else if (option == 1) {
                        panel.deleteTransaction(modelRow);
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, 
                            "Please select a transaction first", 
                            "Hint", 
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(panel, 
                        "Operation failed: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        
        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
    
    // JDateChooser 类，简化日期选择
    static class JDateChooser extends JPanel {
        private final JTextField textField;
        private final JButton button;
        private Date date;
        
        public JDateChooser() {
            setLayout(new BorderLayout());
            
            textField = new JTextField(10);
            textField.setEditable(false);
            
            button = new JButton("...");
            button.addActionListener(e -> showDatePicker());
            
            add(textField, BorderLayout.CENTER);
            add(button, BorderLayout.EAST);
        }
        
        private void showDatePicker() {
            // 简化版，实际应用中应使用完整的日期选择器库
            String inputDate = JOptionPane.showInputDialog(this, 
                    "Please enter date (yyyy-MM-dd):", 
                    (date != null) ? new SimpleDateFormat("yyyy-MM-dd").format(date) : "");
            
            if (inputDate != null && !inputDate.trim().isEmpty()) {
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd").parse(inputDate);
                    textField.setText(new SimpleDateFormat("yyyy-MM-dd").format(date));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                            "Invalid date format", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        public Date getDate() {
            return date;
        }
        
        public void setDate(Date date) {
            this.date = date;
            if (date != null) {
                textField.setText(new SimpleDateFormat("yyyy-MM-dd").format(date));
            } else {
                textField.setText("");
            }
        }
    }
} 