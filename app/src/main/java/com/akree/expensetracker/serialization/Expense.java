package com.akree.expensetracker.serialization;

public class Expense {

    private double amount;
    private String category;
    private String date;
    private String type;

    public Expense(double amount, String category, String date, String type) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.type = type;
    }

    public Expense() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
