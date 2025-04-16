import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    private double amount;
    private String category;
    private LocalDate date;
    private String description;

    public Transaction(double amount, String category, LocalDate date, String description) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("日期: %s | 类别: %s | 金额: %.2f | 描述: %s",
                date.toString(), category, amount, description);
    }
}
