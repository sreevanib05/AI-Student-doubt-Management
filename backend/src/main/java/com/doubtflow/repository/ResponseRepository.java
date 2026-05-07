package com.doubtflow.repository;

import com.doubtflow.model.Response;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ResponseRepository {

    private final DataSource dataSource;

    public ResponseRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Response save(Response response) {
        String sql = "INSERT INTO responses (doubt_id, mentor_id, response_text) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, response.getDoubtId());
            statement.setLong(2, response.getMentorId());
            statement.setString(3, response.getResponseText());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    response.setId(keys.getLong(1));
                }
            }

            return response;
        } catch (Exception exception) {
            throw new RuntimeException("Could not save response.", exception);
        }
    }

    public List<Response> findByDoubtId(Long doubtId) {
        String sql = "SELECT * FROM responses WHERE doubt_id = ? ORDER BY created_at DESC";
        List<Response> responses = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, doubtId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    responses.add(mapResponse(resultSet));
                }
            }

            return responses;
        } catch (Exception exception) {
            throw new RuntimeException("Could not load responses.", exception);
        }
    }

    private Response mapResponse(ResultSet resultSet) throws Exception {
        Response response = new Response();
        response.setId(resultSet.getLong("id"));
        response.setDoubtId(resultSet.getLong("doubt_id"));
        response.setMentorId(resultSet.getLong("mentor_id"));
        response.setResponseText(resultSet.getString("response_text"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        response.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

        return response;
    }
}
