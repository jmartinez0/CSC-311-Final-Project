package com.mycompany.csc311_finalproject;

/**
 *
 * @author Juan
 */
public class Student {
    String firstName, lastName;
    double mathGrade, scienceGrade, englishGrade, gpa;

    public Student() {
        this.firstName = "";
        this.lastName = "";
        this.mathGrade = 0.0;
        this.scienceGrade = 0.0;
        this.englishGrade = 0.0;
        this.gpa = 0.0;
    }
    
    public Student(String firstName, String lastName, double mathGrade, double scienceGrade, double englishGrade, double gpa) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mathGrade = mathGrade;
        this.scienceGrade = scienceGrade;
        this.englishGrade = englishGrade;
        this.gpa = gpa;
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

    public double getMathGrade() {
        return mathGrade;
    }

    public void setMathGrade(double mathGrade) {
        this.mathGrade = mathGrade;
    }

    public double getScienceGrade() {
        return scienceGrade;
    }

    public void setScienceGrade(double scienceGrade) {
        this.scienceGrade = scienceGrade;
    }

    public double getEnglishGrade() {
        return englishGrade;
    }

    public void setEnglishGrade(double englishGrade) {
        this.englishGrade = englishGrade;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }
    
    @Override public String toString() {
        return firstName + " " + lastName + " " + mathGrade + " " + scienceGrade + " " + englishGrade + " " + gpa;
    }
    
}
