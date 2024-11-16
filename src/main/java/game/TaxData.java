package game;

public class TaxData {
    private double income;
    private boolean isComplete;
    
    public void setIncome(double income) {
        this.income = income;
        checkCompletion();
    }
    
    public double getIncome() {
        return income;
    }
    
    private void checkCompletion() {
        isComplete = income > 0;
    }
    
    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public String toString() {
        return String.format("Income: $%.2f", income);
    }
}