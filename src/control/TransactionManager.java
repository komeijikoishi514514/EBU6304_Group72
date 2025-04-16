import java.io.*;
import java.util.*;
import java.time.LocalDate;

public class TransactionManager {
    private List<Transaction> transactions;
    private static final String FILE_NAME = "transactions.txt";

    public TransactionManager() {
        transactions = new ArrayList<>();
        loadTransactions();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        saveTransactions();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    private void saveTransactions() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) {
                writer.println(t.getAmount() + "," +
                        t.getCategory() + "," +
                        t.getDate() + "," +
                        t.getDescription());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTransactions() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",", 4);
                if (parts.length == 4) {
                    double amount = Double.parseDouble(parts[0]);
                    String category = parts[1];
                    LocalDate date = LocalDate.parse(parts[2]);
                    String description = parts[3];
                    transactions.add(new Transaction(amount, category, date, description));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
