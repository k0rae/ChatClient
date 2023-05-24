module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens chat.client to javafx.fxml;
    exports chat.client;
}