package com.doubtflow.repository;

import com.doubtflow.dto.MentorWorkload;
import com.doubtflow.model.Mentor;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MentorRepository {

    private final DataSource dataSource;

    public MentorRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Mentor save(Mentor mentor) {
        String sql = "INSERT INTO mentors (name, email, password_hash, expertise, active) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, mentor.getName());
            statement.setString(2, mentor.getEmail());
            statement.setString(3, mentor.getPasswordHash());
            statement.setString(4, mentor.getExpertise());
            statement.setBoolean(5, mentor.isActive());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    mentor.setId(keys.getLong(1));
                }
            }

            return mentor;
        } catch (Exception exception) {
            throw new RuntimeException("Could not save mentor.", exception);
        }
    }

    public Optional<Mentor> findByEmail(String email) {
        String sql = "SELECT * FROM mentors WHERE email = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapMentor(resultSet));
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find mentor by email.", exception);
        }
    }

    public Optional<Mentor> findById(Long id) {
        String sql = "SELECT * FROM mentors WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapMentor(resultSet));
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find mentor by id.", exception);
        }
    }

    public List<Mentor> findAll() {
        String sql = "SELECT * FROM mentors ORDER BY name";
        List<Mentor> mentors = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                mentors.add(mapMentor(resultSet));
            }

            return mentors;
        } catch (Exception exception) {
            throw new RuntimeException("Could not find mentors.", exception);
        }
    }

    public Optional<Mentor> findLeastBusyMentorForCategory(String category) {
        String keyword = switch (category) {
            case "CONCEPTUAL" -> "%concept%";
            case "CODING" -> "%coding%";
            case "DEBUGGING" -> "%debug%";
            default -> "%";
        };

        Optional<Mentor> categoryMentor = findLeastBusyByExpertise(keyword);
        return categoryMentor.isPresent() ? categoryMentor : findLeastBusyActiveMentor();
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM mentors";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getLong(1);
        } catch (Exception exception) {
            throw new RuntimeException("Could not count mentors.", exception);
        }
    }

    public List<MentorWorkload> findWorkloads() {
        String sql = """
                SELECT m.id, m.name, m.expertise,
                       SUM(CASE WHEN d.status IN ('ASSIGNED', 'IN_PROGRESS') THEN 1 ELSE 0 END) AS assigned_count,
                       SUM(CASE WHEN d.status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_count
                FROM mentors m
                LEFT JOIN doubts d ON d.mentor_id = m.id
                GROUP BY m.id, m.name, m.expertise
                ORDER BY assigned_count DESC, resolved_count DESC, m.name
                """;

        List<MentorWorkload> workloads = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                workloads.add(new MentorWorkload(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("expertise"),
                        resultSet.getLong("assigned_count"),
                        resultSet.getLong("resolved_count")
                ));
            }

            return workloads;
        } catch (Exception exception) {
            throw new RuntimeException("Could not load mentor workloads.", exception);
        }
    }

    private Optional<Mentor> findLeastBusyByExpertise(String keyword) {
        String sql = """
                SELECT m.*, COUNT(d.id) AS active_doubt_count
                FROM mentors m
                LEFT JOIN doubts d
                    ON d.mentor_id = m.id
                   AND d.status IN ('ASSIGNED', 'IN_PROGRESS')
                WHERE m.active = TRUE
                  AND LOWER(m.expertise) LIKE ?
                GROUP BY m.id
                ORDER BY active_doubt_count ASC, m.name ASC
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, keyword);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapMentor(resultSet));
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find mentor by expertise.", exception);
        }
    }

    private Optional<Mentor> findLeastBusyActiveMentor() {
        String sql = """
                SELECT m.*, COUNT(d.id) AS active_doubt_count
                FROM mentors m
                LEFT JOIN doubts d
                    ON d.mentor_id = m.id
                   AND d.status IN ('ASSIGNED', 'IN_PROGRESS')
                WHERE m.active = TRUE
                GROUP BY m.id
                ORDER BY active_doubt_count ASC, m.name ASC
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return Optional.of(mapMentor(resultSet));
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find least busy mentor.", exception);
        }
    }

    private Mentor mapMentor(ResultSet resultSet) throws Exception {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new Mentor(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getString("expertise"),
                resultSet.getBoolean("active"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    }
}
