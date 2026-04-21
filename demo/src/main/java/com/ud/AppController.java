package com.ud;

import javafx.stage.Stage;
import java.util.List;

import com.ud.Services.BikeR;
import com.ud.Services.CarR;
import com.ud.Services.MBikeR;
import com.ud.Services.Navigator;
import com.ud.Services.PublicR;

public class AppController {

    Navigator navigator;
    AppView appView;

    // UI state
    double[] origin = null;
    double[] destination = null;
    boolean selectOriginNext = true;

    public AppController(Stage primaryStage) {
        navigator = new Navigator(new CarR());

        // Pass a lambda that handles map clicks directly — no JavaConnector needed
        appView = new AppView((lat, lng) -> onMapClick(lat, lng));

        appView.getStrategySelector().setOnAction(e -> {
            String val = appView.getStrategySelector().getValue();
            if (val.contains("Carro"))
                navigator.setStrategy(new CarR());
            else if (val.contains("Moto"))
                navigator.setStrategy(new MBikeR());
            else if (val.contains("Bicicleta"))
                navigator.setStrategy(new BikeR());
            else if (val.contains("A pie"))
                navigator.setStrategy(new PublicR());

            if (origin != null && destination != null) {
                calculateAndDrawRoute();
            }
        });

        appView.getCalcButton().setOnAction(e -> calculateAndDrawRoute());

        appView.getResetButton().setOnAction(e -> {
            origin = null;
            destination = null;
            selectOriginNext = true;
            appView.setOriginText("");
            appView.setDestinationText("");
            appView.clearOverlays();
        });

        primaryStage.setTitle("Mapa de Temu");
        primaryStage.setScene(appView.getScene());
        primaryStage.show();

        new Thread(() -> {
            System.out.println("Cargando mapas de fondo...");
            GraphHopperManager.getInstance();
        }).start();
    }

    /** Called by the map click callback in AppView */
    private void onMapClick(double lat, double lng) {
        System.out.println("Map clicked at: " + lat + ", " + lng);
        if (selectOriginNext) {
            origin = new double[]{lat, lng};
            appView.setOriginText(String.format("%.4f, %.4f", lat, lng));
            appView.setStartMarker(lat, lng);
            selectOriginNext = false;
        } else {
            destination = new double[]{lat, lng};
            appView.setDestinationText(String.format("%.4f, %.4f", lat, lng));
            appView.setEndMarker(lat, lng);
            selectOriginNext = true;
            calculateAndDrawRoute();
        }
    }

    void calculateAndDrawRoute() {
        if (origin == null || destination == null) {
            System.out.println("Debe seleccionar origen y destino primero.");
            return;
        }
        System.out.println("Calculando ruta con estrategia actual...");
        List<double[]> path = navigator.buildRoute(origin, destination);

        if (path != null && !path.isEmpty()) {
            appView.setRoute(path);
            System.out.println("Ruta dibujada!");
        } else {
            System.out.println("No se encontró una ruta.");
            appView.setRoute(null);
        }
    }
}
