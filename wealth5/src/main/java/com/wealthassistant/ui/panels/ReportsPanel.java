package com.wealthassistant.ui.panels;

import com.wealthassistant.dao.DataAccessException;
import com.wealthassistant.model.AppSettings;
import com.wealthassistant.model.Budget;
import com.wealthassistant.model.Transaction;
import com.wealthassistant.service.BudgetService;
import com.wealthassistant.service.TransactionService;
import com.wealthassistant.ui.MainFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

// 添加类级别的未检查操作抑制
@SuppressWarnings("unchecked")
public class ReportsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final AppSettings settings;
    
    private JComboBox<String> reportTypeComboBox;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<Month> monthComboBox;
    private JPanel chartsPanel;
    private JPanel summaryPanel;
    
    // 中文字体
    private static final Font CHINESE_FONT = new Font("Arial", Font.PLAIN, 12);
    
    // 静态初始化块，设置JFreeChart的默认主题
    static {
        // 创建自定义主题以支持中文
        StandardChartTheme chartTheme = new StandardChartTheme("EnglishTheme");
        
        // 设置图表标题字体
        chartTheme.setExtraLargeFont(new Font("Arial", Font.BOLD, 20));
        // 设置轴标签字体
        chartTheme.setLargeFont(new Font("Arial", Font.BOLD, 15));
        // 设置图例项目字体
        chartTheme.setRegularFont(new Font("Arial", Font.PLAIN, 12));
        
        // 应用主题
        ChartFactory.setChartTheme(chartTheme);
    }
    
    private enum ReportType {
        MONTHLY("Monthly Report"),
        QUARTERLY("Quarterly Report"),
        YEARLY("Yearly Report");
        
        private final String displayName;
        
        ReportType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public ReportsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.transactionService = new TransactionService();
        this.budgetService = new BudgetService();
        this.settings = AppSettings.getInstance();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(MainFrame.BACKGROUND_COLOR);
        
        initComponents();
    }
    
    private void initComponents() {
        // 顶部控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // 中间图表面板 - 使用单列布局以便充分利用空间
        chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        chartsPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        
        // 使用可滚动面板，允许图表完整显示
        JScrollPane chartsScrollPane = new JScrollPane(chartsPanel);
        chartsScrollPane.getVerticalScrollBar().setUnitIncrement(16); // 增加滚动速度
        
        // 底部汇总面板
        summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Financial Summary"));
        summaryPanel.setBackground(MainFrame.BACKGROUND_COLOR);
        JScrollPane summaryScrollPane = new JScrollPane(summaryPanel);
        
        // 创建分割面板，允许用户调整图表区域和汇总区域的大小
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartsScrollPane, summaryScrollPane);
        splitPane.setOneTouchExpandable(true); // 添加一键展开按钮
        splitPane.setResizeWeight(0.7); // 分配70%的空间给图表区域
        splitPane.setDividerLocation(500); // 默认分隔位置
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 报表类型选择
        panel.add(new JLabel("Report Type:"));
        reportTypeComboBox = new JComboBox<>(new String[]{
                ReportType.MONTHLY.toString(),
                ReportType.QUARTERLY.toString(),
                ReportType.YEARLY.toString()
        });
        panel.add(reportTypeComboBox);
        
        // 年份选择
        panel.add(new JLabel("Year:"));
        yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 5; year <= currentYear + 1; year++) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setSelectedItem(currentYear);
        panel.add(yearComboBox);
        
        // 月份选择（仅月度报表显示）
        panel.add(new JLabel("Month:"));
        monthComboBox = new JComboBox<>(Month.values());
        monthComboBox.setSelectedItem(LocalDate.now().getMonth());
        panel.add(monthComboBox);
        
        // 生成报表按钮
        JButton generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> generateReport());
        panel.add(generateButton);
        
        // 导出按钮
        JButton exportCsvButton = new JButton("Export to CSV");
        exportCsvButton.addActionListener(e -> exportToCSV());
        panel.add(exportCsvButton);
        
        // 根据报表类型调整控件可见性
        reportTypeComboBox.addActionListener(e -> {
            String selectedType = (String) reportTypeComboBox.getSelectedItem();
            monthComboBox.setVisible(selectedType.equals(ReportType.MONTHLY.toString()));
        });
        
        return panel;
    }
    
    private void generateReport() {
        try {
            // 清空图表和汇总面板
            chartsPanel.removeAll();
            summaryPanel.removeAll();
            
            String reportType = (String) reportTypeComboBox.getSelectedItem();
            int year = (int) yearComboBox.getSelectedItem();
            
            if (reportType.equals(ReportType.MONTHLY.toString())) {
                Month month = (Month) monthComboBox.getSelectedItem();
                generateMonthlyReport(year, month);
            } else if (reportType.equals(ReportType.QUARTERLY.toString())) {
                generateQuarterlyReport(year);
            } else if (reportType.equals(ReportType.YEARLY.toString())) {
                generateYearlyReport(year);
            }
            
            // 刷新面板
            chartsPanel.revalidate();
            chartsPanel.repaint();
            summaryPanel.revalidate();
            summaryPanel.repaint();
            
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error generating report: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateMonthlyReport(int year, Month month) throws DataAccessException {
        // 获取月度数据
        double totalIncome = transactionService.getTotalIncomeForMonth(year, month);
        double totalExpense = transactionService.getTotalExpenseForMonth(year, month);
        double netBalance = totalIncome - totalExpense;
        
        Map<String, Double> expensesByCategory = transactionService.getExpensesByCategory(year, month);
        Map<String, Double> incomeByCategory = transactionService.getIncomeByCategory(year, month);
        Map<String, BudgetService.BudgetStatus> budgetStatus = budgetService.checkBudgetStatus(year, month);
        
        // 创建饼图 - 支出分类
        if (!expensesByCategory.isEmpty()) {
            JFreeChart expensePieChart = createPieChart("Expense Categories", expensesByCategory);
            ChartPanel expensePieChartPanel = createResizableChartPanel(expensePieChart);
            expensePieChartPanel.setPreferredSize(new Dimension(800, 400));
            chartsPanel.add(expensePieChartPanel);
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20))); // 添加间距
        }
        
        // 创建饼图 - 收入分类
        if (!incomeByCategory.isEmpty()) {
            JFreeChart incomePieChart = createPieChart("Income Categories", incomeByCategory);
            ChartPanel incomePieChartPanel = createResizableChartPanel(incomePieChart);
            incomePieChartPanel.setPreferredSize(new Dimension(800, 400));
            chartsPanel.add(incomePieChartPanel);
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20))); // 添加间距
        }
        
        // 创建条形图 - 预算vs实际
        if (!budgetStatus.isEmpty()) {
            JFreeChart budgetBarChart = createBudgetBarChart("Budget vs Actual Spending", budgetStatus);
            ChartPanel budgetBarChartPanel = createResizableChartPanel(budgetBarChart);
            budgetBarChartPanel.setPreferredSize(new Dimension(800, 400));
            chartsPanel.add(budgetBarChartPanel);
        }
        
        // 创建汇总表格
        createSummaryTable(year, month, totalIncome, totalExpense, netBalance, budgetStatus);
    }
    
    private void generateQuarterlyReport(int year) throws DataAccessException {
        // 获取季度数据
        double q1Income = 0, q1Expense = 0;
        double q2Income = 0, q2Expense = 0;
        double q3Income = 0, q3Expense = 0;
        double q4Income = 0, q4Expense = 0;
        
        // 第一季度 (1-3月)
        for (Month month : new Month[]{Month.JANUARY, Month.FEBRUARY, Month.MARCH}) {
            q1Income += transactionService.getTotalIncomeForMonth(year, month);
            q1Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 第二季度 (4-6月)
        for (Month month : new Month[]{Month.APRIL, Month.MAY, Month.JUNE}) {
            q2Income += transactionService.getTotalIncomeForMonth(year, month);
            q2Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 第三季度 (7-9月)
        for (Month month : new Month[]{Month.JULY, Month.AUGUST, Month.SEPTEMBER}) {
            q3Income += transactionService.getTotalIncomeForMonth(year, month);
            q3Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 第四季度 (10-12月)
        for (Month month : new Month[]{Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER}) {
            q4Income += transactionService.getTotalIncomeForMonth(year, month);
            q4Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 创建季度收入支出趋势图
        JFreeChart quarterlyTrendChart = createQuarterlyTrendChart(year, 
                q1Income, q1Expense, 
                q2Income, q2Expense, 
                q3Income, q3Expense, 
                q4Income, q4Expense);
        
        ChartPanel quarterlyTrendChartPanel = createResizableChartPanel(quarterlyTrendChart);
        quarterlyTrendChartPanel.setPreferredSize(new Dimension(800, 500));
        chartsPanel.add(quarterlyTrendChartPanel);
        
        // 创建季度汇总表格
        createQuarterlySummaryTable(year, 
                q1Income, q1Expense, 
                q2Income, q2Expense, 
                q3Income, q3Expense, 
                q4Income, q4Expense);
    }
    
    private void generateYearlyReport(int year) throws DataAccessException {
        // 获取年度数据
        double totalIncome = transactionService.getTotalIncomeForYear(year);
        double totalExpense = transactionService.getTotalExpenseForYear(year);
        
        // 获取月度趋势数据
        Map<Month, Double> monthlyIncomes = transactionService.getMonthlyTotals(year, Transaction.TransactionType.INCOME);
        Map<Month, Double> monthlyExpenses = transactionService.getMonthlyTotals(year, Transaction.TransactionType.EXPENSE);
        
        // 创建月度趋势图
        JFreeChart monthlyTrendChart = createMonthlyTrendChart(year, monthlyIncomes, monthlyExpenses);
        ChartPanel monthlyTrendChartPanel = createResizableChartPanel(monthlyTrendChart);
        monthlyTrendChartPanel.setPreferredSize(new Dimension(800, 500));
        chartsPanel.add(monthlyTrendChartPanel);
        
        // 创建年度汇总表格
        createYearlySummaryTable(year, totalIncome, totalExpense, monthlyIncomes, monthlyExpenses);
    }
    
    private JFreeChart createPieChart(String title, Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true,  // 显示图例
                true,  // 显示工具提示
                false  // 不生成URL
        );
        
        // 应用额外的中文字体设置
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(CHINESE_FONT);
        
        return chart;
    }
    
    private JFreeChart createBudgetBarChart(String title, Map<String, BudgetService.BudgetStatus> budgetStatus) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map.Entry<String, BudgetService.BudgetStatus> entry : budgetStatus.entrySet()) {
            String category = entry.getKey();
            BudgetService.BudgetStatus status = entry.getValue();
            
            dataset.addValue(status.getSpent(), "Actual Spending", category);
            dataset.addValue(status.getLimit(), "Budget Limit", category);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                "Category",
                "Amount (" + settings.getDefaultCurrency() + ")",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // 应用额外的中文字体设置
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setTickLabelFont(CHINESE_FONT);
        plot.getRangeAxis().setTickLabelFont(CHINESE_FONT);
        
        return chart;
    }
    
    private JFreeChart createQuarterlyTrendChart(int year, 
            double q1Income, double q1Expense,
            double q2Income, double q2Expense,
            double q3Income, double q3Expense,
            double q4Income, double q4Expense) {
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        dataset.addValue(q1Income, "Income", "Q1");
        dataset.addValue(q2Income, "Income", "Q2");
        dataset.addValue(q3Income, "Income", "Q3");
        dataset.addValue(q4Income, "Income", "Q4");
        
        dataset.addValue(q1Expense, "Expense", "Q1");
        dataset.addValue(q2Expense, "Expense", "Q2");
        dataset.addValue(q3Expense, "Expense", "Q3");
        dataset.addValue(q4Expense, "Expense", "Q4");
        
        JFreeChart chart = ChartFactory.createLineChart(
                year + " Quarterly Income and Expense Trends",
                "Quarter",
                "Amount (" + settings.getDefaultCurrency() + ")",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // 应用额外的中文字体设置
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setTickLabelFont(CHINESE_FONT);
        plot.getRangeAxis().setTickLabelFont(CHINESE_FONT);
        
        return chart;
    }
    
    private JFreeChart createMonthlyTrendChart(int year, 
            Map<Month, Double> monthlyIncomes, 
            Map<Month, Double> monthlyExpenses) {
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Month month : Month.values()) {
            // 使用中文月份名称
            String monthLabel = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            
            double income = monthlyIncomes.getOrDefault(month, 0.0);
            double expense = monthlyExpenses.getOrDefault(month, 0.0);
            
            dataset.addValue(income, "Income", monthLabel);
            dataset.addValue(expense, "Expense", monthLabel);
        }
        
        JFreeChart chart = ChartFactory.createLineChart(
                year + " Monthly Income and Expense Trends",
                "Month",
                "Amount (" + settings.getDefaultCurrency() + ")",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // 应用额外的中文字体设置
        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setTickLabelFont(CHINESE_FONT);
        plot.getRangeAxis().setTickLabelFont(CHINESE_FONT);
        
        return chart;
    }
    
    private void createSummaryTable(int year, Month month, 
            double totalIncome, double totalExpense, double netBalance,
            Map<String, BudgetService.BudgetStatus> budgetStatus) {
        
        // 基本财务汇总
        JPanel basicSummaryPanel = new JPanel(new GridLayout(3, 2));
        basicSummaryPanel.add(new JLabel("Total Income:"));
        basicSummaryPanel.add(new JLabel(String.format("%s %.2f", settings.getDefaultCurrency(), totalIncome)));
        basicSummaryPanel.add(new JLabel("Total Expense:"));
        basicSummaryPanel.add(new JLabel(String.format("%s %.2f", settings.getDefaultCurrency(), totalExpense)));
        basicSummaryPanel.add(new JLabel("Net Balance:"));
        
        JLabel netBalanceLabel = new JLabel(String.format("%s %.2f", settings.getDefaultCurrency(), netBalance));
        netBalanceLabel.setForeground(netBalance >= 0 ? new Color(0, 128, 0) : Color.RED);
        basicSummaryPanel.add(netBalanceLabel);
        
        // 预算执行情况表格
        String[] columnNames = {"Category", "Budget Amount", "Actual Spending", "Remaining", "Usage Percentage", "Status"};
        Object[][] data = new Object[budgetStatus.size()][6];
        
        int i = 0;
        for (Map.Entry<String, BudgetService.BudgetStatus> entry : budgetStatus.entrySet()) {
            BudgetService.BudgetStatus status = entry.getValue();
            
            data[i][0] = entry.getKey();
            data[i][1] = String.format("%.2f", status.getLimit());
            data[i][2] = String.format("%.2f", status.getSpent());
            data[i][3] = String.format("%.2f", status.getRemaining());
            data[i][4] = String.format("%.1f%%", status.getPercentage());
            
            switch (status.getStatus()) {
                case NORMAL:
                    data[i][5] = "Normal";
                    break;
                case WARNING:
                    data[i][5] = "Near Limit";
                    break;
                case OVER_BUDGET:
                    data[i][5] = "Over Budget";
                    break;
            }
            
            i++;
        }
        
        JTable budgetTable = new JTable(data, columnNames);
        JScrollPane budgetScrollPane = new JScrollPane(budgetTable);
        
        // 将两部分添加到汇总面板
        summaryPanel.add(basicSummaryPanel, BorderLayout.NORTH);
        summaryPanel.add(budgetScrollPane, BorderLayout.CENTER);
    }
    
    private void createQuarterlySummaryTable(int year,
            double q1Income, double q1Expense,
            double q2Income, double q2Expense,
            double q3Income, double q3Expense,
            double q4Income, double q4Expense) {
        
        // 计算净余额
        double q1Balance = q1Income - q1Expense;
        double q2Balance = q2Income - q2Expense;
        double q3Balance = q3Income - q3Expense;
        double q4Balance = q4Income - q4Expense;
        double yearlyIncome = q1Income + q2Income + q3Income + q4Income;
        double yearlyExpense = q1Expense + q2Expense + q3Expense + q4Expense;
        double yearlyBalance = yearlyIncome - yearlyExpense;
        
        // 创建季度汇总表格
        String[] columnNames = {"Quarter", "Income", "Expense", "Net Balance"};
        Object[][] data = new Object[5][4];
        
        data[0][0] = "Q1";
        data[0][1] = String.format("%.2f", q1Income);
        data[0][2] = String.format("%.2f", q1Expense);
        data[0][3] = String.format("%.2f", q1Balance);
        
        data[1][0] = "Q2";
        data[1][1] = String.format("%.2f", q2Income);
        data[1][2] = String.format("%.2f", q2Expense);
        data[1][3] = String.format("%.2f", q2Balance);
        
        data[2][0] = "Q3";
        data[2][1] = String.format("%.2f", q3Income);
        data[2][2] = String.format("%.2f", q3Expense);
        data[2][3] = String.format("%.2f", q3Balance);
        
        data[3][0] = "Q4";
        data[3][1] = String.format("%.2f", q4Income);
        data[3][2] = String.format("%.2f", q4Expense);
        data[3][3] = String.format("%.2f", q4Balance);
        
        data[4][0] = "Yearly Total";
        data[4][1] = String.format("%.2f", yearlyIncome);
        data[4][2] = String.format("%.2f", yearlyExpense);
        data[4][3] = String.format("%.2f", yearlyBalance);
        
        JTable summaryTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(summaryTable);
        
        summaryPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createYearlySummaryTable(int year, 
            double totalIncome, double totalExpense,
            Map<Month, Double> monthlyIncomes, 
            Map<Month, Double> monthlyExpenses) {
        
        // 计算净余额
        double netBalance = totalIncome - totalExpense;
        
        // 基本财务汇总
        JPanel basicSummaryPanel = new JPanel(new GridLayout(3, 2));
        basicSummaryPanel.add(new JLabel("Annual Total Income:"));
        basicSummaryPanel.add(new JLabel(String.format("%s %.2f", settings.getDefaultCurrency(), totalIncome)));
        basicSummaryPanel.add(new JLabel("Annual Total Expense:"));
        basicSummaryPanel.add(new JLabel(String.format("%s %.2f", settings.getDefaultCurrency(), totalExpense)));
        basicSummaryPanel.add(new JLabel("Annual Net Balance:"));
        
        JLabel netBalanceLabel = new JLabel(String.format("%s %.2f", settings.getDefaultCurrency(), netBalance));
        netBalanceLabel.setForeground(netBalance >= 0 ? new Color(0, 128, 0) : Color.RED);
        basicSummaryPanel.add(netBalanceLabel);
        
        // 月度汇总表格
        String[] columnNames = {"Month", "Income", "Expense", "Net Balance"};
        Object[][] data = new Object[12][4];
        
        for (int i = 0; i < 12; i++) {
            Month month = Month.of(i + 1);
            double income = monthlyIncomes.getOrDefault(month, 0.0);
            double expense = monthlyExpenses.getOrDefault(month, 0.0);
            double balance = income - expense;
            
            data[i][0] = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            data[i][1] = String.format("%.2f", income);
            data[i][2] = String.format("%.2f", expense);
            data[i][3] = String.format("%.2f", balance);
        }
        
        JTable monthlyTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(monthlyTable);
        
        // 将两部分添加到汇总面板
        summaryPanel.add(basicSummaryPanel, BorderLayout.NORTH);
        summaryPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report Data to CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try {
                String reportType = (String) reportTypeComboBox.getSelectedItem();
                int year = (int) yearComboBox.getSelectedItem();
                
                if (reportType.equals(ReportType.MONTHLY.toString())) {
                    Month month = (Month) monthComboBox.getSelectedItem();
                    exportMonthlyReportToCSV(filePath, year, month);
                } else if (reportType.equals(ReportType.QUARTERLY.toString())) {
                    exportQuarterlyReportToCSV(filePath, year);
                } else if (reportType.equals(ReportType.YEARLY.toString())) {
                    exportYearlyReportToCSV(filePath, year);
                }
                
                JOptionPane.showMessageDialog(this, 
                        "Report data successfully exported to " + filePath, 
                        "Export Successful", 
                        JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                        "Export failed: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportMonthlyReportToCSV(String filePath, int year, Month month) throws DataAccessException, IOException {
        // 获取月度数据
        double totalIncome = transactionService.getTotalIncomeForMonth(year, month);
        double totalExpense = transactionService.getTotalExpenseForMonth(year, month);
        double netBalance = totalIncome - totalExpense;
        
        Map<String, Double> expensesByCategory = transactionService.getExpensesByCategory(year, month);
        Map<String, Double> incomeByCategory = transactionService.getIncomeByCategory(year, month);
        Map<String, BudgetService.BudgetStatus> budgetStatus = budgetService.checkBudgetStatus(year, month);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // 写入标题
            writer.write(year + " " + month.getValue() + " Monthly Financial Report\n\n");
            
            // 写入基本财务汇总
            writer.write("Financial Summary\n");
            writer.write("Total Income," + String.format("%.2f", totalIncome) + "\n");
            writer.write("Total Expense," + String.format("%.2f", totalExpense) + "\n");
            writer.write("Net Balance," + String.format("%.2f", netBalance) + "\n\n");
            
            // 写入支出分类
            writer.write("Expense Categories\n");
            writer.write("Category,Amount\n");
            for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
                writer.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()) + "\n");
            }
            writer.write("\n");
            
            // 写入收入分类
            writer.write("Income Categories\n");
            writer.write("Category,Amount\n");
            for (Map.Entry<String, Double> entry : incomeByCategory.entrySet()) {
                writer.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()) + "\n");
            }
            writer.write("\n");
            
            // 写入预算执行情况
            writer.write("Budget Execution\n");
            writer.write("Category,Budget Amount,Actual Spending,Remaining,Usage Percentage,Status\n");
            for (Map.Entry<String, BudgetService.BudgetStatus> entry : budgetStatus.entrySet()) {
                BudgetService.BudgetStatus status = entry.getValue();
                String statusText;
                
                switch (status.getStatus()) {
                    case NORMAL:
                        statusText = "Normal";
                        break;
                    case WARNING:
                        statusText = "Near Limit";
                        break;
                    case OVER_BUDGET:
                        statusText = "Over Budget";
                        break;
                    default:
                        statusText = "";
                }
                
                writer.write(String.format("%s,%.2f,%.2f,%.2f,%.1f%%,%s\n",
                        entry.getKey(),
                        status.getLimit(),
                        status.getSpent(),
                        status.getRemaining(),
                        status.getPercentage(),
                        statusText));
            }
        }
    }
    
    private void exportQuarterlyReportToCSV(String filePath, int year) throws DataAccessException, IOException {
        // 获取季度数据
        double q1Income = 0, q1Expense = 0;
        double q2Income = 0, q2Expense = 0;
        double q3Income = 0, q3Expense = 0;
        double q4Income = 0, q4Expense = 0;
        
        // 第一季度 (1-3月)
        for (Month month : new Month[]{Month.JANUARY, Month.FEBRUARY, Month.MARCH}) {
            q1Income += transactionService.getTotalIncomeForMonth(year, month);
            q1Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 第二季度 (4-6月)
        for (Month month : new Month[]{Month.APRIL, Month.MAY, Month.JUNE}) {
            q2Income += transactionService.getTotalIncomeForMonth(year, month);
            q2Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 第三季度 (7-9月)
        for (Month month : new Month[]{Month.JULY, Month.AUGUST, Month.SEPTEMBER}) {
            q3Income += transactionService.getTotalIncomeForMonth(year, month);
            q3Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 第四季度 (10-12月)
        for (Month month : new Month[]{Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER}) {
            q4Income += transactionService.getTotalIncomeForMonth(year, month);
            q4Expense += transactionService.getTotalExpenseForMonth(year, month);
        }
        
        // 计算净余额
        double q1Balance = q1Income - q1Expense;
        double q2Balance = q2Income - q2Expense;
        double q3Balance = q3Income - q3Expense;
        double q4Balance = q4Income - q4Expense;
        double yearlyIncome = q1Income + q2Income + q3Income + q4Income;
        double yearlyExpense = q1Expense + q2Expense + q3Expense + q4Expense;
        double yearlyBalance = yearlyIncome - yearlyExpense;
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // 写入标题
            writer.write(year + " Quarterly Financial Report\n\n");
            
            // 写入季度数据
            writer.write("Quarter,Income,Expense,Net Balance\n");
            writer.write(String.format("Q1,%.2f,%.2f,%.2f\n", q1Income, q1Expense, q1Balance));
            writer.write(String.format("Q2,%.2f,%.2f,%.2f\n", q2Income, q2Expense, q2Balance));
            writer.write(String.format("Q3,%.2f,%.2f,%.2f\n", q3Income, q3Expense, q3Balance));
            writer.write(String.format("Q4,%.2f,%.2f,%.2f\n", q4Income, q4Expense, q4Balance));
            writer.write(String.format("Yearly Total,%.2f,%.2f,%.2f\n", yearlyIncome, yearlyExpense, yearlyBalance));
        }
    }
    
    private void exportYearlyReportToCSV(String filePath, int year) throws DataAccessException, IOException {
        // 获取年度数据
        double totalIncome = transactionService.getTotalIncomeForYear(year);
        double totalExpense = transactionService.getTotalExpenseForYear(year);
        double netBalance = totalIncome - totalExpense;
        
        // 获取月度趋势数据
        Map<Month, Double> monthlyIncomes = transactionService.getMonthlyTotals(year, Transaction.TransactionType.INCOME);
        Map<Month, Double> monthlyExpenses = transactionService.getMonthlyTotals(year, Transaction.TransactionType.EXPENSE);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // 写入标题
            writer.write(year + " Annual Financial Report\n\n");
            
            // 写入基本财务汇总
            writer.write("Financial Summary\n");
            writer.write("Annual Total Income," + String.format("%.2f", totalIncome) + "\n");
            writer.write("Annual Total Expense," + String.format("%.2f", totalExpense) + "\n");
            writer.write("Annual Net Balance," + String.format("%.2f", netBalance) + "\n\n");
            
            // 写入月度数据
            writer.write("Monthly Details\n");
            writer.write("Month,Income,Expense,Net Balance\n");
            
            for (int i = 1; i <= 12; i++) {
                Month month = Month.of(i);
                double income = monthlyIncomes.getOrDefault(month, 0.0);
                double expense = monthlyExpenses.getOrDefault(month, 0.0);
                double balance = income - expense;
                
                writer.write(String.format("%s,%.2f,%.2f,%.2f\n",
                        month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        income,
                        expense,
                        balance));
            }
        }
    }
    
    /**
     * 创建可调整大小的图表面板
     * @param chart 要显示的图表
     * @return 可调整大小的图表面板
     */
    private ChartPanel createResizableChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        
        // 启用缩放
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        
        // 允许保存为图片
        chartPanel.setPopupMenu(chartPanel.getPopupMenu());
        
        // 设置大小属性
        chartPanel.setMaximumDrawWidth(2000);
        chartPanel.setMaximumDrawHeight(2000);
        
        // 允许面板调整大小
        chartPanel.setPreferredSize(new Dimension(800, 500));
        
        return chartPanel;
    }
} 