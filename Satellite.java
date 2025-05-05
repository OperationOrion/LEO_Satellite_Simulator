package application;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

    public class Satellite {
        private int satellite_ID = 0;
        private final int user_ID;
        private final String id;
        private final Box body;
        private final double mass;      // in kg
        private final double altitude;  // in meters
        private final double speed = 0;     // in m/s (optional for now)
        private final double area;
        private double angle = 0;

        // Hot FIX by Mitch M 4/16./25
        private final double orbitRadius; // Visual orbit radius in simulation units
        private final double orbitSpeed = 0.5;
        private static final double EARTH_VISUAL_RADIUS = 200.0; // Matches Sphere radius in Earth.java CHANGE YOUR sphere() object creation to 200 in earth.java plse-M
        private static final double EARTH_REAL_RADIUS = 6.371e6; // Real Earth radius in meters
        //OPTIONAL FOR TEAM DISCUSSION
        //Multiply Earth visual Radius by a scaling number, start with value scaling of 5 and go from there if team decides different?
        //Right now this is gonna just be left as is to exactly mathematically accurate -Mitch
        private static final double SCALE_FACTOR = EARTH_VISUAL_RADIUS / EARTH_REAL_RADIUS; // Visual units per meter
        // private final Sphere sphere = Earth.getSphere();

        public Satellite(int user_ID, String id, double mass, double area, double altitude) {
            if (mass <= 0 || area <= 0 || altitude < 0) {
                throw new IllegalArgumentException("Mass and area must be positive, altitude non-negative.");
            }
            this.user_ID = user_ID;
            this.id = id;
            this.mass = mass;
            this.altitude = altitude;
            this.area = area;

            // Calculate visual orbit radius: Earth's visual radius + scaled altitude
            this.orbitRadius = EARTH_VISUAL_RADIUS + (altitude * SCALE_FACTOR);

            // Build visual representation
            this.body = createVisual();
        }

//Make Satellite Image
    private Box createVisual() {
        // Use area to estimate width/height visually (simplified to square face)
        double size = 5; // Scaling factor for visibility TONED down for better visual -Mitch
        Box visual = new Box(size, size, size / 2);

        PhongMaterial material = new PhongMaterial();
        try {
            // Use relative resource path starting with "/"
        	String texturePath = "file:C:/Computer Science Major/ProjectOrionV2/src/Images/satellite_texture.jpg";
            Image texture = new Image(texturePath);
            material.setDiffuseMap(texture);
        } catch (Exception e) {
            System.err.println("Error loading satellite texture: " + e.getMessage());
            // Optionally set a default color if the texture fails to load
            material.setDiffuseColor(javafx.scene.paint.Color.GRAY);
        }
        visual.setMaterial(material);

        return visual;
    }

    // Accessors
    public void setSatellite_ID(int satellite_ID)  {
        this.satellite_ID=satellite_ID;
    }
    public int getSatellite_ID() {
        return satellite_ID;
    }
    public int getUser_Id() {
        return user_ID;
    }

    public String getId() {
        return this.id;
    }

    public double getMass() {
        return mass;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getSpeed() {
        return speed;
    }

    public double getArea() {
        return area;
    }

    public double getOrbitRadius() {
        return orbitRadius;
    }

    public double getOrbitSpeed() {
        return orbitSpeed;
    }

    public double getAngle() {
        return angle;
    }

    public Box getBody() {
        return body;
    }

    //Passes parameters to modify the satellites size and distance visuals
    public void updatePosition() {
        angle += orbitSpeed;
        double radians = Math.toRadians(angle);
        double x = orbitRadius * Math.cos(radians);
        double z = orbitRadius * Math.sin(radians);
        body.setTranslateX(x);
        body.setTranslateZ(z);
    }
}