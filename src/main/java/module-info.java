module com.Akkshay.expensemanager {
    // JavaFX modules required for the UI
    requires javafx.controls;
    requires javafx.fxml;

    // Hibernate module required for database interaction
    requires org.hibernate.orm.core;

    // Jakarta Persistence API, used by Hibernate entities
    requires jakarta.persistence;

    // Java SQL module for database connectivity
    requires java.sql;

    // Hibernate requires the JNDI API, which is in the java.naming module.
    requires java.naming;

    // The JavaFX graphics module needs access to the main package to launch the application.
    exports com.Akkshay.expensemanager to javafx.graphics;

    // --- FINAL FIX ---
    // Open the model package to both JavaFX (for the table) and Hibernate (for data access).
    opens com.Akkshay.expensemanager.model to javafx.base, org.hibernate.orm.core;

    // Open the controller package to the FXML loader.
    opens com.Akkshay.expensemanager.controller to javafx.fxml;
}

