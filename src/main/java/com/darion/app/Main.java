package com.darion.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader btwFXMLObjectsLoader = new FXMLLoader(Main.class.getResource("/com/darion/app/view/fxml/btw-view.fxml"));
        Scene scene = new Scene(btwFXMLObjectsLoader.load(), 850, 600);
        stage.setTitle("Better Than Wolves Saver");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("view/css/btwStyles.css")).toExternalForm());
    }
}