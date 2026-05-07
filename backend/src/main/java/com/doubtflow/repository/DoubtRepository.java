package com.doubtflow.repository;

import com.doubtflow.dto.CategoryStat;
import com.doubtflow.exception.InvalidCategoryException;
import com.doubtflow.model.Doubt;
import com.doubtflow.model.DoubtCategory;
import com.doubtflow.model.DoubtFactory;
import com.doubtflow.model.DoubtStatus;
import com.doubtflow.model.SolvedDoubtAnswer;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DoubtRepository {

    private static final String BASE_SELECT = """
            SELECT d.*,
                   s.name AS student_name,
                   m.name AS mentor_name,
                   (
                       SELECT r.response_text
                       FROM responses r
                       WHERE r.doubt_id = d.id
                       ORDER BY r.created_at DESC
                       LIMIT 1
                   ) AS latest_response
            FROM doubts d
            JOIN students s ON s.id = d.student_id
            LEFT JOIN mentors m ON m.id = d.mentor_id
            """;

    private final DataSource dataSource;

    public DoubtRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Doubt save(Doubt doubt) {
        String sql = """
                INSERT INTO doubts (title, description, category, status, student_id, mentor_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, doubt.getTitle());
            statement.setString(2, doubt.getDescription());
            statement.setString(3, doubt.getCategory().name());
            statement.setString(4, doubt.getStatus().name());
            statement.setLong(5, doubt.getStudentId());

            if (doubt.getMentorId() == null) {
                statement.setNull(6, java.sql.Types.BIGINT);
            } else {
                statement.setLong(6, doubt.getMentorId());
            }

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    doubt.setId(keys.getLong(1));
                }
            }

            return findById(doubt.getId()).orElse(doubt);
        } catch (Exception exception) {
            throw new RuntimeException("Could not save doubt.", exception);
        }
    }

    public boolean existsDuplicateForStudent(Long studentId, String title, String description) {
        String sql = """
                SELECT COUNT(*)
                FROM doubts
                WHERE student_id = ?
                  AND status <> 'RESOLVED'
                  AND (
                        LOWER(title) = LOWER(?)
                     OR LOWER(description) = LOWER(?)
                     OR LOWER(CONCAT(title, ' ', description)) LIKE ?
                  )
                """;

        String compactTitle = title == null ? "" : title.trim().toLowerCase();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, studentId);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setString(4, "%" + compactTitle + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1) > 0;
            }
        } catch (Exception exception) {
            throw new RuntimeException("Could not check duplicate doubts.", exception);
        }
    }

    public Optional<Doubt> findById(Long id) {
        String sql = BASE_SELECT + " WHERE d.id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapDoubt(resultSet));
                }
            }

            return Optional.empty();
        } catch (Exception exception) {
            throw new RuntimeException("Could not find doubt by id.", exception);
        }
    }

    public List<Doubt> findByStudentId(Long studentId) {
        String sql = BASE_SELECT + " WHERE d.student_id = ? ORDER BY d.created_at DESC";
        return findManyByLong(sql, studentId);
    }

    public List<Doubt> findAssignedToMentor(Long mentorId) {
        String sql = BASE_SELECT + " WHERE d.mentor_id = ? ORDER BY d.created_at DESC";
        return findManyByLong(sql, mentorId);
    }

    public List<Doubt> findAll() {
        String sql = BASE_SELECT + " ORDER BY d.created_at DESC";
        List<Doubt> doubts = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                doubts.add(mapDoubt(resultSet));
            }

            return doubts;
        } catch (Exception exception) {
            throw new RuntimeException("Could not find doubts.", exception);
        }
    }

    public List<Doubt> findByCategory(DoubtCategory category) {
        String sql = BASE_SELECT + " WHERE d.category = ? ORDER BY d.created_at DESC";
        List<Doubt> doubts = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    doubts.add(mapDoubt(resultSet));
                }
            }

            return doubts;
        } catch (Exception exception) {
            throw new RuntimeException("Could not find doubts by category.", exception);
        }
    }

    public void updateStatus(Long doubtId, DoubtStatus status) {
        String sql = """
                UPDATE doubts
                SET status = ?,
                    resolved_at = CASE WHEN ? = 'RESOLVED' THEN CURRENT_TIMESTAMP ELSE resolved_at END
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setString(2, status.name());
            statement.setLong(3, doubtId);
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new RuntimeException("Could not update doubt status.", exception);
        }
    }

    public void assignMentor(Long doubtId, Long mentorId) {
        String sql = """
                UPDATE doubts
                SET mentor_id = ?,
                    status = 'ASSIGNED',
                    resolved_at = NULL
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, mentorId);
            statement.setLong(2, doubtId);
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new RuntimeException("Could not assign mentor.", exception);
        }
    }

    public void unassignMentor(Long doubtId) {
        String sql = "UPDATE doubts SET mentor_id = NULL, status = 'OPEN', resolved_at = NULL WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, doubtId);
            statement.executeUpdate();
        } catch (Exception exception) {
            throw new RuntimeException("Could not unassign mentor.", exception);
        }
    }

    public long countAll() {
        return countBySql("SELECT COUNT(*) FROM doubts");
    }

    public long countByStatus(DoubtStatus status) {
        String sql = "SELECT COUNT(*) FROM doubts WHERE status = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Could not count doubts by status.", exception);
        }
    }

    public List<CategoryStat> countByCategory() {
        String sql = "SELECT category, COUNT(*) AS total FROM doubts GROUP BY category ORDER BY total DESC";
        List<CategoryStat> stats = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                stats.add(new CategoryStat(resultSet.getString("category"), resultSet.getLong("total")));
            }

            return stats;
        } catch (Exception exception) {
            throw new RuntimeException("Could not count doubts by category.", exception);
        }
    }

    public List<SolvedDoubtAnswer> findSolvedDoubtsWithResponses() {
        String sql = """
                SELECT d.id, d.title, d.description, d.category, r.response_text
                FROM doubts d
                JOIN responses r ON r.doubt_id = d.id
                WHERE d.status = 'RESOLVED'
                ORDER BY r.created_at DESC
                LIMIT 50
                """;

        List<SolvedDoubtAnswer> answers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                answers.add(new SolvedDoubtAnswer(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getString("category"),
                        resultSet.getString("response_text")
                ));
            }

            return answers;
        } catch (Exception exception) {
            throw new RuntimeException("Could not load FAQ suggestions.", exception);
        }
    }

    private List<Doubt> findManyByLong(String sql, Long value) {
        List<Doubt> doubts = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    doubts.add(mapDoubt(resultSet));
                }
            }

            return doubts;
        } catch (Exception exception) {
            throw new RuntimeException("Could not load doubts.", exception);
        }
    }

    private long countBySql(String sql) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();
            return resultSet.getLong(1);
        } catch (Exception exception) {
            throw new RuntimeException("Could not count doubts.", exception);
        }
    }

    private Doubt mapDoubt(ResultSet resultSet) throws SQLException {
        try {
            Doubt doubt = DoubtFactory.createDoubt(resultSet.getString("category"));
            doubt.setId(resultSet.getLong("id"));
            doubt.setTitle(resultSet.getString("title"));
            doubt.setDescription(resultSet.getString("description"));
            doubt.setCategory(DoubtCategory.valueOf(resultSet.getString("category")));
            doubt.setStatus(DoubtStatus.valueOf(resultSet.getString("status")));
            doubt.setStudentId(resultSet.getLong("student_id"));

            Object mentorId = resultSet.getObject("mentor_id");
            doubt.setMentorId(mentorId == null ? null : resultSet.getLong("mentor_id"));

            doubt.setStudentName(resultSet.getString("student_name"));
            doubt.setMentorName(resultSet.getString("mentor_name"));
            doubt.setLatestResponse(resultSet.getString("latest_response"));

            Timestamp createdAt = resultSet.getTimestamp("created_at");
            Timestamp updatedAt = resultSet.getTimestamp("updated_at");
            Timestamp resolvedAt = resultSet.getTimestamp("resolved_at");

            doubt.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
            doubt.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
            doubt.setResolvedAt(resolvedAt == null ? null : resolvedAt.toLocalDateTime());

            return doubt;
        } catch (InvalidCategoryException exception) {
            throw new SQLException("Invalid category stored in database.", exception);
        }
    }
}
