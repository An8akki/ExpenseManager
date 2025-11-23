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
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, unique = true, length = 100)
    private String categoryName;

    // Establishes a one-to-many relationship with Expense.
    // One category can have many expenses.
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Expense> expenses;

    // --- Constructors --- //

    /** Default constructor required by Hibernate. */
    public Category() {}

    /** Constructor for use when creating a new category by name. */
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

    /** Display the category name in UI dropdowns etc. */
    @Override
    public String toString() {
        return categoryName;
    }

    /** For ComboBox and collection support—categories with the same name are considered equal. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return categoryName != null && categoryName.equals(category.categoryName);
    }

    @Override
    public int hashCode() {
        return categoryName != null ? categoryName.hashCode() : 0;
    }
}
