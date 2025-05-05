package application;

public class Ballistic {
    private static final double DRAG_COEFFICIENT = 2.2; // Typical for satellites
   
    // return Ballistic coefficient in kg/m^2
    public static double calculateBallisticCoefficient(Satellite satellite) {
        return satellite.getMass() / (DRAG_COEFFICIENT * satellite.getArea());
    }
    //function test for tyler B----------------------
    public static void main(String[] args) {
    	Satellite testSat = new Satellite(1,"Zeus", 500, 4, 400_000);
        double coef = Ballistic.calculateBallisticCoefficient(testSat);
        System.out.println("Ballistic coefficient: " + coef + " kg/m^2");
    }
}