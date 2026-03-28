package com.ud;

public class JavaConnector {
    private AppController controller;

    public JavaConnector(AppController controller) {
        this.controller = controller;
    }

    public void onMapClick(double lat, double lng) {
        System.out.println("Map clicked at: " + lat + ", " + lng);
        if (controller.selectOriginNext) {
            controller.origin = new double[] { lat, lng };
            controller.appView.setOriginText(String.format("%.4f, %.4f", lat, lng));
            controller.appView.executeJS("setStartMarker(" + lat + ", " + lng + ")");
            controller.selectOriginNext = false;
        } else {
            controller.destination = new double[] { lat, lng };
            controller.appView.setDestinationText(String.format("%.4f, %.4f", lat, lng));
            controller.appView.executeJS("setEndMarker(" + lat + ", " + lng + ")");
            controller.selectOriginNext = true;
            controller.calculateAndDrawRoute();
        }
    }
}
