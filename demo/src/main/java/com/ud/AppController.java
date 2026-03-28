package com.ud;

import javafx.stage.Stage;
import java.util.List;

public class AppController {

    private Navigator navigator;
    private AppView appView;

    // UI state
    private double[] origin = null;
    private double[] destination = null;
    private boolean selectOriginNext = true;

    public AppController(Stage primaryStage) {
        // Initialize default strategy
        navigator = new Navigator(new CarR());

        // Initialize view
        appView = new AppView(new JavaConnector());

        // Bind events
        appView.getStrategySelector().setOnAction(e -> {
            String val = appView.getStrategySelector().getValue();
            if (val.contains("Car"))
                navigator.setStrategy(new CarR());
            else if (val.contains("Moto"))
                navigator.setStrategy(new MBikeR());
            else if (val.contains("Bike"))
                navigator.setStrategy(new BikeR());
            else if (val.contains("Public"))
                navigator.setStrategy(new PublicR());

            // Auto recalculate if points exist
            if (origin != null && destination != null) {
                calculateAndDrawRoute();
            }
        });

        appView.getCalcButton().setOnAction(e -> calculateAndDrawRoute());

        appView.getResetButton().setOnAction(e -> {
            origin = null;
            destination = null;
            appView.setOriginText("");
            appView.setDestinationText("");
            selectOriginNext = true;
            appView.executeJS("clearMarkers()");
        });

        primaryStage.setTitle("Mapa de temu (por favor usa google maps para evitar errores)");
        primaryStage.setScene(appView.getScene());
        primaryStage.show();

        // Async Initialization of GraphHopper to prevent UI freeze
        new Thread(() -> {
            System.out.println("Cargando mapas de fondo...");
            GraphHopperManager.getInstance();
        }).start();
    }

    private void calculateAndDrawRoute() {
        if (origin == null || destination == null) {
            System.out.println("Debe seleccionar origen y destino primero.");
            return;
        }

        System.out.println("Calculando ruta con estrategia actual...");
        List<double[]> path = navigator.buildRoute(origin, destination);

        if (path != null && !path.isEmpty()) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < path.size(); i++) {
                json.append("[").append(path.get(i)[0]).append(",").append(path.get(i)[1]).append("]");
                if (i < path.size() - 1)
                    json.append(",");
            }
            json.append("]");

            appView.executeJS("setRoute('" + json.toString() + "')");
            System.out.println("Ruta dibujada!");
        } else {
            System.out.println("No se encontró una ruta.");
            appView.executeJS("setRoute('[]')");
        }
    }

    /**
     * Interface to communicate between JavaScript and Java
     */
    public class JavaConnector {
        public void onMapClick(double lat, double lng) {
            System.out.println("Map clicked at: " + lat + ", " + lng);
            if (selectOriginNext) {
                origin = new double[] { lat, lng };
                appView.setOriginText(String.format("%.4f, %.4f", lat, lng));
                appView.executeJS("setStartMarker(" + lat + ", " + lng + ")");
                selectOriginNext = false;
            } else {
                destination = new double[] { lat, lng };
                appView.setDestinationText(String.format("%.4f, %.4f", lat, lng));
                appView.executeJS("setEndMarker(" + lat + ", " + lng + ")");
                selectOriginNext = true;
                calculateAndDrawRoute();
            }
        }
    }
}
