package com.ud;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.net.URL;

public class AppView {

    private Scene scene;
    private ComboBox<String> strategySelector;
    private Button calcButton;
    private Button resetButton;
    private TextField originField;
    private TextField destinationField;
    private WebEngine webEngine;

    public AppView(Object javaConnector) {
        BorderPane root = new BorderPane();

        // --- Top Control Panel ---
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(15));

        HBox selectors = new HBox(15);

        strategySelector = new ComboBox<>();
        strategySelector.getItems().addAll("Carro", "Moto", "Bicicleta", "A pie");
        strategySelector.setValue("Carro");

        calcButton = new Button("Calcular Ruta");
        selectors.getChildren().addAll(new Label("Vehículo:"), strategySelector, calcButton);

        HBox coords = new HBox(15);
        originField = new TextField();
        originField.setPromptText("Lat, Lng");
        originField.setEditable(false);

        destinationField = new TextField();
        destinationField.setPromptText("Lat, Lng");
        destinationField.setEditable(false);

        resetButton = new Button("Limpiar Puntos");

        coords.getChildren().addAll(
                new Label("Origen:"), originField,
                new Label("Destino:"), destinationField,
                resetButton);

        topPanel.getChildren().addAll(
                new Label("Haz clic en el mapa para seleccionar Origen y luego Destino."),
                selectors,
                coords);
        root.setTop(topPanel);

        WebView webView = new WebView();
        webEngine = webView.getEngine();

        URL mapUrl = getClass().getResource("/map.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        } else {
            System.err.println("Map HTML not found in resources!");
        }

        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState,
                            Worker.State newState) {
                        if (newState == Worker.State.SUCCEEDED && javaConnector != null) {
                            JSObject window = (JSObject) webEngine.executeScript("window");
                            window.setMember("javaConnector", javaConnector);
                        }
                    }
                });

        root.setCenter(webView);
        scene = new Scene(root, 1000, 700);
    }

    public Scene getScene() {
        return scene;
    }

    public ComboBox<String> getStrategySelector() {
        return strategySelector;
    }

    public Button getCalcButton() {
        return calcButton;
    }

    public Button getResetButton() {
        return resetButton;
    }

    // Changing textual fields
    public void setOriginText(String text) {
        originField.setText(text);
    }

    public void setDestinationText(String text) {
        destinationField.setText(text);
    }

    // Map control methods
    public void executeJS(String script) {
        if (webEngine != null) {
            webEngine.executeScript(script);
        }
    }
}
