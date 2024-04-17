package com.akree.expensetracker.serialization;

import java.util.LinkedList;
import java.util.List;

public class User {

    private String username;
    private String email;
    private String profilePicture;
    private double budget;
    private List<String> categories = new LinkedList<>();

    public User(String username, String email, String profilePicture, double budget, List<String> categories) {
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
        this.budget = budget;
        this.categories = categories;
    }

    public User() {
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}

