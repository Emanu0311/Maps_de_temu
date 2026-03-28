package com.ud.Services;

import java.util.List;

/**
 * Interface for the Strategy Pattern.
 * Provides a method to build a route between two points.
 */
public interface RouteServices {
    /**
     * Builds a route from an origin to a destination.
     * 
     * @param a The origin coordinates [latitude, longitude]
     * @param b The destination coordinates [latitude, longitude]
     * @return A list of coordinates [latitude, longitude] representing the path,
     *         or an empty list if no route was found.
     */
    List<double[]> buildRoute(double[] a, double[] b);

    /**
     * @return The routing Profile associated with this strategy
     */
    com.graphhopper.config.Profile getProfile();

    /**
     * @return The CHProfile associated with this strategy
     */
    com.graphhopper.config.CHProfile getCHProfile();
}
