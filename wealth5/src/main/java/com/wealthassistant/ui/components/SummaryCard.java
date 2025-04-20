package com.wealthassistant.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SummaryCard extends JPanel {
    private JLabel titleLabel;
    private JLabel valueLabel;
    private JLabel descriptionLabel;
    private Color accentColor;

    public SummaryCard(String title, String value, Color accentColor, String description) {
        this.accentColor = accentColor;
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                new EmptyBorder(15, 15, 15, 15)
        ));
        setBackground(Color.WHITE);
        
        titleLabel = new JLabel(title);
        titleLabel.setForeground(accentColor);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        
        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        
        descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descriptionLabel.setForeground(Color.GRAY);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(valueLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(descriptionLabel);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    public void updateValues(String title, String value, String description) {
        titleLabel.setText(title);
        valueLabel.setText(value);
        descriptionLabel.setText(description);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // 绘制顶部装饰条
        g2d.setColor(accentColor);
        g2d.fillRect(0, 0, getWidth(), 5);
        
        g2d.dispose();
    }
} 