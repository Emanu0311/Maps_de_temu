package com.ud;

import java.util.List;
import com.graphhopper.config.Profile;
import com.graphhopper.config.CHProfile;
import com.graphhopper.util.GHUtility;

public class CarR implements RouteServices {
    @Override
    public List<double[]> buildRoute(double[] a, double[] b) {
        System.out.println("Calculating Car route...");
        return GraphHopperManager.calculateRoute(a, b, "car");
    }

    @Override
    public Profile getProfile() {
        return new Profile("car").setCustomModel(GHUtility.loadCustomModelFromJar("car.json"));
    }

    @Override
    public CHProfile getCHProfile() {
        return new CHProfile("car");
    }
}
