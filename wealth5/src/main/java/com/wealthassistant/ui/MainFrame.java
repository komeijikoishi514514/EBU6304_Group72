package com.wealthassistant.ui;

import com.wealthassistant.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DashboardPanel dashboardPanel;
    private ManualEntryPanel manualEntryPanel;
    private ImportTransactionsPanel importTransactionsPanel;
    private TransactionHistoryPanel transactionHistoryPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    // 定义主题颜色
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185); // 深蓝色
    public static final Color BACKGROUND_COLOR = new Color(240, 240, 240); // 浅灰色背景
    public static final Color SIDEBAR_COLOR = new Color(52, 73, 94); // 深灰蓝色
    public static final Color SIDEBAR_HOVER_COLOR = new Color(41, 128, 185); // 蓝色悬停效果
    public static final Color TEXT_COLOR = new Color(52, 73, 94); // 文本颜色
    public static final Color ACCENT_COLOR = new Color(231, 76, 60); // 强调色（红色）
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font NORMAL_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final int BORDER_RADIUS = 10;

    public static final String DASHBOARD = "DASHBOARD";
    public static final String MANUAL_ENTRY = "MANUAL_ENTRY";
    public static final String IMPORT_TRANSACTIONS = "IMPORT_TRANSACTIONS";
    public static final String TRANSACTION_HISTORY = "TRANSACTION_HISTORY";
    public static final String REPORTS = "REPORTS";
    public static final String SETTINGS = "SETTINGS";

    public MainFrame() {
        setTitle("Personal Wealth Assistant");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768); // 更大的初始窗口尺寸
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        
        try {
            // 尝试使用系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // 自定义UI元素
            UIManager.put("Panel.background", BACKGROUND_COLOR);
            UIManager.put("Button.font", NORMAL_FONT);
            UIManager.put("Label.font", NORMAL_FONT);
            UIManager.put("TextField.font", NORMAL_FONT);
            UIManager.put("ComboBox.font", NORMAL_FONT);
            UIManager.put("Table.font", NORMAL_FONT);
            UIManager.put("TableHeader.font", NORMAL_FONT);
            UIManager.put("TabbedPane.font", NORMAL_FONT);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        initComponents();
        setupLayout();
        
        setVisible(true);
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BACKGROUND_COLOR);
        
        dashboardPanel = new DashboardPanel(this);
        manualEntryPanel = new ManualEntryPanel(this);
        importTransactionsPanel = new ImportTransactionsPanel(this);
        transactionHistoryPanel = new TransactionHistoryPanel(this);
        reportsPanel = new ReportsPanel(this);
        settingsPanel = new SettingsPanel(this);
        
        // 为每个面板添加内边距
        addPanelWithPadding(DASHBOARD, dashboardPanel);
        addPanelWithPadding(MANUAL_ENTRY, manualEntryPanel);
        addPanelWithPadding(IMPORT_TRANSACTIONS, importTransactionsPanel);
        addPanelWithPadding(TRANSACTION_HISTORY, transactionHistoryPanel);
        addPanelWithPadding(REPORTS, reportsPanel);
        addPanelWithPadding(SETTINGS, settingsPanel);
    }
    
    private void addPanelWithPadding(String name, JPanel panel) {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(BACKGROUND_COLOR);
        containerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        containerPanel.add(panel, BorderLayout.CENTER);
        cardPanel.add(containerPanel, name);
    }
    
    private void setupLayout() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(BACKGROUND_COLOR);
        
        // 创建侧边栏导航
        JPanel sidebarPanel = createSidebarPanel();
        
        // 添加侧边栏分隔线
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setForeground(new Color(200, 200, 200));
        
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(sidebarPanel, BorderLayout.CENTER);
        westPanel.add(separator, BorderLayout.EAST);
        
        contentPane.add(westPanel, BorderLayout.WEST);
        contentPane.add(cardPanel, BorderLayout.CENTER);
        
        setContentPane(contentPane);
    }
    
    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        
        // 应用标题
        JLabel titleLabel = new JLabel("Personal Wealth Assistant");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));
        
        sidebarPanel.add(titleLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // 添加导航按钮
        addNavButton(sidebarPanel, "Dashboard", DASHBOARD);
        addNavButton(sidebarPanel, "Manual Entry", MANUAL_ENTRY);
        addNavButton(sidebarPanel, "Import Transactions", IMPORT_TRANSACTIONS);
        addNavButton(sidebarPanel, "Transaction History", TRANSACTION_HISTORY);
        addNavButton(sidebarPanel, "Reports", REPORTS);
        addNavButton(sidebarPanel, "Settings", SETTINGS);
        
        sidebarPanel.add(Box.createVerticalGlue());
        
        // 添加版本信息
        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setForeground(new Color(200, 200, 200));
        versionLabel.setFont(SMALL_FONT);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        sidebarPanel.add(versionLabel);
        
        return sidebarPanel;
    }
    
    private void addNavButton(JPanel panel, String text, String cardName) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_COLOR);
        button.setFont(NORMAL_FONT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 50));
        
        // 设置圆角按钮样式
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.putClientProperty("JButton.focusWidth", 0);
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            private final Color originalBackground = button.getBackground();
            private final Color hoverBackground = SIDEBAR_HOVER_COLOR;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBackground);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 显示手型光标
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalBackground);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        button.addActionListener(e -> {
            cardLayout.show(cardPanel, cardName);
            
            // 当切换到仪表盘或交易历史面板时，刷新数据
            if (cardName.equals(DASHBOARD)) {
                refreshDashboard();
            } else if (cardName.equals(TRANSACTION_HISTORY)) {
                refreshTransactionHistory();
            }
        });
        
        panel.add(button);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    public void navigateTo(String cardName) {
        cardLayout.show(cardPanel, cardName);
        
        // 当导航到仪表盘或交易历史面板时，刷新数据
        if (cardName.equals(DASHBOARD)) {
            refreshDashboard();
        } else if (cardName.equals(TRANSACTION_HISTORY)) {
            refreshTransactionHistory();
        }
    }

    /**
     * 获取手动录入面板实例
     * @return ManualEntryPanel实例
     */
    public ManualEntryPanel getManualEntryPanel() {
        return manualEntryPanel;
    }
    
    /**
     * 刷新仪表盘数据
     */
    public void refreshDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.updateDashboard();
        }
    }
    
    /**
     * 刷新交易历史数据
     */
    public void refreshTransactionHistory() {
        if (transactionHistoryPanel != null) {
            transactionHistoryPanel.loadTransactions();
        }
    }
    
    /**
     * 交易数据发生变化时调用此方法
     * 用于通知需要自动更新的面板刷新数据
     */
    public void notifyTransactionDataChanged() {
        refreshDashboard();
        refreshTransactionHistory();
    }
} 