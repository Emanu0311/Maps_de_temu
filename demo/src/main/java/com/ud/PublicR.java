package com.ud;

import java.util.List;
import com.graphhopper.config.Profile;
import com.graphhopper.config.CHProfile;
import com.graphhopper.util.GHUtility;

public class PublicR implements RouteServices {
    @Override
    public List<double[]> buildRoute(double[] a, double[] b) {
        System.out.println("Calculating Public Transport (using foot fallback) route...");
        return GraphHopperManager.calculateRoute(a, b, "foot");
    }

    @Override
    public Profile getProfile() {
        return new Profile("foot").setCustomModel(GHUtility.loadCustomModelFromJar("foot.json"));
    }

    @Override
    public CHProfile getCHProfile() {
        return new CHProfile("foot");
    }
}
