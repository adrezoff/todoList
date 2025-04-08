package org.example.repositories;

import org.example.database.DatabaseConnection;
import org.example.models.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS tasks (
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT REFERENCES users(id),
            title VARCHAR(255) NOT NULL,
            description TEXT,
            start_date TIMESTAMP,
            end_date TIMESTAMP,
            completed BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;

    private static final String INSERT_TASK = """
        INSERT INTO tasks (user_id, title, description, start_date, end_date, completed)
        VALUES (?, ?, ?, ?, ?, ?)
        RETURNING id
        """;

    private static final String FIND_BY_USER_ID = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC";
    private static final String FIND_BY_ID = "SELECT * FROM tasks WHERE id = ?";
    private static final String UPDATE_TASK = """
        UPDATE tasks SET
            title = ?,
            description = ?,
            start_date = ?,
            end_date = ?,
            completed = ?
        WHERE id = ?
        """;

    private static final String DELETE_TASK = "DELETE FROM tasks WHERE id = ?";

    public TaskRepository() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }


    public void save(Task task) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_TASK, Statement.RETURN_GENERATED_KEYS)) {
            setTaskParameters(stmt, task);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    task.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save task", e);
        }
    }

    public List<Task> findByUserId(long userId) {
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USER_ID)) {
            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tasks", e);
        }
        return tasks;
    }

    public Optional<Task> findById(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find task", e);
        }
        return Optional.empty();
    }

    public void update(Task task) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_TASK)) {
            setTaskParameters(stmt, task);
            stmt.setLong(6, task.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task", e);
        }
    }

    public void delete(long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_TASK)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }

    private void setTaskParameters(PreparedStatement stmt, Task task) throws SQLException {
        stmt.setLong(1, task.getUserId());
        stmt.setString(2, task.getTitle());
        stmt.setString(3, task.getDescription());
        stmt.setObject(4, task.getStartDate());
        stmt.setObject(5, task.getEndDate());
        stmt.setBoolean(6, task.isCompleted());
    }

    private Task mapRowToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setUserId(rs.getLong("user_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStartDate(rs.getObject("start_date", LocalDateTime.class));
        task.setEndDate(rs.getObject("end_date", LocalDateTime.class));
        task.setCompleted(rs.getBoolean("completed"));
        return task;
    }
    public void markAsCompleted(long taskId) {
        String sql = "UPDATE tasks SET completed = true WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark task as completed", e);
        }
    }
}