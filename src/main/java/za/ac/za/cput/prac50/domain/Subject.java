/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.za.cput.prac50.domain;

/**
 *
 * @author Amanda
 */
public class Subject {
    private String subjectCode;
    private String moduleName;
    private int credits;
    
    // Constructors
    public Subject() {}
    
    public Subject(String subjectCode, String moduleName, int credits) {
        this.subjectCode = subjectCode;
        this.moduleName = moduleName;
        this.credits = credits;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "Subject Code: " + subjectCode + ", Module Name: " + moduleName + ", credits: " + credits;
    }
    
}
