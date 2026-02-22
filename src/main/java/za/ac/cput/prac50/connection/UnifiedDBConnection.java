package za.ac.cput.prac50.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * UNIFIED Database Connection for LearnHub Application
 * Consolidates all database operations into ONE Derby database
 * * @author LearnHub Team - Consolidated Version
 */
public class UnifiedDBConnection {
    // SINGLE DATABASE CONFIGURATION
    private static final String DB_URL = "jdbc:derby://localhost:1527/LearnHubUnified;create=true";
    private static final String DB_USER = "learnhub";
    private static final String DB_PASSWORD = "learnhub123";
    private static final String DRIVER = "org.apache.derby.jdbc.ClientDriver";
    
    private static UnifiedDBConnection instance;
    private Connection connection;
    
    /**
     * Private constructor for Singleton pattern
     */
    private UnifiedDBConnection() {
        initializeDatabase();
    }
    
    /**
     * Get singleton instance of database connection
     * @return UnifiedDBConnection instance
     */
    public static synchronized UnifiedDBConnection getInstance() {
        if (instance == null) {
            instance = new UnifiedDBConnection();
        }
        return instance;
    }
    
    /**
     * Initialize database and create all required tables
     */
    private void initializeDatabase() {
        try {
            // Load Derby driver
            Class.forName(DRIVER);
            System.out.println("✓ Derby driver loaded successfully");
            
            // Create connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Database connection established: " + DB_URL);
            
            // Create all tables
            createAllTables();
            
            // Insert sample data
            insertSampleData();
            
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Derby driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create all required tables for the unified database
     */
    private void createAllTables() {
        try (Statement stmt = connection.createStatement()) {
            
            // 1. STUDENTS TABLE (Unified from Student, Student1, Student3, Student4)
            createTableIfNotExists(stmt, "students",
                "CREATE TABLE students (" +
                "student_id VARCHAR(20) PRIMARY KEY, " +
                "first_name VARCHAR(50) NOT NULL, " +
                "last_name VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(100) NOT NULL, " +
                "student_year VARCHAR(20), " +
                "stream VARCHAR(50), " +
                "course VARCHAR(100), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // 2. SUBJECTS TABLE
            createTableIfNotExists(stmt, "subjects",
                "CREATE TABLE subjects (" +
                "subject_code VARCHAR(20) PRIMARY KEY, " +
                "module_name VARCHAR(100) NOT NULL, " +
                "credits INT NOT NULL, " +
                "subject_year VARCHAR(20), " +
                "stream VARCHAR(50)" +
                ")"
            );
            
            // 3. STUDY GROUPS TABLE - WITH VENUE LINK (Part 3)
            createTableIfNotExists(stmt, "study_groups",
                "CREATE TABLE study_groups (" +
                "group_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "group_name VARCHAR(100) NOT NULL, " +
                "description VARCHAR(500), " +
                "subject_code VARCHAR(20) NOT NULL, " +
                "group_year VARCHAR(20), " +
                "stream VARCHAR(50), " +
                "max_members INT DEFAULT 10, " +
                "venue_id INT, " +  // ✅ NEW: Link to venue where group meets
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (subject_code) REFERENCES subjects(subject_code), " +
                "FOREIGN KEY (venue_id) REFERENCES venues(venue_id), " +  // ✅ NEW
                "UNIQUE (group_name, subject_code)" +
                ")"
            );
            
            // 4. MEMBERSHIPS TABLE (Unified from Membership and Membership2)
            createTableIfNotExists(stmt, "memberships",
                "CREATE TABLE memberships (" +
                "membership_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "student_id VARCHAR(20) NOT NULL, " +
                "group_id INT NOT NULL, " +
                "subject_code VARCHAR(20) NOT NULL, " +
                "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (group_id) REFERENCES study_groups(group_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (subject_code) REFERENCES subjects(subject_code), " +
                "UNIQUE (student_id, subject_code)" +
                ")"
            );
            
            // 5. STUDY LOCATIONS TABLE
            createTableIfNotExists(stmt, "study_locations",
                "CREATE TABLE study_locations (" +
                "location_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "group_name VARCHAR(100) NOT NULL, " +
                "location_type VARCHAR(50), " +
                "building_name VARCHAR(100), " +
                "latitude DOUBLE NOT NULL, " +
                "longitude DOUBLE NOT NULL, " +
                "capacity INT DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // 6. VENUES TABLE (Study Points) - WITH COORDINATES (Part 1)
            createTableIfNotExists(stmt, "venues",
                "CREATE TABLE venues (" +
                "venue_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "venue_name VARCHAR(100) NOT NULL UNIQUE, " +
                "capacity INT NOT NULL, " +
                "current_usage INT DEFAULT 0, " +
                "study_types VARCHAR(200), " +
                "operating_hours VARCHAR(50), " +
                "amenities VARCHAR(500), " +
                "latitude DOUBLE, " +  // ✅ NEW: Add coordinates
                "longitude DOUBLE, " +  // ✅ NEW: Add coordinates
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // 7. USER LOCATION TABLE (for map preferences)
            createTableIfNotExists(stmt, "user_location",
                "CREATE TABLE user_location (" +
                "id INT PRIMARY KEY, " +
                "latitude DOUBLE NOT NULL, " +
                "longitude DOUBLE NOT NULL, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // 8. SECURITY QUESTIONS TABLE (for password reset)
            createTableIfNotExists(stmt, "security_questions",
                "CREATE TABLE security_questions (" +
                "question_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "student_id VARCHAR(20) NOT NULL, " +
                "question VARCHAR(200) NOT NULL, " +
                "answer VARCHAR(200) NOT NULL, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE" +
                ")"
            );
            
            // 9. MESSAGES TABLE
            createTableIfNotExists(stmt, "messages",
                "CREATE TABLE messages (" +
                "message_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "group_id INT NOT NULL, " +
                "student_id VARCHAR(20) NOT NULL, " +
                "message_content VARCHAR(1000) NOT NULL, " +
                "message_type VARCHAR(20) DEFAULT 'TEXT', " +
                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (group_id) REFERENCES study_groups(group_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE" +
                ")"
            );
            
            // 10. NOTIFICATIONS TABLE
            createTableIfNotExists(stmt, "notifications",
                "CREATE TABLE notifications (" +
                "notification_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, " +
                "student_id VARCHAR(20) NOT NULL, " +
                "message VARCHAR(500) NOT NULL, " +
                "is_read BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE" +
                ")"
            );

        } catch (SQLException e) {
            System.err.println("✗ Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to check and create tables
     */
    private void createTableIfNotExists(Statement stmt, String tableName, String createSQL) throws SQLException {
        try {
            stmt.execute("SELECT * FROM " + tableName + " WHERE 1=0");
            // If select succeeds, table exists.
            System.out.println("✓ Table exists: " + tableName);
        } catch (SQLException e) {
            if ("42X05".equals(e.getSQLState())) {
                stmt.executeUpdate(createSQL);
                System.out.println("✓ Table created: " + tableName);
            } else {
                throw e;
            }
        }
    }
    
    /**
     * Insert sample data into all tables if empty
     */
    private void insertSampleData() {
        // ... Assuming existing sample data insertion for other tables runs here ...
        
        insertSampleVenueData(); // (Part 2 - Call)
        
        // --- ADD SAMPLE DATA TO LINK GROUPS TO VENUES ---
        try {
            // Check if groups have venue links before inserting sample links
            String checkSQL = "SELECT COUNT(venue_id) FROM study_groups WHERE venue_id IS NOT NULL";
            ResultSet rs = connection.createStatement().executeQuery(checkSQL);
            rs.next();
            
            // Assuming venue_id 1 = 'D6 Library Seminar Room', 2 = 'Engineering Building Room 1.19', etc.
            if (rs.getInt(1) == 0) {
                 // Link group names (from sample data) to their respective new venue IDs (1-based identity)
                 connection.createStatement().executeUpdate("UPDATE study_groups SET venue_id = 1 WHERE group_name = 'ADF262S Study Group'");
                 connection.createStatement().executeUpdate("UPDATE study_groups SET venue_id = 1 WHERE group_name = 'MAF261S Group'");
                 connection.createStatement().executeUpdate("UPDATE study_groups SET venue_id = 2 WHERE group_name = 'PRJ262S Team'");
                 connection.createStatement().executeUpdate("UPDATE study_groups SET venue_id = 4 WHERE group_name = 'CNF260S Network Group'");
                 
                 System.out.println("✅ Sample venue links inserted into study_groups");
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("⚠ Note linking sample groups to venues: " + e.getMessage());
        }
    }

    // ===== PART 2: Add sample venue data with coordinates in UnifiedDBConnection.java =====

    private void insertSampleVenueData() {
        try {
            // Check if venues exist
            String checkSQL = "SELECT COUNT(*) FROM venues";
            ResultSet rs = connection.createStatement().executeQuery(checkSQL);
            rs.next();
            
            if (rs.getInt(1) == 0) {
                // Insert sample venues WITH coordinates
                String[] venueInserts = {
                    "INSERT INTO venues (venue_name, capacity, current_usage, study_types, operating_hours, amenities, latitude, longitude) " +
                    "VALUES ('D6 Library Seminar Room', 25, 8, 'Silent Study, Individual Study', '07:00 - 22:00', " +
                    "'Wi-Fi, Power Outlets, Air Conditioning', -33.93050, 18.43081)",
                    
                    "INSERT INTO venues (venue_name, capacity, current_usage, study_types, operating_hours, amenities, latitude, longitude) " +
                    "VALUES ('Engineering Building Room 1.19', 50, 15, 'Group Discussion, Silent Study', '07:00 - 22:00', " +
                    "'Wi-Fi, Power Outlets, Whiteboard, Projector', -33.93087, 18.42936)",
                    
                    "INSERT INTO venues (venue_name, capacity, current_usage, study_types, operating_hours, amenities, latitude, longitude) " +
                    "VALUES ('E-Learning Center', 30, 5, 'Individual Study, Computer Lab', '08:00 - 20:00', " +
                    "'Wi-Fi, Computers, Power Outlets', -33.92893, 18.42840)",
                    
                    "INSERT INTO venues (venue_name, capacity, current_usage, study_types, operating_hours, amenities, latitude, longitude) " +
                    "VALUES ('Commerce Building Study Room', 20, 3, 'Group Discussion, Presentation Practice', '07:00 - 19:00', " +
                    "'Wi-Fi, Power Outlets, Whiteboard', -33.93020, 18.42946)"
                };
                
                for (String sql : venueInserts) {
                    connection.createStatement().executeUpdate(sql);
                }
                
                System.out.println("✅ Sample venue data with coordinates inserted");
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("⚠ Note inserting venue data: " + e.getMessage());
        }
    }

    /**
     * Get the active database connection
     * @return Connection object
     * @throws SQLException 
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }
    
    /**
     * Test if connection is active
     * @return true if connection is valid
     */
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Shutdown Derby database (call when application exits)
     */
    public void shutdown() {
        try {
            closeConnection();
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // Expected behavior - Derby throws exception on shutdown
            if (e.getSQLState().equals("XJ015")) {
                System.out.println("✓ Database shut down successfully");
            }
        }
    }
}