package com.wealthassistant.ui.panels;

import com.wealthassistant.service.LlamaService;
import com.wealthassistant.model.Transaction;
import com.wealthassistant.service.TransactionService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AIAdvicePanel extends JPanel {
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton generateButton;
    private JTextArea adviceArea;
    private LlamaService llamaService;
    private TransactionService transactionService;
    private JProgressBar progressBar;

    public AIAdvicePanel() {
        setLayout(new BorderLayout());
        llamaService = new LlamaService();
        transactionService = new TransactionService();
        initializeComponents();
        layoutComponents();
    }

    private void initializeComponents() {
        // Initialize date spinners
        SpinnerDateModel startModel = new SpinnerDateModel();
        SpinnerDateModel endModel = new SpinnerDateModel();
        startDateSpinner = new JSpinner(startModel);
        endDateSpinner = new JSpinner(endModel);
        
        // Set date spinner format
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startEditor);
        endDateSpinner.setEditor(endEditor);
        
        // Initialize generate button
        generateButton = new JButton("Generate AI Advice");
        generateButton.addActionListener(e -> generateAdvice());
        
        // Initialize advice display area
        adviceArea = new JTextArea();
        adviceArea.setEditable(false);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);
        
        // Initialize progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
    }

    private void layoutComponents() {
        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.add(new JLabel("Start Date:"));
        controlPanel.add(startDateSpinner);
        controlPanel.add(new JLabel("End Date:"));
        controlPanel.add(endDateSpinner);
        controlPanel.add(generateButton);

        // Create advice display area
        JScrollPane scrollPane = new JScrollPane(adviceArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // Add to main panel
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }

    private void generateAdvice() {
        LocalDate startDate = ((java.util.Date) startDateSpinner.getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = ((java.util.Date) endDateSpinner.getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Show loading message and progress bar
        adviceArea.setText("Analyzing transaction data... Please wait...");
        adviceArea.setCaretPosition(0);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        generateButton.setEnabled(false);

        // Generate advice in a background thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Check if Ollama service is available
                if (!llamaService.isServiceAvailable()) {
                    adviceArea.setText("Error: Ollama service is not available. Please make sure to:\n" +
                        "1. Install Ollama from https://ollama.ai\n" +
                        "2. Run 'ollama serve' in terminal\n" +
                        "3. Run 'ollama pull llama2' to download the model\n" +
                        "4. Restart the application");
                    return;
                }

                // Get transactions for the selected period
                List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
                
                if (transactions.isEmpty()) {
                    adviceArea.setText("No transactions found for the selected period.");
                    return;
                }
                
                // Calculate total income and expenses by category
                double totalIncome = transactions.stream()
                    .filter(t -> t.getAmount() > 0)
                    .mapToDouble(Transaction::getAmount)
                    .sum();
                
                Map<String, Double> categoryExpenses = transactions.stream()
                    .filter(t -> t.getAmount() < 0)
                    .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                    ));

                // Generate advice
                String advice = llamaService.generateAdvice(startDate, endDate, categoryExpenses, totalIncome);
                adviceArea.setText(advice);
            } catch (Exception e) {
                adviceArea.setText("Error generating advice: " + e.getMessage());
            } finally {
                progressBar.setVisible(false);
                generateButton.setEnabled(true);
            }
        });
    }
} 