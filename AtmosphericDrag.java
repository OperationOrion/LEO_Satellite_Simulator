package application;

public class AtmosphericDrag {
    private static final double MU_EARTH = 3.986004418e14;  // m^3/s^2
    private static final double EARTH_RADIUS = 6.371e6;     // m

    // Reference altitudes (m), densities (kg/m^3), and scale heights (m) up to 1,800 km
    /* values provided below are approximate, based on empirical data from NRLMSISE-00 
     * (Naval Research Laboratory Mass Spectrometer and Incoherent Scatter Radar Exosphere model), 
     * adjusted for average solar activity (e.g., F10.7 = 150 sfu). These are not exact for all 
     * conditions (solar activity, geomagnetic activity, and seasonal variations affect density),
     * but they provide a realistic baseline -Mitch M 4/7/25
     */
    
    private static final double[] REF_ALTITUDES = {
        100_000, 200_000, 300_000, 400_000, 500_000, 600_000, 800_000,
        1_000_000, 1_200_000, 1_400_000, 1_600_000, 1_800_000
    };
    private static final double[] REF_DENSITIES = {
        5.0e-8, 2.0e-9, 1.0e-10, 1.0e-11, 2.0e-12, 5.0e-13, 3.0e-14,
        2.0e-15, 1.0e-16, 5.0e-18, 2.0e-19, 1.0e-20
    };
    private static final double[] SCALE_HEIGHTS = {
        40_000, 50_000, 55_000, 60_000, 65_000, 70_000, 80_000,
        100_000, 150_000, 200_000, 250_000, 300_000
    };

    // POSSIBLE IDEA FOR MITCH: Solar activity multiplier (1.0 = average, 0.5 = low, 2.0 = high)
    private static final double SOLAR_ACTIVITY_FACTOR = 2.0;

    // Calculate atmospheric density based on altitude
    public static double calculateDensity(double altitude) {
        if (altitude < 100_000) {
            throw new IllegalArgumentException("Altitude must be >= 100 km for this model.");
        }

        // Find the appropriate altitude band
        for (int i = 0; i < REF_ALTITUDES.length - 1; i++) {
            if (altitude >= REF_ALTITUDES[i] && altitude < REF_ALTITUDES[i + 1]) {
                double rho0 = REF_DENSITIES[i];
                double h0 = REF_ALTITUDES[i];
                double H = SCALE_HEIGHTS[i];
                return rho0 * Math.exp(-(altitude - h0) / H) * SOLAR_ACTIVITY_FACTOR;
            }
        }
        // For altitudes >= 400 km, use the last band
        double rho0 = REF_DENSITIES[REF_DENSITIES.length - 1];
        double h0 = REF_ALTITUDES[REF_ALTITUDES.length - 1];
        double H = SCALE_HEIGHTS[SCALE_HEIGHTS.length - 1];
        return rho0 * Math.exp(-(altitude - h0) / H); //* SOLAR_ACTIVITY_FACTOR;
    }

    // Calculate orbital decay in meters per orbit
    public static double calculateDecayPerOrbit(Satellite satellite) {
        double altitude = satellite.getAltitude();
        double radius = EARTH_RADIUS + altitude;

        double speed = OrbitalPeriod.calculateCircularSpeed(satellite);  // Orbital velocity (m/s)
        double period = OrbitalPeriod.calculatePeriod(satellite);       // Orbital period (s)

        double density = calculateDensity(altitude);
        double bc = Ballistic.calculateBallisticCoefficient(satellite);

        // Drag force per unit mass (acceleration, m/s^2)
        double dragAccel = 0.5 * density * speed * speed / bc;

        // Rate of change of semi-major axis (m/s) due to drag
        double da_dt = -2 * Math.PI * radius * dragAccel / speed;

        // Decay per orbit (m)
        return -da_dt * period;  // Negative sign indicates loss of altitude
    }
    
    //function test for Mitch M------------------------------------------------
    public static void main(String[] args) {
        Satellite testSat = new Satellite(1, "Mercury", 500, 4, 400_000);
        double decay = calculateDecayPerOrbit(testSat);
        System.out.printf("Decay per orbit: %.4f meters%n", decay);
    }
}