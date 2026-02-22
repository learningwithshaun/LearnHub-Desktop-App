/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.za.cput.prac50.domain;

/**
 *
 * @author PC
 */
public class PassSetup {
    private String studentMail;
    private String SecurityQuestion;
    private String answer;
    private String newPassword;
    private String repeatPassword;

    public PassSetup() {
    }

    public PassSetup(String studentMail, String SecurityQuestion, String answer, String newPassword, String repeatPassword) {
        this.studentMail = studentMail;
        this.SecurityQuestion = SecurityQuestion;
        this.answer = answer;
        this.newPassword = newPassword;
        this.repeatPassword = repeatPassword;
    }

    public String getStudentMail() {
        return studentMail;
    }

    public void setStudentMail(String studentMail) {
        this.studentMail = studentMail;
    }

    public String getSecurityQuestion() {
        return SecurityQuestion;
    }

    public void setSecurityQuestion(String SecurityQuestion) {
        this.SecurityQuestion = SecurityQuestion;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    @Override
    public String toString() {
        return "PassSetup{" + "studentMail=" + studentMail + ", SecurityQuestion=" + SecurityQuestion + ", answer=" + answer + ", newPassword=" + newPassword + ", repeatPassword=" + repeatPassword + '}';
    }
            
}
