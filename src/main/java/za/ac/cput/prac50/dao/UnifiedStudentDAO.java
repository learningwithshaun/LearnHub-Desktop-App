package za.ac.cput.prac50.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * UNIFIED Student Data Access Object
 * Consolidates functionality from: LearnHubDatabase, StudyLocationDBDemo
 * 
 * @author LearnHub Team - Consolidated Version
 */
public class UnifiedStudentDAO {
    
    private UnifiedDBConnection dbConnection;
    
    /**
     * Constructor - initializes database connection
     */
    public UnifiedStudentDAO() {
        this.dbConnection = UnifiedDBConnection.getInstance();
    }
    
    // ===== CREATE =====
    
    /**
     * Add a new student to the database
     * @param student Student object to add
     * @return true if successful
     */
    public boolean addStudent(UnifiedStudent student) {
        String sql = "INSERT INTO students (student_id, first_name, last_name, email, password, student_year, stream, course) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getStudentId());
            stmt.setString(2, student.getFirstName());
            stmt.setString(3, student.getLastName());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getPassword());
            stmt.setString(6, student.getYear());
            stmt.setString(7, student.getStream());
            stmt.setString(8, student.getCourse());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Register a new student (includes security question)
     * @param student Student object
     * @param securityQuestion Security question
     * @param securityAnswer Security answer
     * @return true if successful
     */
    public boolean registerStudent(UnifiedStudent student, String securityQuestion, String securityAnswer) {
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert student
            String studentSQL = "INSERT INTO students (student_id, first_name, last_name, email, password, student_year, stream, course) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(studentSQL)) {
                stmt.setString(1, student.getStudentId());
                stmt.setString(2, student.getFirstName());
                stmt.setString(3, student.getLastName());
                stmt.setString(4, student.getEmail());
                stmt.setString(5, student.getPassword());
                stmt.setString(6, student.getYear());
                stmt.setString(7, student.getStream());
                stmt.setString(8, student.getCourse());
                stmt.executeUpdate();
            }
            
            // Insert security question
            String securitySQL = "INSERT INTO security_questions (student_id, question, answer) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(securitySQL)) {
                stmt.setString(1, student.getStudentId());
                stmt.setString(2, securityQuestion);
                stmt.setString(3, securityAnswer);
                stmt.executeUpdate();
            }
            
            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Error registering student: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // ===== READ =====
    
    /**
     * Get student by ID
     * @param studentId Student ID
     * @return Student object or null if not found
     */
    public UnifiedStudent getStudentById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting student: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get student by email
     * @param email Student email
     * @return Student object or null if not found
     */
    public UnifiedStudent getStudentByEmail(String email) {
        String sql = "SELECT * FROM students WHERE email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting student by email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all students
     * @return List of all students
     */
    public List<UnifiedStudent> getAllStudents() {
        List<UnifiedStudent> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all students: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }
    
    /**
     * Load current student (for demo - uses first student or default)
     * @return Current student object
     */
    public UnifiedStudent loadCurrentStudent() {
        String sql = "SELECT * FROM students ORDER BY student_id FETCH FIRST 1 ROW ONLY";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading current student: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Return default student if none found
        return new UnifiedStudent("230145623", "John", "Doe", "example@mycput.ac.za");
    }
    
    /**
     * Get students by year
     * @param year Year to filter by
     * @return List of students in that year
     */
    public List<UnifiedStudent> getStudentsByYear(String year) {
        List<UnifiedStudent> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE student_year = ? ORDER BY last_name, first_name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, year);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting students by year: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }
    
    /**
     * Get students by stream
     * @param stream Stream to filter by
     * @return List of students in that stream
     */
    public List<UnifiedStudent> getStudentsByStream(String stream) {
        List<UnifiedStudent> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE stream = ? ORDER BY last_name, first_name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, stream);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting students by stream: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }
    
    // ===== UPDATE =====
    
    /**
     * Update student information
     * @param student Student object with updated information
     * @return true if successful
     */
    public boolean updateStudent(UnifiedStudent student) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ?, " +
                    "password = ?, student_year = ?, stream = ?, course = ? WHERE student_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setString(3, student.getEmail());
            stmt.setString(4, student.getPassword());
            stmt.setString(5, student.getYear());
            stmt.setString(6, student.getStream());
            stmt.setString(7, student.getCourse());
            stmt.setString(8, student.getStudentId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update student password
     * @param studentId Student ID
     * @param newPassword New password
     * @return true if successful
     */
    public boolean updatePassword(String studentId, String newPassword) {
        String sql = "UPDATE students SET password = ? WHERE student_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPassword);
            stmt.setString(2, studentId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Reset password using security question
     * @param email Student email
     * @param securityAnswer Security answer
     * @param newPassword New password
     * @return true if successful
     */
    public boolean resetPassword(String email, String securityAnswer, String newPassword) {
        String sql = "UPDATE students s SET s.password = ? " +
                    "WHERE s.email = ? AND EXISTS (" +
                    "SELECT 1 FROM security_questions sq " +
                    "WHERE sq.student_id = s.student_id AND UPPER(sq.answer) = UPPER(?))";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            stmt.setString(3, securityAnswer);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== DELETE =====
    
    /**
     * Delete student by ID
     * @param studentId Student ID
     * @return true if successful
     */
    public boolean deleteStudent(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== AUTHENTICATION =====
    
    /**
     * Authenticate student with email and password
     * @param email Student email
     * @param password Password
     * @return Student object if authenticated, null otherwise
     */
    public UnifiedStudent authenticate(String email, String password) {
        String sql = "SELECT * FROM students WHERE email = ? AND password = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error authenticating student: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Authenticate student with student ID and password
     * @param studentId Student ID
     * @param password Password
     * @return Student object if authenticated, null otherwise
     */
    public UnifiedStudent authenticateById(String studentId, String password) {
        String sql = "SELECT * FROM students WHERE student_id = ? AND password = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error authenticating student: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Check if email exists in database
     * @param email Email to check
     * @return true if email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM students WHERE email = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if student ID exists in database
     * @param studentId Student ID to check
     * @return true if student ID exists
     */
    public boolean studentIdExists(String studentId) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking student ID: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Extract Student object from ResultSet
     * @param rs ResultSet from query
     * @return Student object
     * @throws SQLException if database error occurs
     */
    private UnifiedStudent extractStudentFromResultSet(ResultSet rs) throws SQLException {
        UnifiedStudent student = new UnifiedStudent();
        student.setStudentId(rs.getString("student_id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));
        student.setPassword(rs.getString("password"));
        student.setYear(rs.getString("student_year"));
        student.setStream(rs.getString("stream"));
        student.setCourse(rs.getString("course"));
        return student;
    }
    
    /**
     * Test database connection
     * @return true if connection is active
     */
    public boolean testConnection() {
        return dbConnection.testConnection();
    }
    
    /**
     * Get count of all students
     * @return Number of students in database
     */
    public int getStudentCount() {
        String sql = "SELECT COUNT(*) FROM students";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting student count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Shutdown database connection
     */
    public void shutdown() {
        dbConnection.shutdown();
    }
}
