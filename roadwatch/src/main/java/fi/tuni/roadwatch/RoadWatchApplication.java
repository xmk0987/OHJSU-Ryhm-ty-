package fi.tuni.roadwatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class RoadWatchApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(RoadWatchApplication.class.getResource("fxml/roadwatch.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        final RoadWatchController roadWatchController = fxmlLoader.getController();
        SessionData sessionData = new SessionData();
        roadWatchController.setSessionData(sessionData);
        roadWatchController.loadMap();


        stage.setTitle("RoadWatch");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}