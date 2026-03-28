package com.ud;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Delegate all setup and logic to the Controller
        new AppController(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
