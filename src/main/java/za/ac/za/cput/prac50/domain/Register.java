/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.za.cput.prac50.domain;

/**
 *
 * @author PC
 */
public class Register {
   private String firstName;
   private String lastName;
   private String studentMail;
   private String questionBox;
   private String answer;
   private String password;
   private String repeatPassword;

    public Register() {
    }

    public Register(String firstName, String lastName, String studentMail, String questionBox, String answer, String password, String repeatPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentMail = studentMail;
        this.questionBox = questionBox;
        this.answer = answer;
        this.password = password;
        this.repeatPassword = repeatPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudentMail() {
        return studentMail;
    }

    public void setStudentMail(String studentMail) {
        this.studentMail = studentMail;
    }

    public String getQuestionBox() {
        return questionBox;
    }

    public void setQuestionBox(String questionBox) {
        this.questionBox = questionBox;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    @Override
    public String toString() {
        return "Register{" + "firstName=" + firstName + ", lastName=" + lastName + ", studentMail=" + studentMail + ", questionBox=" + questionBox + ", answer=" + answer + ", password=" + password + ", repeatPassword=" + repeatPassword + '}';
    }
   
   
   
}
