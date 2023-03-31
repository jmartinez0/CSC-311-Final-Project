package com.mycompany.csc311_finalproject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PrimaryController implements Initializable {

    @FXML
    private VBox optionsMenu;
    @FXML
    private Label importJsonLabel, exportJsonLabel, readDatabaseLabel, scaleLabel, label;
    @FXML
    private TableView<Student> studentTableView;
    @FXML
    private TableColumn<Student, String> firstNameColumn, lastNameColumn;
    @FXML
    private TableColumn<Student, Double> mathGradeColumn, scienceGradeColumn, englishGradeColumn, gpaColumn;
    @FXML
    private ImageView dropDownArrowImageView;
    Image dropDownArrow = new Image(getClass().getResourceAsStream("dropDownArrow.png"));

    LinkedList<Student> studentList = new LinkedList<>();
    private ObservableList<Student> listOfStudents = FXCollections.observableArrayList();
    private Student currentStudent;

    /**
     * Takes student grades from a file, calculates each student's GPA, and
     * enters a list of students sorted by GPA into a database.
     */
    @FXML
    public void importJson() {
        Object averageStudentGradesLock = new Object();
        Object sortStudentGradesLock = new Object();

//************************ USES THREADS FOR BACKGROUND PROCESSING REQUIREMENT HERE ************************************************************************
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
//************************ GUI DOES NOT HANG FOR ANY LONG RUNNING OPERATIONS ************************************************************************                
                Platform.runLater(() -> importJsonLabel.setDisable(true)); // Once we import the json data we don't need to do it again
                Platform.runLater(() -> label.setText("Reading JSON data..."));
                System.out.println("Reading JSON data...");

//************************ BACK-END SQL DATABASE REQUIREMENT HERE ************************************************************************
                // Connect to the Microsoft Access database and clear existing data
                String databaseURL = "";
                Connection conn = null;
                try {
                    databaseURL = "jdbc:ucanaccess://.//Students.accdb";
                    conn = DriverManager.getConnection(databaseURL);
                    String sql = "DELETE FROM StudentTable";
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    int rowsDeleted = preparedStatement.executeUpdate();
                    System.out.println(rowsDeleted + " rows deleted");
                } catch (SQLException ex) {
                    System.out.println("Connection unsuccessful");
                }

//************************ SHOULD BE ABLE TO IMPORT JSON DATA REQUIREMENT HERE ************************************************************************
                // Read the data from the json file into a linked list of Students              
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                try {
                    File file = new File("students.json");
                    FileReader fr = new FileReader(file);
//************************ USES A JAVA COLLECTION CLASS REQUIREMENT HERE ************************************************************************                    
                    studentList = gson.fromJson(fr, new TypeToken<LinkedList<Student>>() {
                    }.getType());
                    fr.close();
                } catch (IOException ex) {
                }

                // Loop through the linked list to calculate the GPA for each student
                Platform.runLater(() -> label.setText("Sorting JSON data..."));
                System.out.println("Sorting JSON data...");
                for (int i = 0; i < studentList.size(); i++) {
                    try {
                        Thread.sleep(75);
                        String first = studentList.get(i).getFirstName();
                        String last = studentList.get(i).getLastName();
                        double math = studentList.get(i).getMathGrade();
                        double science = studentList.get(i).getScienceGrade();
                        double english = studentList.get(i).getEnglishGrade();

                        // Before we can compare student GPAs and sort the list, each student needs to have their GPA calculated
//************************ LAMBDAS AND STREAMS INCORPORATED HERE ************************************************************************
                        synchronized (averageStudentGradesLock) {
                            List<Double> grades = List.of(math, science, english);
                            final double average = grades.stream().mapToDouble(x -> x).average().getAsDouble();
                            studentList.get(i).setGpa(average);
                        }

                        // Sorts the list of students by GPA in descending order
                        synchronized (sortStudentGradesLock) {
                            Collections.sort(studentList, Comparator.comparingDouble(Student::getGpa).reversed());
                        }

                    } catch (InterruptedException ex) {
                        System.out.println("Calling sleep didn't work");
                    }
                }

                // Loop through the linked list and insert the sorted list into the database
                Platform.runLater(() -> label.setText("Inserting student data into database..."));
                System.out.println("Inserting student data into database...");
                // The student list is already sorted by GPA in descending order so we just iterate through studentList from the first index (the highest gpa)
                for (int i = 0; i < studentList.size(); i++) {
                    try {
                        Thread.sleep(75);
                        try {
                            String sql = "INSERT INTO StudentTable (First, Last, mathGrade, scienceGrade, englishGrade, GPA) VALUES (?, ?, ?, ?, ?, ?)";
                            PreparedStatement preparedStatement = conn.prepareStatement(sql);
                            preparedStatement.setString(1, studentList.get(i).getFirstName());
                            preparedStatement.setString(2, studentList.get(i).getLastName());
                            preparedStatement.setDouble(3, studentList.get(i).getMathGrade());
                            preparedStatement.setDouble(4, studentList.get(i).getScienceGrade());
                            preparedStatement.setDouble(5, studentList.get(i).getEnglishGrade());
                            preparedStatement.setDouble(6, studentList.get(i).getGpa());
                            int row = preparedStatement.executeUpdate();
                            if (row > 0) {
                                System.out.println("Row inserted");
                            }
                        } catch (SQLException e) {
                        }

                    } catch (InterruptedException ex) {
                        System.out.println("Calling sleep didn't work");
                    }
                }
                Platform.runLater(() -> label.setText("JSON Data Imported"));
                System.out.println("JSON Data Imported");
                Platform.runLater(() -> readDatabaseLabel.setDisable(false)); // Once the json data is imported we can now read the database into the TableView
            }
        });
        t.start();
    }

    /**
     * Exports the student data from the database into a json file.
     */
    @FXML
    public void exportJson() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(() -> exportJsonLabel.setDisable(true)); // The data has already been exported to JSON file
                Platform.runLater(() -> label.setText("Exporting to JSON file..."));
                System.out.println("Exporting to JSON file...");

                // Connect to the Microsoft Access database
                String databaseURL = "";
                Connection conn = null;
                try {
                    databaseURL = "jdbc:ucanaccess://.//Students.accdb";
                    conn = DriverManager.getConnection(databaseURL);
                } catch (SQLException ex) {
                    System.out.println("Connection unsuccessful");
                }

//************************ SHOULD BE ABLE TO EXPORT JSON DATA FROM THE SQL DATABASE REQUIREMENT HERE ************************************************************************
                // Query the Microsoft Access database to get data for every student then export it to a JSON file
                try {
                    PrintStream ps;
                    ps = new PrintStream("StudentExport.json");
                    Student[] studentArray = new Student[20];
                    int i = 0;
                    try {
                        String tableName = "StudentTable";
                        Statement stmt = conn.createStatement();
                        ResultSet result = stmt.executeQuery("select * from " + tableName);
                        while (result.next()) {
                            try {
                                Thread.sleep(75);
                                String first = result.getString("First");
                                String last = result.getString("Last");
                                double math = result.getDouble("mathGrade");
                                double science = result.getDouble("scienceGrade");
                                double english = result.getDouble("englishGrade");
                                double gpa = result.getDouble("GPA");
                                // Create a new student using the data from the database to use as a json object for the file
                                currentStudent = new Student(first, last, math, science, english, gpa);
                                studentArray[i] = currentStudent;
                                i++;
                            } catch (InterruptedException ex) {
                                System.out.println("Calling sleep didn't work");
                            }
                        }
                        // Export the studentArray to a json file with pretty printing formatting
                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();
                        String jsonString = gson.toJson(studentArray);
                        ps.println(jsonString);
                    } catch (SQLException except) {
                        except.printStackTrace();
                    }
                } catch (FileNotFoundException ex) {
                    System.out.println("File not found");
                }

                Platform.runLater(() -> label.setText("Exported to JSON file (StudentExport.json)"));
                System.out.println("Exported to JSON file (StudentExport.json)");
            }
        });
        t.start();
    }

    /**
     * Reads a database of student grades into a table on screen.
     */
    @FXML
    public void readDatabase() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(() -> readDatabaseLabel.setDisable(true)); // Once we read the database we don't need to do it again
                Platform.runLater(() -> label.setText("Reading database..."));
                System.out.println("Reading database...");

                // Connect to the Microsoft Access database
                String databaseURL = "";
                Connection conn = null;
                try {
                    databaseURL = "jdbc:ucanaccess://.//Students.accdb";
                    conn = DriverManager.getConnection(databaseURL);
                } catch (SQLException ex) {
                    System.out.println("Connection unsuccessful");
                }

//************************ SHOULD BE ABLE TO DISPLAY DATA FROM THE SQL DATABASE REQUIREMENT HERE ************************************************************************
                // Query the Microsoft Access database to get data for every student then display it in a TableView
                try {
                    String tableName = "StudentTable";
                    Statement stmt = conn.createStatement();
                    ResultSet result = stmt.executeQuery("select * from " + tableName);
                    while (result.next()) {
                        try {
                            Thread.sleep(75);
                            String first = result.getString("First");
                            String last = result.getString("Last");
                            double math = result.getDouble("mathGrade");
                            double science = result.getDouble("scienceGrade");
                            double english = result.getDouble("englishGrade");
                            double gpa = result.getDouble("GPA");
                            // Create a new student using the data from the database and add it into an ObservableList to display in a TableView
                            currentStudent = new Student(first, last, math, science, english, gpa);
                            listOfStudents = studentTableView.getItems();
                            listOfStudents.add(currentStudent);
                            studentTableView.setItems(listOfStudents);
                        } catch (InterruptedException ex) {
                            System.out.println("Calling sleep didn't work");
                        }
                    }
                } catch (SQLException except) {
                    except.printStackTrace();
                }
                Platform.runLater(() -> exportJsonLabel.setDisable(false)); // There is now data to be exported to a new json file
                Platform.runLater(() -> scaleLabel.setDisable(false)); // The database is now read so we get the option to switch between 4.0 and 100 scale
                Platform.runLater(() -> label.setText("Database read successfully"));
                System.out.println("Database read successfully");
            }
        });
        t.start();
    }

    /**
     * Switches the student GPA column in the table between a 100 point GPA
     * scale and 4.0 GPA scale.
     */
    @FXML
    public void changeScale() {
        
        if (scaleLabel.getText().equals("4.0 GPA Scale")) {
            // A temp ObservableList is used to retain the student data because when the TableView is cleared, the listOfStudents ObservableList goes with it too
            ObservableList<Student> temp = FXCollections.observableArrayList();
            temp.clear();
            listOfStudents = studentTableView.getItems();
            for (int i = 0; i < listOfStudents.size(); i++) {
                listOfStudents.get(i).setGpa((listOfStudents.get(i).getGpa() / 20) - 1);
                temp.add(listOfStudents.get(i));
            }
            studentTableView.getItems().clear();
            studentTableView.setItems(temp);
            label.setText("Switched to 4.0 scale");
            scaleLabel.setText("100 GPA Scale");
            return;
        }
        
        if (scaleLabel.getText().equals("100 GPA Scale")) {
            ObservableList<Student> temp = FXCollections.observableArrayList();
            temp.clear();
            listOfStudents = studentTableView.getItems();
            for (int i = 0; i < listOfStudents.size(); i++) {
                listOfStudents.get(i).setGpa((listOfStudents.get(i).getGpa() + 1) * 20);
                temp.add(listOfStudents.get(i));
            }
            studentTableView.getItems().clear();
            studentTableView.setItems(temp);
            label.setText("Switched to 100 point scale");
            scaleLabel.setText("4.0 GPA Scale");
            return;
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    public void exit() {
        Platform.exit();
    }

    /**
     * Opens the options drop down menu.
     */
    @FXML
    public void openDropDownOptions() {
//************************ SHOULD INCLUDE AN ANIMATION REQUIREMENT HERE ************************************************************************
        TranslateTransition openDropDown = new TranslateTransition(Duration.millis(500), optionsMenu);
        openDropDown.setToY(305);
        openDropDown.play();
    }

    /**
     * Closes the options drop down menu.
     */
    @FXML
    public void closeDropDownOptions() {
        TranslateTransition closeDropDown = new TranslateTransition(Duration.millis(500), optionsMenu);
        closeDropDown.setToY(-245);
        closeDropDown.play();
    }

    /**
     * Initializes what options are available to the user when the application
     * first starts and sets up the table to hold student data.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dropDownArrowImageView.setImage(dropDownArrow);
        readDatabaseLabel.setDisable(true); // When the application first starts, the json data has not been imported yet so we can't read the database
        exportJsonLabel.setDisable(true); // When the application first starts, the database has not been read so there is nothing to export to a new json file
        scaleLabel.setDisable(true); // When the application first starts, the json data has not been imported yet so there is no data in the table to view
        studentTableView.setFocusTraversable(false);
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        mathGradeColumn.setCellValueFactory(new PropertyValueFactory<>("mathGrade"));
        scienceGradeColumn.setCellValueFactory(new PropertyValueFactory<>("scienceGrade"));
        englishGradeColumn.setCellValueFactory(new PropertyValueFactory<>("englishGrade"));
        gpaColumn.setCellValueFactory(new PropertyValueFactory<>("gpa"));
    }

}
