package de.syntaxjason.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.syntaxjason.model.EventType;
import de.syntaxjason.model.SabathonConfig;
import de.syntaxjason.model.TimerEvent;
import de.syntaxjason.util.PathManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventRepository implements IEventRepository {
    private final HikariDataSource dataSource;

    public EventRepository(SabathonConfig config) {
        this.dataSource = createDataSource(config);
        initializeDatabase();
    }

    private HikariDataSource createDataSource(SabathonConfig config) {
        HikariConfig hikariConfig = new HikariConfig();

        String dbPath = PathManager.getDatabasePath().toString();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbPath);
        hikariConfig.setDriverClassName("org.sqlite.JDBC");

        hikariConfig.setMaximumPoolSize(config.getDatabaseSettings().getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getDatabaseSettings().getMinIdleConnections());
        hikariConfig.setConnectionTimeout(config.getDatabaseSettings().getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getDatabaseSettings().getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getDatabaseSettings().getMaxLifetime());
        hikariConfig.setAutoCommit(config.getDatabaseSettings().isAutoCommit());

        hikariConfig.setPoolName("SabathonPool");
        hikariConfig.setConnectionTestQuery("SELECT 1");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(hikariConfig);
    }

    private void initializeDatabase() {
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    event_type TEXT NOT NULL,
                    username TEXT NOT NULL,
                    channel_name TEXT NOT NULL,
                    minutes_added INTEGER NOT NULL,
                    timestamp TEXT NOT NULL,
                    details TEXT
                )
                """;

        String createIndexSQL = "CREATE INDEX IF NOT EXISTS idx_username ON events(username)";
        String createTimestampIndexSQL = "CREATE INDEX IF NOT EXISTS idx_timestamp ON events(timestamp)";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            stmt.execute(createIndexSQL);
            stmt.execute(createTimestampIndexSQL);

            if (!conn.getAutoCommit()) {
                conn.commit();
            }

            System.out.println("Datenbank initialisiert: " + PathManager.getDatabasePath());

        } catch (SQLException e) {
            System.err.println("Fehler beim Initialisieren der Datenbank");
            e.printStackTrace();
        }
    }

    @Override
    public void save(TimerEvent event) {
        String sql = "INSERT INTO events (event_type, username, channel_name, minutes_added, timestamp, details) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, event.getEventType().name());
            pstmt.setString(2, event.getUsername());
            pstmt.setString(3, event.getChannelName());
            pstmt.setInt(4, event.getMinutesAdded());
            pstmt.setString(5, event.getTimestamp().toString());
            pstmt.setString(6, event.getDetails());

            pstmt.executeUpdate();

            if (!conn.getAutoCommit()) {
                conn.commit();
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Speichern des Events");
            e.printStackTrace();
        }
    }

    @Override
    public List<TimerEvent> findAll() {
        List<TimerEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                events.add(mapResultSetToEvent(rs));
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden aller Events");
            e.printStackTrace();
        }

        return events;
    }

    @Override
    public List<TimerEvent> findByUsername(String username) {
        List<TimerEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE username = ? ORDER BY timestamp DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    events.add(mapResultSetToEvent(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Laden der Events für User: " + username);
            e.printStackTrace();
        }

        return events;
    }

    @Override
    public boolean existsByUsernameAndTypeAfter(String username, EventType eventType, LocalDateTime after) {
        String sql = "SELECT COUNT(*) FROM events WHERE username = ? AND event_type = ? AND timestamp > ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, eventType.name());
            pstmt.setString(3, after.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("Fehler beim Prüfen auf Event-Existenz");
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM events";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

            if (!conn.getAutoCommit()) {
                conn.commit();
            }

            System.out.println("Alle Events gelöscht");

        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen aller Events");
            e.printStackTrace();
        }
    }

    private TimerEvent mapResultSetToEvent(ResultSet rs) throws SQLException {
        return new TimerEvent.Builder()
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .username(rs.getString("username"))
                .channelName(rs.getString("channel_name"))
                .minutesAdded(rs.getInt("minutes_added"))
                .timestamp(LocalDateTime.parse(rs.getString("timestamp")))
                .details(rs.getString("details"))
                .build();
    }
}
