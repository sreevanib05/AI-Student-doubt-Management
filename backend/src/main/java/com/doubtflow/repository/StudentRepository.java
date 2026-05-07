package com.doubtflow.repository;

import com.doubtflow.model.Student;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class StudentRepository {

    private final DataSource dataSource;

    public StudentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Student save(Student student) {
        String sql = "INSERT INTO students (name, email, password_hash) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, student.getName());
            statement.setString(2, student.getEmail());
            statement.setString(3, student.getPasswordHash());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setId(keys.getLong(1));
                }
            }

            return student;
        } catch (Exception exception) {
            throw new RuntimeException("Could not save student.", exception);
        }
    }

    public Optional<Student> findByEmail(String email) {
        String sql = "SELECT * FROM students WHERE email = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapStudent(resultSet));
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find student by email.", exception);
        }
    }

    public Optional<Student> findById(Long id) {
        String sql = "SELECT * FROM students WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapStudent(resultSet));
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find student by id.", exception);
        }
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getLong(1);
        } catch (Exception exception) {
            throw new RuntimeException("Could not count students.", exception);
        }
    }

    private Student mapStudent(ResultSet resultSet) throws Exception {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new Student(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}
