package application;

public class NumOrbitsLifecycle {
    private static final double DEFAULT_LIFECYCLE_SECONDS = 5 * 365.25 * 24 * 60 * 60; // 5 years
    private final Satellite satellite;
    private final double lifecycleSeconds;

    public NumOrbitsLifecycle(Satellite satellite) {
        this.satellite = satellite;
        this.lifecycleSeconds = DEFAULT_LIFECYCLE_SECONDS;
    }

    public NumOrbitsLifecycle(Satellite satellite, double lifecycleYears) {
        if (lifecycleYears <= 0) {
            throw new IllegalArgumentException("Lifecycle duration must be positive.");
        }
        this.satellite = satellite;
        this.lifecycleSeconds = lifecycleYears * 365.25 * 24 * 60 * 60;
    }

    // Calculates number of orbits over entire lifecycle.
  
    public long calculateNumberOfOrbits() {
        double period = OrbitalPeriod.calculatePeriod(satellite);
        return (long) (lifecycleSeconds / period);
    }

    public double getLifecycleYears() {
        return lifecycleSeconds / (365.25 * 24 * 60 * 60);
    }
    //function test for tyler B---------------------------------------------
    public static void main(String[] args) {
        Satellite testSat = new Satellite(1, "Hercules", 500, 4, 400_000);
        NumOrbitsLifecycle lifecycle = new NumOrbitsLifecycle(testSat);
        long numberOfOrbits = lifecycle.calculateNumberOfOrbits();
        System.out.println("Number of orbits made over lifecycle " + numberOfOrbits);
    }
}