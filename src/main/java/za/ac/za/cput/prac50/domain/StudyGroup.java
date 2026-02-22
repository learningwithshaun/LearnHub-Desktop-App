package za.ac.za.cput.prac50.domain;

/**
 * Student name: Amanda Msutu Student number: 222428600 Group: 2G Program
 * Description: Project 2 (LearnHub) Date: 24 August 2025 Version: 1.0
 */
// StudyGroup.java
public class StudyGroup {

    private int groupId;
    private String groupName;
    private String description;
    private String year;
    private String stream;
    private String subjectCode;
    private String subject;
    private int maxMembers;
    private int currentMembers;

    public StudyGroup() {
    }

    public StudyGroup(int groupId, String groupName, String description, String year,
            String stream, String subjectCode, String subject,
            int maxMembers, int currentMembers) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.year = year;
        this.stream = stream;
        this.subjectCode = subjectCode;
        this.subject = subject;
        this.maxMembers = maxMembers;
        this.currentMembers = currentMembers;
    }

    // Getters and Setters
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public int getCurrentMembers() {
        return currentMembers;
    }

    public void setCurrentMembers(int currentMembers) {
        this.currentMembers = currentMembers;
    }

    public String toString() {
        return "StudyGroup{"
                + "groupId=" + groupId
                + ", groupName='" + groupName + '\''
                + ", description='" + description + '\''
                + ", year='" + year + '\''
                + ", stream='" + stream + '\''
                + ", subjectCode='" + subjectCode + '\''
                + ", subject='" + subject + '\''
                + ", maxMembers=" + maxMembers
                + ", currentMembers=" + currentMembers
                + '}';
    }
}
