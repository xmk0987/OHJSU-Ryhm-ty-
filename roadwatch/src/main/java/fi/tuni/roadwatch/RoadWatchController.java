package fi.tuni.roadwatch;

import com.sothawo.mapjfx.Projection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;


public class RoadWatchController {
    // Components
    private Stage stage;
    private Scene scene;
    public VBox root;
    public StackPane contentPane;
    public BorderPane mapPane;
    public BorderPane infoPane;
    public MapController mapController;

    public Pane home;
    public HomeController homeController;

    public Pane weather;
    public WeatherController weatherController;

    public Pane quickView;
    public QuickViewController quickViewController;

    public Pane preferences;
    public PreferencesController preferencesController;

    public Pane road;
    public RoadController roadController;

    private SessionData sessionData;

    //MAINWINDOW
    @FXML
    private Label logo;
    @FXML
    private ButtonBar buttonBar;
    @FXML
    private Button homeButton;
    @FXML
    private Button weatherButton;
    @FXML
    private Button quickViewButton;
    @FXML
    private Button preferencesButton;
    @FXML
    private Button roadDataButton;
    @FXML
    private Label siteLabel;


    public void setSessionData(SessionData sessionData){
        this.sessionData = sessionData;
    }

    public void loadMap() throws IOException {
        FXMLLoader mapFxmlLoader = new FXMLLoader();
        Parent rootNode = mapFxmlLoader.load(getClass().getResourceAsStream("fxml/mapview.fxml"));

        mapController = mapFxmlLoader.getController();
        final Projection projection = Projection.WEB_MERCATOR;

        // init map controls and set sessiondata
        mapController.setSessionData(sessionData);
        mapController.initMapAndControls(projection);

        Pane mapView = (Pane) rootNode;

        mapPane.setCenter(mapView);
    }

    // Actions
    public void loadHome(ActionEvent event) throws IOException {

        if(homeController == null){
            FXMLLoader homeFxmlLoader = new FXMLLoader();
            Parent rootNode = homeFxmlLoader.load(getClass().getResourceAsStream("fxml/home.fxml"));
            homeController = homeFxmlLoader.getController();
            homeController.setSessionData(sessionData);
            home = (Pane) rootNode;
        }

        infoPane.setCenter(home);
        siteLabel.setText("HOME");
        StackPane.setAlignment(infoPane, Pos.CENTER_RIGHT);
        mapPane.setVisible(true);
    }


    public void loadWeather(ActionEvent event) throws IOException {

        if(weatherController == null){
            FXMLLoader weatherFxmlLoader = new FXMLLoader();
            Parent rootNode = weatherFxmlLoader.load(getClass().getResourceAsStream("fxml/weather.fxml"));
            weatherController = weatherFxmlLoader.getController();
            weatherController.setSessionData(sessionData);
            weather = (Pane) rootNode;
        }

        infoPane.setCenter(weather);
        siteLabel.setText("WEATHER");
        StackPane.setAlignment(infoPane, Pos.CENTER_RIGHT);
        mapPane.setVisible(true);

    }

    public void loadQuickView(ActionEvent event) throws IOException {
        if(quickViewController == null){
            FXMLLoader quickViewFxmlLoader = new FXMLLoader();
            Parent rootNode = quickViewFxmlLoader.load(getClass().getResourceAsStream("fxml/quickview.fxml"));
            quickViewController = quickViewFxmlLoader.getController();
            quickViewController.setSessionData(sessionData);
            quickViewController.setData(mapPane, infoPane, siteLabel);
            quickView = (Pane) rootNode;
        }

        // Test output of setting coordinates to a view
        infoPane.setCenter(quickView);
        siteLabel.setText("QUICK VIEW");
        StackPane.setAlignment(infoPane, Pos.CENTER_RIGHT);
        mapPane.setVisible(true);
    }

    public void loadPreferences(ActionEvent event) throws IOException {
        if(preferencesController == null){
            FXMLLoader preferencesFxmlLoader = new FXMLLoader();
            Parent rootNode = preferencesFxmlLoader.load(getClass().getResourceAsStream("fxml/preferences.fxml"));
            preferencesController = preferencesFxmlLoader.getController();
            preferencesController.setSessionData(sessionData);
            preferences = (Pane) rootNode;
        }
        mapPane.setVisible(false);
        infoPane.setCenter(preferences);
        StackPane.setAlignment(infoPane, Pos.CENTER);
        siteLabel.setText("PREFERENCES");
    }

    public void loadRoadData(ActionEvent event) throws IOException {

        if(roadController == null){
            FXMLLoader roadFxmlLoader = new FXMLLoader();
            Parent rootNode = roadFxmlLoader.load(getClass().getResourceAsStream("fxml/roaddata.fxml"));
            roadController = roadFxmlLoader.getController();
            roadController.setSessionData(sessionData);
            road = (Pane) rootNode;
        }
        mapPane.setVisible(false);
        infoPane.setCenter(road);
        StackPane.setAlignment(infoPane, Pos.CENTER);
        siteLabel.setText("ROAD DATA");
    }

    void onButtonClick(ActionEvent event, Button btn, String oldIcon, String newIcon) {
        btn.setOnAction((ActionEvent e) -> {
            btn.getStyleClass().removeAll(oldIcon);
            btn.getStyleClass().add(newIcon);
        });
    }
}