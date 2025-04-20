package com.wealthassistant.ui.panels;

import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.service.TransactionService;
import com.wealthassistant.ui.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

public class ImportTransactionsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TransactionService transactionService;
    
    private JTextField filePathField;
    private JTextArea previewArea;
    private JTextArea errorArea;
    private JTabbedPane tabbedPane;
    private List<String> errors;
    private File selectedFile;
    private JPanel dropPanel;
    
    public ImportTransactionsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = new TransactionService();
        this.errors = new ArrayList<>();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }
    
    private void initComponents() {
        // 文件选择部分
        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePanel.add(filePathField, BorderLayout.CENTER);
        
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseFile());
        filePanel.add(browseButton, BorderLayout.EAST);
        
        // 拖放区域
        dropPanel = new JPanel();
        dropPanel.setLayout(new BoxLayout(dropPanel, BoxLayout.Y_AXIS));
        dropPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        dropPanel.setBackground(new Color(240, 240, 240));
        dropPanel.setPreferredSize(new Dimension(0, 100));
        
        JLabel dragDropLabel = new JLabel("Drag and drop files here or click Browse");
        dragDropLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dragDropLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dropPanel.add(Box.createVerticalGlue());
        dropPanel.add(dragDropLabel);
        dropPanel.add(Box.createVerticalGlue());
        
        // 实现文件拖放功能
        setupDragAndDrop();
        
        // 添加点击事件打开文件浏览器
        dropPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                browseFile();
            }
        });
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        
        // 预览面板
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        JScrollPane previewScrollPane = new JScrollPane(previewArea);
        tabbedPane.addTab("Preview", previewScrollPane);
        
        // 错误面板
        errorArea = new JTextArea();
        errorArea.setEditable(false);
        errorArea.setForeground(Color.RED);
        JScrollPane errorScrollPane = new JScrollPane(errorArea);
        tabbedPane.addTab("Errors", errorScrollPane);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton parseButton = new JButton("Parse File");
        parseButton.addActionListener(e -> parseFile());
        
        JButton importButton = new JButton("Import");
        importButton.addActionListener(e -> importFile());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> mainFrame.navigateTo(MainFrame.DASHBOARD));
        
        buttonPanel.add(parseButton);
        buttonPanel.add(importButton);
        buttonPanel.add(cancelButton);
        
        // 主面板布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(filePanel, BorderLayout.NORTH);
        mainPanel.add(dropPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Import File");
        
        // 设置文件过滤器
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        FileNameExtensionFilter jsonFilter = new FileNameExtensionFilter("JSON Files (*.json)", "json");
        fileChooser.addChoosableFileFilter(csvFilter);
        fileChooser.addChoosableFileFilter(jsonFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(csvFilter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            
            // 清空预览和错误
            previewArea.setText("");
            errorArea.setText("");
            errors.clear();
        }
    }
    
    private void parseFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a file first", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 清空预览和错误
        previewArea.setText("");
        errorArea.setText("");
        errors.clear();
        
        try {
            // 根据文件扩展名决定如何解析
            String fileName = selectedFile.getName().toLowerCase();
            if (fileName.endsWith(".csv")) {
                parseCsvFile();
            } else if (fileName.endsWith(".json")) {
                parseJsonFile();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Unsupported file format, please select a CSV or JSON file", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error parsing file: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void parseCsvFile() {
        try {
            // 读取 CSV 文件前几行进行预览
            java.nio.file.Path path = selectedFile.toPath();
            List<String> lines = java.nio.file.Files.readAllLines(path);
            
            StringBuilder preview = new StringBuilder();
            int previewLines = Math.min(10, lines.size());
            
            for (int i = 0; i < previewLines; i++) {
                preview.append(lines.get(i)).append("\n");
            }
            
            previewArea.setText(preview.toString());
            
            // 检查 CSV 格式
            if (lines.size() > 0) {
                String header = lines.get(0);
                String[] columns = header.split(",");
                
                // 检查必要的列是否存在
                if (columns.length < 5) {
                    errors.add("CSV file format error: at least 5 columns are required (amount, currency, type, category, date)");
                }
                
                // 检查数据行格式
                for (int i = 1; i < lines.size(); i++) {
                    String[] parts = lines.get(i).split(",");
                    if (parts.length < 5) {
                        errors.add("Line " + (i+1) + ": insufficient columns");
                        continue;
                    }
                    
                    // 检查金额是否为数字
                    try {
                        double amount = Double.parseDouble(parts[0]);
                        if (amount <= 0) {
                            errors.add("Line " + (i+1) + ": amount must be greater than zero");
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Line " + (i+1) + ": invalid amount format");
                    }
                    
                    // 检查类型是否为 INCOME 或 EXPENSE
                    if (!parts[2].equalsIgnoreCase("INCOME") && !parts[2].equalsIgnoreCase("EXPENSE")) {
                        errors.add("Line " + (i+1) + ": type must be INCOME or EXPENSE");
                    }
                    
                    // 类别不能为空
                    if (parts[3].trim().isEmpty()) {
                        errors.add("Line " + (i+1) + ": category cannot be empty");
                    }
                    
                    // 日期格式检查
                    try {
                        java.time.LocalDate.parse(parts[4]);
                    } catch (Exception e) {
                        errors.add("Line " + (i+1) + ": invalid date format, should be yyyy-MM-dd");
                    }
                }
            }
            
            // 显示错误（如果有）
            if (!errors.isEmpty()) {
                StringBuilder errorText = new StringBuilder();
                for (String error : errors) {
                    errorText.append(error).append("\n");
                }
                errorArea.setText(errorText.toString());
                tabbedPane.setSelectedIndex(1); // 切换到错误选项卡
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error parsing CSV file: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void parseJsonFile() {
        try {
            // 读取JSON文件内容进行预览
            java.nio.file.Path path = selectedFile.toPath();
            String content = new String(java.nio.file.Files.readAllBytes(path));
            
            // 只显示前1000个字符作为预览
            if (content.length() > 1000) {
                previewArea.setText(content.substring(0, 1000) + "...");
            } else {
                previewArea.setText(content);
            }
            
            // 尝试解析JSON以检查格式
            try {
                // 使用Jackson解析JSON（实际实现时需要添加）
                // 这里简单检查JSON格式
                if (!content.trim().startsWith("[") || !content.trim().endsWith("]")) {
                    errors.add("JSON format error: should be an array of transactions");
                }
            } catch (Exception e) {
                errors.add("JSON parsing error: " + e.getMessage());
            }
            
            // 显示错误（如果有）
            if (!errors.isEmpty()) {
                StringBuilder errorText = new StringBuilder();
                for (String error : errors) {
                    errorText.append(error).append("\n");
                }
                errorArea.setText(errorText.toString());
                tabbedPane.setSelectedIndex(1); // 切换到错误选项卡
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error parsing JSON file: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void importFile() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, 
                    "Please select a file first", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 如果有错误，询问用户是否继续
        if (!errors.isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this, 
                    "There are " + errors.size() + " errors in the imported file. Do you still want to import?\n" +
                    "(Records with errors will be skipped)", 
                    "Confirm Import", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        try {
            int importedCount = 0;
            String fileName = selectedFile.getName().toLowerCase();
            
            if (fileName.endsWith(".csv")) {
                importedCount = transactionService.importTransactionsFromCsv(selectedFile.getAbsolutePath());
            } else if (fileName.endsWith(".json")) {
                importedCount = transactionService.importTransactionsFromJson(selectedFile.getAbsolutePath());
            }
            
            // 通知MainFrame更新数据
            mainFrame.notifyTransactionDataChanged();
            
            JOptionPane.showMessageDialog(this, 
                    "Successfully imported " + importedCount + " transaction records", 
                    "Import Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            // 导入成功后返回仪表盘
            mainFrame.navigateTo(MainFrame.DASHBOARD);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error importing file: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 实现拖放功能
    private void setupDragAndDrop() {
        // 创建TransferHandler
        TransferHandler transferHandler = new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                // 检查是否支持文件拖放
                if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return false;
                }
                
                // 只接受COPY操作
                if (!support.isDrop()) {
                    return false;
                }
                
                support.setDropAction(TransferHandler.COPY);
                return true;
            }
            
            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                // 检查是否支持拖放
                if (!canImport(support)) {
                    return false;
                }
                
                // 获取拖放的文件
                Transferable transferable = support.getTransferable();
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    
                    if (files.size() > 0) {
                        File file = files.get(0);
                        // 检查文件扩展名
                        String fileName = file.getName().toLowerCase();
                        if (fileName.endsWith(".csv") || fileName.endsWith(".json")) {
                            // 设置文件并清空预览
                            selectedFile = file;
                            filePathField.setText(selectedFile.getAbsolutePath());
                            previewArea.setText("");
                            errorArea.setText("");
                            errors.clear();
                            
                            // 自动解析文件
                            parseFile();
                            return true;
                        } else {
                            JOptionPane.showMessageDialog(
                                ImportTransactionsPanel.this,
                                "Unsupported file format, please select a CSV or JSON file",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        };
        
        // 设置TransferHandler
        dropPanel.setTransferHandler(transferHandler);
        
        // 使用DropTarget添加视觉反馈和处理拖放
        DropTargetAdapter dropTargetAdapter = new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                // 检查是否支持文件拖放
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    // 当拖动进入时，高亮显示边框
                    dropPanel.setBorder(BorderFactory.createLineBorder(new Color(41, 128, 185), 3, true));
                    dropPanel.repaint();
                } else {
                    dtde.rejectDrag();
                }
            }
            
            @Override
            public void dragExit(java.awt.dnd.DropTargetEvent dte) {
                // 拖动离开时，恢复边框
                dropPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
                dropPanel.repaint();
            }
            
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    // 恢复边框
                    dropPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
                    dropPanel.repaint();
                    
                    // 接受拖放操作
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    
                    // 获取拖放的文件
                    Transferable transferable = dtde.getTransferable();
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    
                    if (files.size() > 0) {
                        File file = files.get(0);
                        String fileName = file.getName().toLowerCase();
                        if (fileName.endsWith(".csv") || fileName.endsWith(".json")) {
                            // 设置文件并清空预览
                            selectedFile = file;
                            filePathField.setText(selectedFile.getAbsolutePath());
                            previewArea.setText("");
                            errorArea.setText("");
                            errors.clear();
                            
                            // 自动解析文件
                            SwingUtilities.invokeLater(() -> parseFile());
                            dtde.dropComplete(true);
                        } else {
                            JOptionPane.showMessageDialog(
                                ImportTransactionsPanel.this,
                                "Unsupported file format, please select a CSV or JSON file",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                            dtde.dropComplete(false);
                        }
                    } else {
                        dtde.dropComplete(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dtde.dropComplete(false);
                    JOptionPane.showMessageDialog(
                        ImportTransactionsPanel.this,
                        "Error processing file drop: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        
        // 创建DropTarget - 不需要try-catch块
        new DropTarget(dropPanel, DnDConstants.ACTION_COPY, dropTargetAdapter);
    }
} 