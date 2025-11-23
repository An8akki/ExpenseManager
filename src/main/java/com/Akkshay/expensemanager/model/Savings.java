package com.Akkshay.expensemanager.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TBL_SAVINGS")
public class Savings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "savings_id")
    private Long savingsId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "savings_date", nullable = false)
    private LocalDate savingsDate;

    @Column(name = "goal", length = 100)
    private String goal;

    @Column(name = "description", length = 255)
    private String description;

    public Savings() {
    }

    public Savings(BigDecimal amount, LocalDate savingsDate, String description, String goal) {
        this.amount = amount;
        this.savingsDate = savingsDate;
        this.description = description;
        this.goal = goal;
    }

    public Long getSavingsId() {
        return savingsId;
    }

    public void setSavingsId(Long savingsId) {
        this.savingsId = savingsId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getSavingsDate() {
        return savingsDate;
    }

    public void setSavingsDate(LocalDate savingsDate) {
        this.savingsDate = savingsDate;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
