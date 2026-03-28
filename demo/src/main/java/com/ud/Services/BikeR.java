package com.ud.Services;

import java.util.List;
import com.graphhopper.config.Profile;
import com.graphhopper.config.CHProfile;
import com.graphhopper.util.GHUtility;
import com.ud.GraphHopperManager;

public class BikeR implements RouteServices {
    @Override
    public List<double[]> buildRoute(double[] a, double[] b) {
        System.out.println("Calculating Bike route...");
        return GraphHopperManager.calculateRoute(a, b, "bike");
    }

    @Override
    public Profile getProfile() {
        return new Profile("bike").setCustomModel(GHUtility.loadCustomModelFromJar("bike.json"));
    }

    @Override
    public CHProfile getCHProfile() {
        return new CHProfile("bike");
    }
}
