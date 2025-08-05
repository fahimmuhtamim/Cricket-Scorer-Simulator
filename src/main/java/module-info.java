module com.example.cricketscorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;

    opens com.example.cricketscorer to javafx.fxml;
    exports com.example.cricketscorer;
    exports com.example.cricketscorer.sync;
    opens com.example.cricketscorer.sync to javafx.fxml;
}