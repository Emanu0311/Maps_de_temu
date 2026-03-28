package com.ud;

import java.util.List;

/**
 * Context class for the Strategy Pattern.
 * It uses a given routing strategy (RouteServices) to build routes.
 */
public class Navigator {
    private RouteServices activeStrategy;

    public Navigator(RouteServices defaultStrategy) {
        this.activeStrategy = defaultStrategy;
    }

    public void setStrategy(RouteServices newStrategy) {
        this.activeStrategy = newStrategy;
    }

    public List<double[]> buildRoute(double[] a, double[] b) {
        if (activeStrategy == null) {
            System.err.println("No routing strategy defined!");
            return null;
        }
        return activeStrategy.buildRoute(a, b);
    }
}
