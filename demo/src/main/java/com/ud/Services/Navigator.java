package com.ud.Services;

import java.util.List;

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
