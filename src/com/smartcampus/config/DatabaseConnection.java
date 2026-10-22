package com.smartcampus.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Singleton Database Connection Manager for JDBC
 * Supports MySQL as primary server, with graceful automatic setup & fallback
 */
public class DatabaseConnection {

    private static Connection connection = null;
    private static String connectionMode = "Not Connected";
    private static boolean isMySql = false;

    private static String dbHost = "localhost";
    private static String dbPort = "3306";
    private static String dbName = "smart_campus_db";
    private static String dbUser = "root";
    private static String dbPassword = "";

    private static final String CONFIG_FILE = "database/db.properties";

    static {
        loadConfig();
    }

    private DatabaseConnection() {}

    public static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(fis);
                dbHost = props.getProperty("db.host", "localhost");
                dbPort = props.getProperty("db.port", "3306");
                dbName = props.getProperty("db.name", "smart_campus_db");
                dbUser = props.getProperty("db.user", "root");
                dbPassword = props.getProperty("db.password", "");
            } catch (Exception e) {
                System.err.println("Warning: Could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        }
    }

    public static void saveConfig(String host, String port, String name, String user, String pass) {
        dbHost = host;
        dbPort = port;
        dbName = name;
        dbUser = user;
        dbPassword = pass;

        try {
            File dir = new File("database");
            if (!dir.exists()) dir.mkdirs();

            Properties props = new Properties();
            props.setProperty("db.type", "mysql");
            props.setProperty("db.host", dbHost);
            props.setProperty("db.port", dbPort);
            props.setProperty("db.name", dbName);
            props.setProperty("db.user", dbUser);
            props.setProperty("db.password", dbPassword);

            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "Smart Campus Database Configuration");
            }
        } catch (Exception e) {
            System.err.println("Failed to save db config: " + e.getMessage());
        }
    }

    private static boolean initialized = false;

    public static synchronized void initDatabase() {
        // 1. Attempt MySQL connection first
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String serverUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            
            // Connect to MySQL server to ensure DB exists
            try (Connection serverConn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
                 Statement stmt = serverConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            }

            // Connect to target database
            String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection testConn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                initializeMySqlTables(testConn);
            }
            isMySql = true;
            connectionMode = "MySQL (" + dbHost + ":" + dbPort + "/" + dbName + ")";
            System.out.println(" Connected to MySQL Server: " + connectionMode);
            initialized = true;

        } catch (Exception mysqlEx) {
            System.err.println("⚠️ MySQL connection attempt failed: " + mysqlEx.getMessage());
            System.err.println("🔄 Initializing Local Database (SQLite) fallback for offline/demo reliability...");
            
            // 2. Fallback to SQLite so user is never blocked
            try {
                Class.forName("org.sqlite.JDBC");
                String sqliteUrl = "jdbc:sqlite:smart_campus_local.db";
                try (Connection sqliteConn = DriverManager.getConnection(sqliteUrl)) {
                    initializeSqliteTables(sqliteConn);
                }
                isMySql = false;
                connectionMode = "Local SQLite Mode (Fallback)";
                System.out.println(" Connected to SQLite: smart_campus_local.db");
                initialized = true;
            } catch (Exception sqliteEx) {
                System.err.println("Database init error: " + sqliteEx.getMessage());
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initDatabase();
        }
        if (isMySql) {
            String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } else {
            String sqliteUrl = "jdbc:sqlite:smart_campus_local.db";
            return DriverManager.getConnection(sqliteUrl);
        }
    }

    public static boolean testMySqlConnection(String host, String port, String name, String user, String pass) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            try (Connection c = DriverManager.getConnection(url, user, pass)) {
                return c != null && !c.isClosed();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static synchronized void reconnect(String host, String port, String name, String user, String pass) throws SQLException {
        saveConfig(host, port, name, user, pass);
        initialized = false;
        initDatabase();
    }

    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
            connection = null;
        }
    }

    public static String getConnectionMode() { return connectionMode; }
    public static boolean isUsingMySQL() { return isMySql; }

    public static String getDbHost() { return dbHost; }
    public static String getDbPort() { return dbPort; }
    public static String getDbName() { return dbName; }
    public static String getDbUser() { return dbUser; }
    public static String getDbPassword() { return dbPassword; }

    // Table auto-creation for MySQL
    private static void initializeMySqlTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(30) NOT NULL DEFAULT 'FACULTY', " +
                    "department VARCHAR(100), " +
                    "email VARCHAR(100), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS resources (" +
                    "resource_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(120) NOT NULL, " +
                    "category VARCHAR(50) NOT NULL, " +
                    "building VARCHAR(80) NOT NULL, " +
                    "floor_room VARCHAR(40) NOT NULL, " +
                    "capacity INT NOT NULL DEFAULT 1, " +
                    "power_rating_kw DOUBLE NOT NULL DEFAULT 2.0, " +
                    "status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', " +
                    "features TEXT)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS allocations (" +
                    "allocation_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "resource_id INT NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "purpose VARCHAR(255) NOT NULL, " +
                    "booking_date DATE NOT NULL, " +
                    "start_time TIME NOT NULL, " +
                    "end_time TIME NOT NULL, " +
                    "status VARCHAR(30) NOT NULL DEFAULT 'APPROVED', " +
                    "allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS telemetry_logs (" +
                    "log_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "resource_id INT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "occupancy_count INT NOT NULL DEFAULT 0, " +
                    "power_draw_kw DOUBLE NOT NULL DEFAULT 0.0, " +
                    "temperature_c DOUBLE NOT NULL DEFAULT 24.0, " +
                    "hvac_status VARCHAR(20) DEFAULT 'OFF', " +
                    "light_status VARCHAR(20) DEFAULT 'OFF', " +
                    "FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS maintenance_alerts (" +
                    "alert_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "resource_id INT NOT NULL, " +
                    "issue VARCHAR(255) NOT NULL, " +
                    "severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', " +
                    "reported_date DATE NOT NULL, " +
                    "is_resolved BOOLEAN DEFAULT FALSE, " +
                    "FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE)");

            seedInitialDataIfEmpty(conn, true);

        } catch (SQLException e) {
            System.err.println("Warning during MySQL table init: " + e.getMessage());
        }
    }

    // Table auto-creation for SQLite fallback
    private static void initializeSqliteTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'FACULTY', " +
                    "department TEXT, " +
                    "email TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS resources (" +
                    "resource_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "building TEXT NOT NULL, " +
                    "floor_room TEXT NOT NULL, " +
                    "capacity INTEGER NOT NULL DEFAULT 1, " +
                    "power_rating_kw REAL NOT NULL DEFAULT 2.0, " +
                    "status TEXT NOT NULL DEFAULT 'AVAILABLE', " +
                    "features TEXT)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS allocations (" +
                    "allocation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resource_id INTEGER NOT NULL, " +
                    "user_id INTEGER NOT NULL, " +
                    "purpose TEXT NOT NULL, " +
                    "booking_date DATE NOT NULL, " +
                    "start_time TIME NOT NULL, " +
                    "end_time TIME NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'APPROVED', " +
                    "allocated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS telemetry_logs (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resource_id INTEGER NOT NULL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "occupancy_count INTEGER NOT NULL DEFAULT 0, " +
                    "power_draw_kw REAL NOT NULL DEFAULT 0.0, " +
                    "temperature_c REAL NOT NULL DEFAULT 24.0, " +
                    "hvac_status TEXT DEFAULT 'OFF', " +
                    "light_status TEXT DEFAULT 'OFF')");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS maintenance_alerts (" +
                    "alert_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "resource_id INTEGER NOT NULL, " +
                    "issue TEXT NOT NULL, " +
                    "severity TEXT NOT NULL DEFAULT 'MEDIUM', " +
                    "reported_date DATE NOT NULL, " +
                    "is_resolved BOOLEAN DEFAULT 0)");

            seedInitialDataIfEmpty(conn, false);

        } catch (SQLException e) {
            System.err.println("Warning during SQLite table init: " + e.getMessage());
        }
    }

    private static void seedInitialDataIfEmpty(Connection conn, boolean isMysql) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println(" Seeding default campus records into database...");
                
                stmt.executeUpdate("INSERT INTO users (username, password, full_name, role, department, email) VALUES " +
                        "('admin', 'admin123', 'Campus Facility Director', 'ADMIN', 'Campus Administration', 'admin@smartcampus.edu')," +
                        "('sharma', 'sharma123', 'Dr. Rajesh Sharma', 'FACULTY', 'Computer Science & Engg', 'sharma@smartcampus.edu')," +
                        "('ananya', 'ananya123', 'Prof. Ananya Roy', 'FACULTY', 'Electronics & Comm', 'ananya@smartcampus.edu')," +
                        "('lucky', 'lucky123', 'Lucky Biswal (Student Lead)', 'STUDENT_COORDINATOR', 'Information Technology', 'lucky@smartcampus.edu')");

                stmt.executeUpdate("INSERT INTO resources (name, category, building, floor_room, capacity, power_rating_kw, status, features) VALUES " +
                        "('APJ Abdul Kalam Seminar Hall', 'SEMINAR_HALL', 'Main Admin Block', 'Ground Floor - Aud 1', 350, 12.5, 'AVAILABLE', 'Dolby Surround, Dual 4K Projectors, Smart Lighting, Central AC')," +
                        "('AI & Robotics Research Lab', 'LABORATORY', 'Tech Tower C', '3rd Floor - Lab 304', 60, 15.0, 'ALLOCATED', '30x RTX GPU Workstations, Robot Arm Testbeds, Fiber Optical Link')," +
                        "('Smart Lecture Theatre 101', 'SMART_CLASSROOM', 'Science Block A', '1st Floor - LT 101', 120, 4.5, 'AVAILABLE', 'Interactive Digital Whiteboard, Lecture Capture Camera, Wireless Mics')," +
                        "('IoT & Embedded Systems Lab', 'LABORATORY', 'Electronics Wing', '2nd Floor - Lab 208', 45, 6.2, 'AVAILABLE', 'Oscilloscopes, Sensor Dev Kits, Soldering Benches, 3D Printers')," +
                        "('Cloud Computing & Cyber Lab', 'LABORATORY', 'Tech Tower C', '4th Floor - Lab 402', 75, 11.0, 'AVAILABLE', 'Dual Xeon Servers, Virtualization Cluster, Gigabit Ethernet')," +
                        "('Smart Classroom 205', 'SMART_CLASSROOM', 'Academic Block B', '2nd Floor - CR 205', 80, 3.2, 'AVAILABLE', 'Short-throw Projector, Acoustic ceiling, Motorized Blinds')," +
                        "('High-End 4K Mobile Projector Unit', 'EQUIPMENT', 'Media Center', 'AV Depot - Slot 4', 1, 0.6, 'AVAILABLE', '4000 Lumens, Wireless HDMI, Battery Backup')," +
                        "('EV Campus Shuttle Fast Charger 1', 'EQUIPMENT', 'East Parking Bay', 'Bay Station 1', 2, 22.0, 'AVAILABLE', 'Type-2 Fast AC Charging, RFID Authentication')");

                // Sample allocations
                stmt.executeUpdate("INSERT INTO allocations (resource_id, user_id, purpose, booking_date, start_time, end_time, status) VALUES " +
                        "(2, 2, 'Hands-on Deep Learning Workshop', CURRENT_DATE, '09:00:00', '13:00:00', 'ACTIVE')," +
                        "(1, 1, 'Annual Tech Fest Keynote', CURRENT_DATE, '14:00:00', '17:30:00', 'APPROVED')");

                // Sample alerts
                stmt.executeUpdate("INSERT INTO maintenance_alerts (resource_id, issue, severity, reported_date, is_resolved) VALUES " +
                        "(6, 'Projector HDMI port loose connection', 'MEDIUM', CURRENT_DATE, 0)," +
                        "(8, 'Charger cable insulation routine check', 'LOW', CURRENT_DATE, 0)");
            }
        } catch (SQLException e) {
            System.err.println("Notice: Seeding check skipped: " + e.getMessage());
        }
    }
}
