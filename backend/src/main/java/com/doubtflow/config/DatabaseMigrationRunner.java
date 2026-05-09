package com.doubtflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final DataSource dataSource;
    private final DataSeeder dataSeeder;

    public DatabaseMigrationRunner(DataSource dataSource, DataSeeder dataSeeder) {
        this.dataSource = dataSource;
        this.dataSeeder = dataSeeder;
    }

    @Override
    public void run(String... args) {
        Thread bootstrapThread = new Thread(this::bootstrapDatabase, "database-bootstrap");
        bootstrapThread.setDaemon(true);
        bootstrapThread.start();
    }

    private void bootstrapDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            createTables(connection);
            addColumnIfMissing(connection, "doubts", "subject", "VARCHAR(80) NOT NULL DEFAULT 'General'");
            addColumnIfMissing(connection, "doubts", "context_notes", "TEXT NULL");
            addColumnIfMissing(connection, "doubts", "prompt_template", "TEXT NULL");
            addColumnIfMissing(connection, "doubts", "pdf_file_name", "VARCHAR(255) NULL");
            addColumnIfMissing(connection, "doubts", "pdf_content_type", "VARCHAR(120) NULL");
            addColumnIfMissing(connection, "doubts", "pdf_data", "MEDIUMTEXT NULL");
            createIndexIfMissing(connection, "doubts", "idx_doubts_student",
                    "CREATE INDEX idx_doubts_student ON doubts(student_id)");
            createIndexIfMissing(connection, "doubts", "idx_doubts_mentor",
                    "CREATE INDEX idx_doubts_mentor ON doubts(mentor_id)");
            createIndexIfMissing(connection, "doubts", "idx_doubts_category",
                    "CREATE INDEX idx_doubts_category ON doubts(category)");
            createIndexIfMissing(connection, "doubts", "idx_doubts_status",
                    "CREATE INDEX idx_doubts_status ON doubts(status)");
            dataSeeder.seedStarterAccounts();
            LOGGER.info("Database bootstrap completed.");
        } catch (Exception exception) {
            LOGGER.warn(
                    "Database bootstrap could not complete. Check DB_URL, DB_USERNAME, DB_PASSWORD, and schema permissions.",
                    exception
            );
        }
    }

    private void createTables(Connection connection) throws Exception {
        executeUpdate(connection, """
                CREATE TABLE IF NOT EXISTS students (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

        executeUpdate(connection, """
                CREATE TABLE IF NOT EXISTS mentors (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    expertise VARCHAR(100) NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

        executeUpdate(connection, """
                CREATE TABLE IF NOT EXISTS admins (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

        executeUpdate(connection, """
                CREATE TABLE IF NOT EXISTS doubts (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    subject VARCHAR(80) NOT NULL DEFAULT 'General',
                    context_notes TEXT NULL,
                    prompt_template TEXT NULL,
                    pdf_file_name VARCHAR(255) NULL,
                    pdf_content_type VARCHAR(120) NULL,
                    pdf_data MEDIUMTEXT NULL,
                    category VARCHAR(50) NOT NULL,
                    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
                    student_id BIGINT NOT NULL,
                    mentor_id BIGINT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    resolved_at TIMESTAMP NULL,
                    CONSTRAINT fk_doubts_student
                        FOREIGN KEY (student_id) REFERENCES students(id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_doubts_mentor
                        FOREIGN KEY (mentor_id) REFERENCES mentors(id)
                        ON DELETE SET NULL
                )
                """);

        executeUpdate(connection, """
                CREATE TABLE IF NOT EXISTS responses (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    doubt_id BIGINT NOT NULL,
                    mentor_id BIGINT NOT NULL,
                    response_text TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_responses_doubt
                        FOREIGN KEY (doubt_id) REFERENCES doubts(id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_responses_mentor
                        FOREIGN KEY (mentor_id) REFERENCES mentors(id)
                        ON DELETE CASCADE
                )
                """);
    }

    private void executeUpdate(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void addColumnIfMissing(Connection connection, String tableName, String columnName, String definition)
            throws Exception {

        if (!hasTable(connection, tableName)) {
            return;
        }

        if (hasColumn(connection, tableName, columnName)) {
            return;
        }

        executeUpdate(connection, "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
    }

    private void createIndexIfMissing(Connection connection, String tableName, String indexName, String sql)
            throws Exception {

        if (!hasIndex(connection, tableName, indexName)) {
            executeUpdate(connection, sql);
        }
    }

    private boolean hasTable(Connection connection, String tableName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet tables = metadata.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private boolean hasColumn(Connection connection, String tableName, String columnName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet columns = metadata.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }

    private boolean hasIndex(Connection connection, String tableName, String indexName) throws Exception {
        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet indexes = metadata.getIndexInfo(connection.getCatalog(), null, tableName, false, false)) {
            while (indexes.next()) {
                if (indexName.equals(indexes.getString("INDEX_NAME"))) {
                    return true;
                }
            }

            return false;
        }
    }
}
