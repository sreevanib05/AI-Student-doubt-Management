package com.doubtflow.repository;

import com.doubtflow.model.Admin;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class AdminRepository {

    private final DataSource dataSource;

    public AdminRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Admin save(Admin admin) {
        String sql = "INSERT INTO admins (name, email, password_hash) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, admin.getName());
            statement.setString(2, admin.getEmail());
            statement.setString(3, admin.getPasswordHash());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    admin.setId(keys.getLong(1));
                }
            }

            return admin;
        } catch (Exception exception) {
            throw new RuntimeException("Could not save admin.", exception);
        }
    }

    public Optional<Admin> findByEmail(String email) {
        String sql = "SELECT * FROM admins WHERE email = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAdmin(resultSet));
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find admin by email.", exception);
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM admins";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getLong(1);
        } catch (Exception exception) {
            throw new RuntimeException("Could not count admins.", exception);
        }
    }

    private Admin mapAdmin(ResultSet resultSet) throws Exception {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new Admin(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}
