package com.doubtflow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Order(0)
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            addColumnIfMissing(connection, "doubts", "subject", "VARCHAR(80) NOT NULL DEFAULT 'General'");
            addColumnIfMissing(connection, "doubts", "context_notes", "TEXT NULL");
            addColumnIfMissing(connection, "doubts", "prompt_template", "TEXT NULL");
            addColumnIfMissing(connection, "doubts", "pdf_file_name", "VARCHAR(255) NULL");
            addColumnIfMissing(connection, "doubts", "pdf_content_type", "VARCHAR(120) NULL");
            addColumnIfMissing(connection, "doubts", "pdf_data", "MEDIUMTEXT NULL");
        }
    }

    private void addColumnIfMissing(Connection connection, String tableName, String columnName, String definition)
            throws Exception {

        if (hasColumn(connection, tableName, columnName)) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private boolean hasColumn(Connection connection, String tableName, String columnName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }
}
