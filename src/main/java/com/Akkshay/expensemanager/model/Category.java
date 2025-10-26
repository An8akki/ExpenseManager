package com.Akkshay.expensemanager.model;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Represents the CATEGORIES table in the database.
 * Each instance of this class is a single category row.
 */
@Entity
@Table(name = "TBL_CATEGORIES")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // For Oracle 12c+
    // Note: For Oracle 11g, the sequence and trigger handle this.
    // Hibernate understands this and works correctly.
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String categoryName;

    // Establishes a one-to-many relationship with Expense.
    // One category can have many expenses.
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Expense> expenses;

    // --- Constructors --- //

    /**
     * Default constructor required by Hibernate.
     */
    public Category() {
    }

    public Category(String categoryName) {
        this.categoryName = categoryName;
    }

    // --- Getters and Setters --- //

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<Expense> expenses) {
        this.expenses = expenses;
    }

    /**
     * The toString() method is overridden to return the category name.
     * This is very useful for displaying categories in a ComboBox (dropdown menu) in the UI.
     */
    @Override
    public String toString() {
        return categoryName;
    }
}

