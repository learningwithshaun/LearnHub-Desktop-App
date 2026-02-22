package za.ac.cput.prac50.dao;

import java.sql.*;
import java.util.*;
import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.za.cput.prac50.domain.UnifiedMembership;

/**
 * UNIFIED Membership Data Access Object
 * Consolidates functionality from: MembershipDAO (both versions)
 * Handles student group memberships
 * 
 * @author LearnHub Team - Consolidated Version
 */
public class UnifiedMembershipDAO {
    
    private UnifiedDBConnection dbConnection;
    private static final int DEFAULT_MAX_GROUP_SIZE = 10;
    
    /**
     * Constructor - initializes database connection
     */
    public UnifiedMembershipDAO() {
        this.dbConnection = UnifiedDBConnection.getInstance();
    }
    
    // ===== CREATE =====
    
    /**
     * Add a new membership (student joins a group)
     * @param membership Membership object
     * @return true if successful
     * @throws SQLException if database error or validation fails
     */
    public boolean addMembership(UnifiedMembership membership) throws SQLException {
        // Validation
        if (!membership.isValid()) {
            throw new IllegalArgumentException("Invalid membership data");
        }
        
        // Check if student already in group for this subject
        if (membershipExists(membership.getStudentId(), membership.getSubjectCode())) {
            throw new SQLException("Student already has a membership for subject " + membership.getSubjectCode());
        }
        
        // Check group capacity
        if (!hasGroupSpace(membership.getGroupId())) {
            throw new SQLException("Group is full (maximum " + DEFAULT_MAX_GROUP_SIZE + " members)");
        }
        
        String sql = "INSERT INTO memberships (student_id, group_id, subject_code) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, membership.getStudentId());
            stmt.setInt(2, membership.getGroupId());
            stmt.setString(3, membership.getSubjectCode());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get generated membership ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    membership.setMembershipId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding membership: " + e.getMessage());
            throw e;
        }
        return false;
    }
    
    // ===== READ =====
    
    /**
     * Get membership by student ID and subject code
     * @param studentId Student ID
     * @param subjectCode Subject code
     * @return Membership object or null if not found
     */
    public UnifiedMembership getMembership(String studentId, String subjectCode) {
        String sql = "SELECT * FROM memberships WHERE student_id = ? AND subject_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractMembershipFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting membership: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get membership by ID
     * @param membershipId Membership ID
     * @return Membership object or null if not found
     */
    public UnifiedMembership getMembershipById(int membershipId) {
        String sql = "SELECT * FROM memberships WHERE membership_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, membershipId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractMembershipFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting membership by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all memberships for a student
     * @param studentId Student ID
     * @return List of memberships
     */
    public List<UnifiedMembership> getAllMembershipsForStudent(String studentId) {
        List<UnifiedMembership> memberships = new ArrayList<>();
        String sql = "SELECT * FROM memberships WHERE student_id = ? ORDER BY subject_code";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                memberships.add(extractMembershipFromResultSet(rs));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting memberships for student: " + e.getMessage());
            e.printStackTrace();
        }
        return memberships;
    }
    
    /**
     * Get all members (students) in a specific group
     * @param groupId Group ID
     * @return List of student IDs in the group
     */
    public List<String> getGroupMembers(int groupId) {
        List<String> members = new ArrayList<>();
        String sql = "SELECT student_id FROM memberships WHERE group_id = ? ORDER BY joined_at";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                members.add(rs.getString("student_id"));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting group members: " + e.getMessage());
            e.printStackTrace();
        }
        return members;
    }
    
    /**
     * Get all members in a group for a specific subject (by subject code)
     * @param subjectCode Subject code
     * @param groupId Group ID
     * @return List of student IDs
     */
    public List<String> getGroupMembersBySubject(String subjectCode, int groupId) {
        List<String> members = new ArrayList<>();
        String sql = "SELECT student_id FROM memberships WHERE subject_code = ? AND group_id = ? ORDER BY joined_at";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            stmt.setInt(2, groupId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                members.add(rs.getString("student_id"));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting group members by subject: " + e.getMessage());
            e.printStackTrace();
        }
        return members;
    }
    
    /**
     * Get all memberships for a specific group
     * @param groupId Group ID
     * @return List of memberships
     */
    public List<UnifiedMembership> getMembershipsForGroup(int groupId) {
        List<UnifiedMembership> memberships = new ArrayList<>();
        String sql = "SELECT * FROM memberships WHERE group_id = ? ORDER BY joined_at";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                memberships.add(extractMembershipFromResultSet(rs));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting memberships for group: " + e.getMessage());
            e.printStackTrace();
        }
        return memberships;
    }
    
    // ===== UPDATE =====
    
    /**
     * Update membership (move student to different group)
     * @param membership Membership with updated group ID
     * @return true if successful
     * @throws SQLException if validation fails
     */
    public boolean updateMembership(UnifiedMembership membership) throws SQLException {
        // Check if membership exists
        if (!membershipExists(membership.getStudentId(), membership.getSubjectCode())) {
            throw new SQLException("Membership does not exist");
        }
        
        // Check new group capacity
        if (!hasGroupSpace(membership.getGroupId())) {
            throw new SQLException("Cannot move to group - group is full");
        }
        
        String sql = "UPDATE memberships SET group_id = ? WHERE student_id = ? AND subject_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, membership.getGroupId());
            stmt.setString(2, membership.getStudentId());
            stmt.setString(3, membership.getSubjectCode());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating membership: " + e.getMessage());
            throw e;
        }
    }
    
    // ===== DELETE =====
    
    /**
     * Delete membership (student leaves group)
     * @param studentId Student ID
     * @param subjectCode Subject code
     * @return true if successful
     */
    public boolean deleteMembership(String studentId, String subjectCode) {
        String sql = "DELETE FROM memberships WHERE student_id = ? AND subject_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, subjectCode);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting membership: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Remove membership by ID
     * @param membershipId Membership ID
     * @return true if successful
     */
    public boolean deleteMembershipById(int membershipId) {
        String sql = "DELETE FROM memberships WHERE membership_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, membershipId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting membership by ID: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== VALIDATION & CHECKS =====
    
    /**
     * Check if membership exists
     * @param studentId Student ID
     * @param subjectCode Subject code
     * @return true if membership exists
     */
    public boolean membershipExists(String studentId, String subjectCode) {
        String sql = "SELECT COUNT(*) FROM memberships WHERE student_id = ? AND subject_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setString(2, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking membership existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get group size (number of members)
     * @param groupId Group ID
     * @return Number of members in group
     */
    public int getGroupSize(int groupId) {
        String sql = "SELECT COUNT(*) FROM memberships WHERE group_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting group size: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Check if group has space for new members
     * @param groupId Group ID
     * @return true if group has space
     */
    public boolean hasGroupSpace(int groupId) {
        return getGroupSize(groupId) < DEFAULT_MAX_GROUP_SIZE;
    }
    
    /**
     * Check if group has space (with custom max)
     * @param groupId Group ID
     * @param maxMembers Maximum members allowed
     * @return true if group has space
     */
    public boolean hasGroupSpace(int groupId, int maxMembers) {
        return getGroupSize(groupId) < maxMembers;
    }
    
    /**
     * Get group statistics for a subject
     * @param subjectCode Subject code
     * @return Map of group IDs to member counts
     */
    public Map<Integer, Integer> getGroupStatistics(String subjectCode) {
        Map<Integer, Integer> statistics = new HashMap<>();
        String sql = "SELECT group_id, COUNT(*) as member_count FROM memberships " +
                    "WHERE subject_code = ? GROUP BY group_id ORDER BY group_id";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                statistics.put(rs.getInt("group_id"), rs.getInt("member_count"));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting group statistics: " + e.getMessage());
            e.printStackTrace();
        }
        return statistics;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Extract Membership object from ResultSet
     * @param rs ResultSet from query
     * @return Membership object
     * @throws SQLException if database error occurs
     */
    private UnifiedMembership extractMembershipFromResultSet(ResultSet rs) throws SQLException {
        return new UnifiedMembership(
            rs.getInt("membership_id"),
            rs.getString("student_id"),
            rs.getInt("group_id"),
            rs.getString("subject_code"),
            rs.getTimestamp("joined_at")
        );
    }
    
    /**
     * Get maximum group size constant
     * @return Maximum group size
     */
    public int getMaxGroupSize() {
        return DEFAULT_MAX_GROUP_SIZE;
    }
    
    /**
     * Get total number of memberships
     * @return Total membership count
     */
    public int getTotalMembershipCount() {
        String sql = "SELECT COUNT(*) FROM memberships";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting membership count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Close DAO (for compatibility with old code)
     */
    public void close() {
        // Connection is managed by UnifiedDBConnection singleton
        // No action needed here
    }
}