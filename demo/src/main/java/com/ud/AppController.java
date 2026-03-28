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

        appView = new AppView(new JavaConnector(this));

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

        new Thread(() -> {
            System.out.println("Cargando mapas de fondo...");
            GraphHopperManager.getInstance();
        }).start();
    }

    void calculateAndDrawRoute() {
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

}
