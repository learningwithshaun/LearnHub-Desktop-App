package za.ac.za.cput.prac50.domain;

import java.sql.Timestamp;

/**
 * UNIFIED Membership Domain Class
 * Consolidates: Membership.java and Membership2.java
 * 
 * @author LearnHub Team - Consolidated Version
 */
public class UnifiedMembership {
    // Fields from both Membership classes
    private int membershipId;          // Auto-generated ID
    private String studentId;          // Student identifier
    private int groupId;               // Group identifier  
    private String subjectCode;        // Subject code (e.g., "ADF262S")
    private Timestamp joinedAt;        // When student joined the group
    
    // ===== CONSTRUCTORS =====
    
    /**
     * Default constructor
     */
    public UnifiedMembership() {
        this.joinedAt = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Constructor with student ID, group ID, and subject code
     * Used when creating new memberships
     */
    public UnifiedMembership(String studentId, int groupId, String subjectCode) {
        this.studentId = studentId;
        this.groupId = groupId;
        this.subjectCode = subjectCode;
        this.joinedAt = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * Full constructor (used when loading from database)
     */
    public UnifiedMembership(int membershipId, String studentId, int groupId, 
                            String subjectCode, Timestamp joinedAt) {
        this.membershipId = membershipId;
        this.studentId = studentId;
        this.groupId = groupId;
        this.subjectCode = subjectCode;
        this.joinedAt = joinedAt;
    }
    
    /**
     * Constructor compatible with old Membership2 (student name, subject, group name)
     * Note: This requires conversion to proper IDs in the DAO layer
     */
    public UnifiedMembership(String studentId, String subjectCode, int groupId) {
        this(studentId, groupId, subjectCode);
    }
    
    // ===== GETTERS =====
    
    public int getMembershipId() {
        return membershipId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    // Alias for old code compatibility
    public String getStudent() {
        return studentId;
    }
    
    public int getGroupId() {
        return groupId;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    // Alias for old code compatibility
    public String getSubject() {
        return subjectCode;
    }
    
    public Timestamp getJoinedAt() {
        return joinedAt;
    }
    
    // ===== SETTERS =====
    
    public void setMembershipId(int membershipId) {
        this.membershipId = membershipId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    // Alias for old code compatibility
    public void setStudent(String studentId) {
        this.studentId = studentId;
    }
    
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    // Alias for old code compatibility
    public void setSubject(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Check if membership is valid (has required fields)
     * @return true if all required fields are present
     */
    public boolean isValid() {
        return studentId != null && !studentId.trim().isEmpty() &&
               groupId > 0 &&
               subjectCode != null && !subjectCode.trim().isEmpty();
    }
    
    /**
     * Get membership age in days
     * @return Number of days since joining
     */
    public long getDaysSinceJoined() {
        if (joinedAt == null) return 0;
        
        long diffInMillis = System.currentTimeMillis() - joinedAt.getTime();
        return diffInMillis / (1000 * 60 * 60 * 24);
    }
    
    /**
     * Check if membership is recent (less than 7 days old)
     * @return true if membership is less than 7 days old
     */
    public boolean isRecentMembership() {
        return getDaysSinceJoined() < 7;
    }
    
    // ===== OVERRIDES =====
    
    @Override
    public String toString() {
        return "Membership{" +
                "membershipId=" + membershipId +
                ", studentId='" + studentId + '\'' +
                ", groupId=" + groupId +
                ", subjectCode='" + subjectCode + '\'' +
                ", joinedAt=" + joinedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UnifiedMembership that = (UnifiedMembership) obj;
        
        // Two memberships are equal if they have the same student and subject
        // (since a student can only be in one group per subject)
        return studentId != null && studentId.equals(that.studentId) &&
               subjectCode != null && subjectCode.equals(that.subjectCode);
    }
    
    @Override
    public int hashCode() {
        int result = studentId != null ? studentId.hashCode() : 0;
        result = 31 * result + (subjectCode != null ? subjectCode.hashCode() : 0);
        return result;
    }
}
