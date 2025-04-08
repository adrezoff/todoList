package org.example.repositories;

import org.example.database.DatabaseConnection;
import org.example.models.User;
import org.example.bot.utils.UserState;

import java.sql.*;
import java.util.Optional;

public class UserRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            id BIGINT PRIMARY KEY,
            username VARCHAR(255),
            is_premium BOOLEAN DEFAULT FALSE,
            state VARCHAR(32) DEFAULT 'IDLE',
            time_zone VARCHAR(32) DEFAULT 'UTC',
            temp_data TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;

    private static final String INSERT_USER = """
    INSERT INTO users (id, username, is_premium, state, time_zone, temp_data)
    VALUES (?, ?, ?, ?, ?, ?)
    ON CONFLICT (id) DO NOTHING
    """;

    private static final String UPDATE_USER = """
    UPDATE users SET
        username = ?,
        is_premium = ?,
        state = ?,
        time_zone = ?,
        temp_data = ?
    WHERE id = ?
    """;

    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";

    public UserRepository() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public Optional<User> findById(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPremium(rs.getBoolean("is_premium"));

                    String stateStr = rs.getString("state");
                    if (stateStr != null) {
                        user.setState(UserState.valueOf(stateStr));
                    }

                    String tempDataJson = rs.getString("temp_data");
                    if (tempDataJson != null) {
                        user.setTempDataJson(tempDataJson);
                    }

                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user", e);
        }
        return Optional.empty();
    }

    public void save(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {
            stmt.setLong(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setBoolean(3, user.isPremium());
            stmt.setString(4, user.getState().name());
            stmt.setString(5, user.getTimeZone().toString());
            stmt.setString(6, user.getTempDataJson());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public void update(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER)) {
            stmt.setString(1, user.getUsername());
            stmt.setBoolean(2, user.isPremium());
            stmt.setString(3, user.getState().name());
            stmt.setString(4, user.getTimeZone().toString());
            stmt.setString(5, user.getTempDataJson());
            stmt.setLong(6, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }
}