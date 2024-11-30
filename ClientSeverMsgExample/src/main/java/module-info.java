module org.example.clientsevermsgexample {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.clientsevermsgexample to javafx.fxml;

    exports org.example.clientsevermsgexample;

}