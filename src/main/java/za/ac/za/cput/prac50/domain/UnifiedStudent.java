package za.ac.za.cput.prac50.domain;

/**
 * UNIFIED Student Domain Class
 * Consolidates: Student.java, Student1.java, Student3.java, Student4.java
 * 
 * @author LearnHub Team - Consolidated Version
 */
public class UnifiedStudent {
    // All fields from various Student classes
    private String studentId;        // Primary key (was studentNumber in some classes)
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String year;            // e.g., "First Year", "Second Year", "Third Year"
    private String stream;          // e.g., "Application Development", "Multimedia", "Communication and Networks"
    private String course;          // e.g., "Diploma in ICT in Applications Development"
    
    // ===== CONSTRUCTORS =====
    
    /**
     * Default constructor
     */
    public UnifiedStudent() {
        // Initialize with default values
        this.studentId = "";
        this.firstName = "User";
        this.lastName = "";
        this.email = "";
        this.password = "";
        this.year = "";
        this.stream = "";
        this.course = "";
    }
    
    /**
     * Constructor with all fields
     */
    public UnifiedStudent(String studentId, String firstName, String lastName, 
                         String email, String password, String year, 
                         String stream, String course) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.year = year;
        this.stream = stream;
        this.course = course;
    }
    
    /**
     * Constructor without password (for display purposes)
     */
    public UnifiedStudent(String studentId, String firstName, String lastName, 
                         String email, String year, String stream, String course) {
        this(studentId, firstName, lastName, email, "", year, stream, course);
    }
    
    /**
     * Minimal constructor (authentication purposes)
     */
    public UnifiedStudent(String studentId, String firstName, String lastName, String email) {
        this(studentId, firstName, lastName, email, "", "", "", "");
    }
    
    // ===== GETTERS =====
    
    public String getStudentId() {
        return studentId;
    }
    
    // Alias for compatibility with old code
    public String getStudentNumber() {
        return studentId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getYear() {
        return year;
    }
    
    public String getStream() {
        return stream;
    }
    
    public String getCourse() {
        return course;
    }
    
    // ===== SETTERS =====
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    // Alias for compatibility with old code
    public void setStudentNumber(String studentNumber) {
        this.studentId = studentNumber;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setYear(String year) {
        this.year = year;
    }
    
    public void setStream(String stream) {
        this.stream = stream;
    }
    
    public void setCourse(String course) {
        this.course = course;
    }
    
    // ===== HELPER METHODS (Used across UI components) =====
    
    /**
     * Get full name (first + last)
     * @return Full name string
     */
    public String getFullName() {
        if (lastName == null || lastName.trim().isEmpty()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
    
    /**
     * Get first name initial (used in UI avatars)
     * @return First character of first name, or 'U' if empty
     */
    public String getFirstInitial() {
        if (firstName != null && !firstName.isEmpty()) {
            return String.valueOf(firstName.charAt(0)).toUpperCase();
        }
        return "U";
    }
    
    /**
     * Get first name initial as char (alternative method)
     * @return First character or 'U'
     */
    public char getFirstInitialChar() {
        if (firstName != null && !firstName.isEmpty()) {
            return Character.toUpperCase(firstName.charAt(0));
        }
        return 'U';
    }
    
    /**
     * Check if student has complete profile
     * @return true if all required fields are filled
     */
    public boolean hasCompleteProfile() {
        return studentId != null && !studentId.isEmpty() &&
               firstName != null && !firstName.isEmpty() &&
               lastName != null && !lastName.isEmpty() &&
               email != null && !email.isEmpty();
    }
    
    /**
     * Validate email format (basic validation)
     * @return true if email contains @ and .
     */
    public boolean isValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    /**
     * Check if student is enrolled in a specific year
     * @param yearToCheck Year to check
     * @return true if matches
     */
    public boolean isInYear(String yearToCheck) {
        return year != null && year.equalsIgnoreCase(yearToCheck);
    }
    
    /**
     * Check if student is in a specific stream
     * @param streamToCheck Stream to check
     * @return true if matches
     */
    public boolean isInStream(String streamToCheck) {
        return stream != null && stream.equalsIgnoreCase(streamToCheck);
    }
    
    // ===== OVERRIDES =====
    
    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", year='" + year + '\'' +
                ", stream='" + stream + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UnifiedStudent student = (UnifiedStudent) obj;
        return studentId != null && studentId.equals(student.studentId);
    }
    
    @Override
    public int hashCode() {
        return studentId != null ? studentId.hashCode() : 0;
    }
}
