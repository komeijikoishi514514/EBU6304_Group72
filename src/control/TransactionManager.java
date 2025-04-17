import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public boolean importFromCSV(File file) {
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
            saveTransactions();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean importFromJSON(File file) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                double amount = obj.getDouble("amount");
                String category = obj.getString("category");
                LocalDate date = LocalDate.parse(obj.getString("date"));
                String description = obj.getString("description");
                transactions.add(new Transaction(amount, category, date, description));
            }
            saveTransactions();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
