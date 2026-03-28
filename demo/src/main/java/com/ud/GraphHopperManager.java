package com.ud;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.util.PointList;
import com.ud.Services.BikeR;
import com.ud.Services.CarR;
import com.ud.Services.MBikeR;
import com.ud.Services.PublicR;
import com.ud.Services.RouteServices;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphHopperManager {
    private static GraphHopper hopper;

    public static synchronized GraphHopper getInstance() {
        if (hopper == null) {
            System.out.println("Initializing GraphHopper... This may take a while for large maps.");
            hopper = new GraphHopper();

            String mapFile = "demo/src/main/resources/mapacolombia.pbf";
            File file = new File(mapFile);
            if (!file.exists()) {
                file = new File("src/main/resources/mapacolombia.pbf");
            }
            if (!file.exists()) {
                file = new File("demo/target/classes/mapacolombia.pbf");
            }
            if (!file.exists()) {
                file = new File("target/classes/mapacolombia.pbf");
            }
            if (!file.exists()) {
                System.err.println("No se encontró el mapa en src/main/resources ni en target/classes.");
            }
            mapFile = file.getAbsolutePath();

            hopper.setOSMFile(mapFile);

            hopper.setGraphHopperLocation("target/routing-graph-cache");

            List<RouteServices> services = Arrays.asList(new CarR(), new BikeR(), new MBikeR(), new PublicR());
            List<Profile> profiles = new ArrayList<>();
            List<CHProfile> chProfiles = new ArrayList<>();

            for (RouteServices s : services) {
                profiles.add(s.getProfile());
                chProfiles.add(s.getCHProfile());
            }

            hopper.setProfiles(profiles);

            hopper.setEncodedValuesString("car_access, car_average_speed, bike_priority, bike_access, roundabout, " +
                    "bike_average_speed, track_type, road_access, road_class, surface, foot_access, hike_rating, " +
                    "foot_priority, foot_average_speed");

            try {
                hopper.importOrLoad();
                System.out.println("GraphHopper initialized successfully.");
            } catch (Exception e) {
                System.err.println("Failed to load map file. Please make sure the OSM/PBF file exists at " + mapFile);
                e.printStackTrace();
                hopper = null;
            }
        }
        return hopper;
    }

    public static List<double[]> calculateRoute(double[] origin, double[] destination, String profileName) {
        GraphHopper gh = getInstance();
        if (gh == null || origin == null || destination == null) {
            return new ArrayList<>();
        }

        GHRequest req = new GHRequest(origin[0], origin[1], destination[0], destination[1])
                .setProfile(profileName);

        GHResponse rsp = gh.route(req);

        if (rsp.hasErrors()) {
            System.err.println("Route computation errors for profile " + profileName + ": " + rsp.getErrors());
            return new ArrayList<>();
        }

        List<double[]> pathList = new ArrayList<>();
        PointList pointList = rsp.getBest().getPoints();
        for (int i = 0; i < pointList.size(); i++) {
            pathList.add(new double[] { pointList.getLat(i), pointList.getLon(i) });
        }
        return pathList;
    }
}
