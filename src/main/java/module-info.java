module com.mycompany.csc311_finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;

    opens com.mycompany.csc311_finalproject to javafx.fxml, com.google.gson;
    exports com.mycompany.csc311_finalproject;
}
