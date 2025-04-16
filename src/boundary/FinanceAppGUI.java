import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

public class FinanceAppGUI extends JFrame {
    private JTextField amountField, categoryField, dateField, descriptionField;
    private JTextArea transactionArea;
    private TransactionManager manager;

    public FinanceAppGUI() {
        manager = new TransactionManager();

        setTitle("Personal Finance Tracker");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Transaction"));

        amountField = new JTextField();
        categoryField = new JTextField();
        dateField = new JTextField("2025-04-16");
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
        inputPanel.add(addButton);

        JButton viewButton = new JButton("View Transactions");
        inputPanel.add(viewButton);

        add(inputPanel, BorderLayout.NORTH);

        // Output area
        transactionArea = new JTextArea();
        transactionArea.setEditable(false);
        transactionArea.setBorder(BorderFactory.createTitledBorder("Transaction Records"));
        add(new JScrollPane(transactionArea), BorderLayout.CENTER);

        // Add transaction action
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
                JOptionPane.showMessageDialog(this, "Invalid input. Please check the format.");
            }
        });

        // View transactions action
        viewButton.addActionListener(e -> {
            List<Transaction> transactions = manager.getTransactions();
            transactionArea.setText("");
            for (Transaction t : transactions) {
                transactionArea.append(t.toString() + "\n");
            }
        });
    }

    private void clearFields() {
        amountField.setText("");
        categoryField.setText("");
        dateField.setText(LocalDate.now().toString());
        descriptionField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FinanceAppGUI().setVisible(true);
        });
    }
}
