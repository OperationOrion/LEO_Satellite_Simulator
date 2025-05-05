package application;

public class OrbitalPeriod {
    private static final double MU_EARTH = 3.986004418e14; // m^3/s^2
    private static final double EARTH_RADIUS = 6.371e6;    // m

    //Calculates period from altitude (circular orbit), returns Period in seconds
    public static double calculatePeriodFromAltitude(Satellite satellite) {
        double radius = EARTH_RADIUS + satellite.getAltitude();
        if (radius <= 0) {
            throw new IllegalArgumentException("Effective radius must be positive.");
        }
        return 2 * Math.PI * Math.sqrt(Math.pow(radius, 3) / MU_EARTH);
    }

    //Default period calculation (speed if provided, else altitude).
    public static double calculatePeriod(Satellite satellite) {
        return (calculatePeriodFromAltitude(satellite));
    }

    //Calculates circular orbit speed for a given altitude, Speed in m/s
    public static double calculateCircularSpeed(Satellite satellite) {
        double radius = EARTH_RADIUS + satellite.getAltitude();
        return Math.sqrt(MU_EARTH / radius);
    }
    //function test for tyler B----------------------------------------------
    public static void main(String[] args) {
        Satellite testSat = new Satellite(1, "Pegasus", 500, 4, 400_000);
        
        double period = OrbitalPeriod.calculatePeriod(testSat);
        double circSpeed = OrbitalPeriod.calculateCircularSpeed(testSat);
        
        System.out.println("Orbital period in seconds: " + period);
        System.out.println("Circular speed m/s: " + circSpeed);
    }
}