import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class FinanceAppGUI extends JFrame {
    private JTextField amountField, categoryField, dateField, descriptionField;
    private JTextArea transactionArea;
    private TransactionManager manager;

    public FinanceAppGUI() {
        manager = new TransactionManager();

        setTitle("Personal Finance Tracker");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add or Import Transaction"));

        amountField = new JTextField();
        categoryField = new JTextField();
        dateField = new JTextField(LocalDate.now().toString());
        descriptionField = new JTextField();

        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Date (yyyy-MM-dd):"));
        inputPanel.add(dateField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descriptionField);

        JButton addButton = new JButton("Add Transaction");
        JButton viewButton = new JButton("View Records");
        JButton importButton = new JButton("Import Transactions");

        inputPanel.add(addButton);
        inputPanel.add(viewButton);
        inputPanel.add(importButton);

        add(inputPanel, BorderLayout.NORTH);

        // Output area
        transactionArea = new JTextArea();
        transactionArea.setEditable(false);
        transactionArea.setBorder(BorderFactory.createTitledBorder("Transaction Records"));
        add(new JScrollPane(transactionArea), BorderLayout.CENTER);

        // Add transaction button
        addButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String category = categoryField.getText();
                LocalDate date = LocalDate.parse(dateField.getText());
                String description = descriptionField.getText();

                Transaction t = new Transaction(amount, category, date, description);
                manager.addTransaction(t);
                JOptionPane.showMessageDialog(this, "Transaction added successfully!");
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input format. Please check your entries.");
            }
        });

        // View records button
        viewButton.addActionListener(e -> refreshTransactionList());

        // Import transactions button
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                boolean success = false;

                if (selectedFile.getName().endsWith(".csv")) {
                    success = manager.importFromCSV(selectedFile);
                } else if (selectedFile.getName().endsWith(".json")) {
                    success = manager.importFromJSON(selectedFile);
                } else {
                    JOptionPane.showMessageDialog(this, "Only CSV or JSON files are supported!");
                    return;
                }

                if (success) {
                    JOptionPane.showMessageDialog(this, "Import successful!");
                    refreshTransactionList();
                } else {
                    JOptionPane.showMessageDialog(this, "Import failed. File format error.");
                }
            }
        });
    }

    private void clearFields() {
        amountField.setText("");
        categoryField.setText("");
        dateField.setText(LocalDate.now().toString());
        descriptionField.setText("");
    }

    private void refreshTransactionList() {
        List<Transaction> transactions = manager.getTransactions();
        transactionArea.setText("");
        for (Transaction t : transactions) {
            transactionArea.append(t.toString() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinanceAppGUI().setVisible(true));
    }
}
