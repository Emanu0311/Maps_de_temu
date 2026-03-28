package com.ud;

import java.util.List;
import com.graphhopper.config.Profile;
import com.graphhopper.config.CHProfile;
import com.graphhopper.util.GHUtility;

public class MBikeR implements RouteServices {
    @Override
    public List<double[]> buildRoute(double[] a, double[] b) {
        System.out.println("Calculating Motorbike route...");
        return GraphHopperManager.calculateRoute(a, b, "motorcycle");
    }

    @Override
    public Profile getProfile() {
        return new Profile("motorcycle").setCustomModel(GHUtility.loadCustomModelFromJar("motorcycle.json"));
    }

    @Override
    public CHProfile getCHProfile() {
        return new CHProfile("motorcycle");
    }
}
