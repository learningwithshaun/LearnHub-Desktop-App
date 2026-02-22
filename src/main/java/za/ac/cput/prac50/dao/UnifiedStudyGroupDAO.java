package za.ac.cput.prac50.dao;

import java.sql.*;
import java.util.*;
import za.ac.cput.prac50.connection.UnifiedDBConnection;
import za.ac.za.cput.prac50.domain.StudyGroup;

/**
 * UNIFIED Study Group Data Access Object
 * Consolidates functionality from: GroupRegistrationDAO
 * 
 * @author LearnHub Team - Consolidated Version
 */
public class UnifiedStudyGroupDAO {
    
    private UnifiedDBConnection dbConnection;
    
    /**
     * Constructor - initializes database connection
     */
    public UnifiedStudyGroupDAO() {
        this.dbConnection = UnifiedDBConnection.getInstance();
    }
    
    // ===== CREATE =====
    
    /**
     * Create a new study group
     * @param group StudyGroup object
     * @return true if successful
     * @throws SQLException if validation fails or database error
     */
    public boolean createGroup(StudyGroup group) throws SQLException {
        // Validation: Check if group name exists for this subject
        if (groupNameExistsForSubject(group.getGroupName(), group.getSubjectCode())) {
            throw new SQLException("Group name already exists for this subject");
        }
        
        // Validation: Check if subject exists
        if (!subjectExists(group.getSubjectCode())) {
            throw new SQLException("Invalid subject code");
        }
        
        String sql = "INSERT INTO study_groups (group_name, description, subject_code, group_year, stream, max_members) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, group.getGroupName());
            stmt.setString(2, group.getDescription());
            stmt.setString(3, group.getSubjectCode());
            stmt.setString(4, group.getYear());
            stmt.setString(5, group.getStream());
            stmt.setInt(6, group.getMaxMembers());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get generated group ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    group.setGroupId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating group: " + e.getMessage());
            throw e;
        }
        return false;
    }
    
    // ===== READ =====
    
    /**
     * Get study group by ID
     * @param groupId Group ID
     * @return StudyGroup object or null if not found
     */
    public StudyGroup getGroupById(int groupId) {
        String sql = "SELECT sg.*, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as current_members " +
                    "FROM study_groups sg WHERE sg.group_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractStudyGroupFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting group by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get study group by name and subject code
     * @param groupName Group name
     * @param subjectCode Subject code
     * @return StudyGroup object or null if not found
     */
    public StudyGroup getGroupByNameAndSubject(String groupName, String subjectCode) {
        String sql = "SELECT sg.*, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as current_members " +
                    "FROM study_groups sg WHERE sg.group_name = ? AND sg.subject_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, groupName);
            stmt.setString(2, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractStudyGroupFromResultSet(rs);
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting group by name: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all study groups
     * @return List of all study groups
     */
    public List<StudyGroup> getAllGroups() {
        List<StudyGroup> groups = new ArrayList<>();
        String sql = "SELECT sg.*, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as current_members " +
                    "FROM study_groups sg ORDER BY sg.group_name";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                groups.add(extractStudyGroupFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all groups: " + e.getMessage());
            e.printStackTrace();
        }
        return groups;
    }
    
    /**
     * Get groups by subject code
     * @param subjectCode Subject code
     * @return List of groups for that subject
     */
    public List<StudyGroup> getGroupsBySubject(String subjectCode) {
        List<StudyGroup> groups = new ArrayList<>();
        String sql = "SELECT sg.*, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as current_members " +
                    "FROM study_groups sg WHERE sg.subject_code = ? ORDER BY sg.group_name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                groups.add(extractStudyGroupFromResultSet(rs));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting groups by subject: " + e.getMessage());
            e.printStackTrace();
        }
        return groups;
    }
    
    /**
     * Get groups by year
     * @param year Year (e.g., "First Year", "Second Year")
     * @return List of groups for that year
     */
    public List<StudyGroup> getGroupsByYear(String year) {
        List<StudyGroup> groups = new ArrayList<>();
        String sql = "SELECT sg.*, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as current_members " +
                    "FROM study_groups sg WHERE sg.group_year = ? ORDER BY sg.group_name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, year);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                groups.add(extractStudyGroupFromResultSet(rs));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting groups by year: " + e.getMessage());
            e.printStackTrace();
        }
        return groups;
    }
    
    /**
     * Get groups by stream
     * @param stream Stream (e.g., "Application Development")
     * @return List of groups for that stream
     */
    public List<StudyGroup> getGroupsByStream(String stream) {
        List<StudyGroup> groups = new ArrayList<>();
        String sql = "SELECT sg.*, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as current_members " +
                    "FROM study_groups sg WHERE sg.stream = ? ORDER BY sg.group_name";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, stream);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                groups.add(extractStudyGroupFromResultSet(rs));
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting groups by stream: " + e.getMessage());
            e.printStackTrace();
        }
        return groups;
    }
    
    /**
     * Get available groups (groups with space)
     * @return List of groups with available space
     */
    public List<StudyGroup> getAvailableGroups() {
        List<StudyGroup> groups = new ArrayList<>();
        String sql = "SELECT sg.*, " +
                    "COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) as current_members " +
                    "FROM study_groups sg " +
                    "WHERE COALESCE((SELECT COUNT(*) FROM memberships m WHERE m.group_id = sg.group_id), 0) < sg.max_members " +
                    "ORDER BY sg.group_name";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                groups.add(extractStudyGroupFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting available groups: " + e.getMessage());
            e.printStackTrace();
        }
        return groups;
    }
    
    // ===== UPDATE =====
    
    /**
     * Update study group information
     * @param group StudyGroup object with updated info
     * @return true if successful
     */
    public boolean updateGroup(StudyGroup group) {
        String sql = "UPDATE study_groups SET group_name = ?, description = ?, " +
                    "subject_code = ?, group_year = ?, stream = ?, max_members = ? " +
                    "WHERE group_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, group.getGroupName());
            stmt.setString(2, group.getDescription());
            stmt.setString(3, group.getSubjectCode());
            stmt.setString(4, group.getYear());
            stmt.setString(5, group.getStream());
            stmt.setInt(6, group.getMaxMembers());
            stmt.setInt(7, group.getGroupId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating group: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update group description
     * @param groupId Group ID
     * @param description New description
     * @return true if successful
     */
    public boolean updateGroupDescription(int groupId, String description) {
        String sql = "UPDATE study_groups SET description = ? WHERE group_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, description);
            stmt.setInt(2, groupId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating group description: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update group max members
     * @param groupId Group ID
     * @param maxMembers New max members
     * @return true if successful
     */
    public boolean updateGroupMaxMembers(int groupId, int maxMembers) {
        String sql = "UPDATE study_groups SET max_members = ? WHERE group_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maxMembers);
            stmt.setInt(2, groupId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating group max members: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== DELETE =====
    
    /**
     * Delete study group
     * @param groupId Group ID
     * @return true if successful
     */
    public boolean deleteGroup(int groupId) {
        String sql = "DELETE FROM study_groups WHERE group_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, groupId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting group: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ===== VALIDATION & CHECKS =====
    
    /**
     * Check if group name exists for a subject
     * @param groupName Group name
     * @param subjectCode Subject code
     * @return true if exists
     */
    public boolean groupNameExistsForSubject(String groupName, String subjectCode) {
        String sql = "SELECT COUNT(*) FROM study_groups WHERE group_name = ? AND subject_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, groupName);
            stmt.setString(2, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking group name: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if subject exists in database
     * @param subjectCode Subject code
     * @return true if exists
     */
    public boolean subjectExists(String subjectCode) {
        String sql = "SELECT COUNT(*) FROM subjects WHERE subject_code = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subjectCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking subject: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get current membership count for a group
     * @param groupId Group ID
     * @return Number of members
     */
    public int getGroupMembershipCount(int groupId) {
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
            System.err.println("Error getting membership count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Check if group is full
     * @param groupId Group ID
     * @return true if group is at capacity
     */
    public boolean isGroupFull(int groupId) {
        StudyGroup group = getGroupById(groupId);
        if (group != null) {
            return group.getCurrentMembers() >= group.getMaxMembers();
        }
        return false;
    }
    
    /**
     * Check if student is already in a group for this subject
     * @param studentId Student ID
     * @param groupId Group ID
     * @return true if student is in the group
     */
    public boolean isStudentInGroup(String studentId, int groupId) {
        String sql = "SELECT COUNT(*) FROM memberships WHERE student_id = ? AND group_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            stmt.setInt(2, groupId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            rs.close();
            
        } catch (SQLException e) {
            System.err.println("Error checking student in group: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * Extract StudyGroup object from ResultSet
     * @param rs ResultSet from query
     * @return StudyGroup object
     * @throws SQLException if database error occurs
     */
    private StudyGroup extractStudyGroupFromResultSet(ResultSet rs) throws SQLException {
        StudyGroup group = new StudyGroup();
        group.setGroupId(rs.getInt("group_id"));
        group.setGroupName(rs.getString("group_name"));
        group.setDescription(rs.getString("description"));
        group.setSubjectCode(rs.getString("subject_code"));
        group.setYear(rs.getString("group_year"));
        group.setStream(rs.getString("stream"));
        group.setMaxMembers(rs.getInt("max_members"));
        group.setCurrentMembers(rs.getInt("current_members"));
        return group;
    }
    
    /**
     * Get total number of groups
     * @return Total group count
     */
    public int getTotalGroupCount() {
        String sql = "SELECT COUNT(*) FROM study_groups";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting group count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}