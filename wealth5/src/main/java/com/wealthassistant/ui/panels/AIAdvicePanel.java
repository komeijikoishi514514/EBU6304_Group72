package com.wealthassistant.ui.panels;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AIAdvicePanel extends JPanel {
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton generateButton;
    private JTextArea adviceArea;

    public AIAdvicePanel() {
        setLayout(new BorderLayout());
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
    }

    private void layoutComponents() {
        // Create top control panel
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
    }

    private void generateAdvice() {
        // Get selected dates
        LocalDate startDate = ((java.util.Date) startDateSpinner.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = ((java.util.Date) endDateSpinner.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        // This is where the actual AI advice generation logic will be added in the future
        String advice = String.format("AI Advice (%s to %s):\n\n" +
                "1. Based on your portfolio analysis, consider increasing your stock allocation\n" +
                "2. Consider adding defensive assets such as gold ETFs\n" +
                "3. Focus on opportunities in the technology and healthcare sectors\n" +
                "4. Consider adding some fixed-income products to your portfolio",
                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        adviceArea.setText(advice);
    }
} 