package za.ac.cput.prac50.gui;

import za.ac.za.cput.prac50.domain.UnifiedStudent;

/**
 * Session Manager - Singleton Pattern
 * Tracks the currently logged-in student across all screens
 * 
 * @author LearnHub Team
 */
public class SessionManager {
    
    private static SessionManager instance;
    private UnifiedStudent currentStudent;
    
    /**
     * Private constructor for Singleton pattern
     */
    private SessionManager() {
        this.currentStudent = null;
    }
    
    /**
     * Get singleton instance
     * @return SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Set the currently logged-in student
     * Called after successful login
     * @param student The logged-in student
     */
    public void setCurrentStudent(UnifiedStudent student) {
        this.currentStudent = student;
        System.out.println("✓ Session started for: " + student.getFullName() + " (" + student.getStudentId() + ")");
    }
    
    /**
     * Get the currently logged-in student
     * @return Current student or null if no one is logged in
     */
    public UnifiedStudent getCurrentStudent() {
        return currentStudent;
    }
    
    /**
     * Check if a student is currently logged in
     * @return true if someone is logged in
     */
    public boolean isLoggedIn() {
        return currentStudent != null;
    }
    
    /**
     * Get current student's full name
     * @return Full name or "Guest" if not logged in
     */
    public String getCurrentStudentName() {
        if (currentStudent != null) {
            return currentStudent.getFullName();
        }
        return "Guest User";
    }
    
    /**
     * Get current student's student number
     * @return Student number or "000000000" if not logged in
     */
    public String getCurrentStudentNumber() {
        if (currentStudent != null) {
            return currentStudent.getStudentId();
        }
        return "000000000";
    }
    
    /**
     * Get current student's email
     * @return Email or "guest@mycput.ac.za" if not logged in
     */
    public String getCurrentStudentEmail() {
        if (currentStudent != null) {
            return currentStudent.getEmail();
        }
        return "guest@mycput.ac.za";
    }
    
    /**
     * Get current student's first initial for avatar
     * @return First initial or "G" for guest
     */
    public String getCurrentStudentInitial() {
        if (currentStudent != null) {
            return currentStudent.getFirstInitial();
        }
        return "G";
    }
    
    /**
     * Logout current student
     * Clears the session
     */
    public void logout() {
        if (currentStudent != null) {
            System.out.println("✓ Session ended for: " + currentStudent.getFullName());
            currentStudent = null;
        }
    }
    
    /**
     * Update current student information
     * Use this after profile updates
     * @param student Updated student object
     */
    public void updateCurrentStudent(UnifiedStudent student) {
        if (currentStudent != null && student != null && 
            currentStudent.getStudentId().equals(student.getStudentId())) {
            this.currentStudent = student;
            System.out.println("✓ Session updated for: " + student.getFullName());
        }
    }
    
    /**
     * Get a default/fallback student for testing
     * Only use if no student is logged in and you need placeholder data
     * @return Default student object
     */
    public UnifiedStudent getDefaultStudent() {
        if (currentStudent != null) {
            return currentStudent;
        }
        
        // Create fallback student
        UnifiedStudent defaultStudent = new UnifiedStudent();
        defaultStudent.setStudentId("230145623");
        defaultStudent.setFirstName("Guest");
        defaultStudent.setLastName("User");
        defaultStudent.setEmail("guest@mycput.ac.za");
        defaultStudent.setYear("Second Year");
        defaultStudent.setStream("Application Development");
        defaultStudent.setCourse("Diploma in ICT");
        
        return defaultStudent;
    }
    
    /**
     * Validate session
     * Ensures current student data is valid
     * @return true if session is valid
     */
    public boolean isSessionValid() {
        if (currentStudent == null) {
            return false;
        }
        
        return currentStudent.getStudentId() != null && 
               !currentStudent.getStudentId().isEmpty() &&
               currentStudent.getEmail() != null &&
               !currentStudent.getEmail().isEmpty();
    }
    
    /**
     * Print session info (for debugging)
     */
    public void printSessionInfo() {
        if (isLoggedIn()) {
            System.out.println("\n=== SESSION INFO ===");
            System.out.println("Student ID: " + currentStudent.getStudentId());
            System.out.println("Name: " + currentStudent.getFullName());
            System.out.println("Email: " + currentStudent.getEmail());
            System.out.println("Year: " + currentStudent.getYear());
            System.out.println("Stream: " + currentStudent.getStream());
            System.out.println("===================\n");
        } else {
            System.out.println("No active session");
        }
    }
}