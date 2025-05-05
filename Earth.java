package application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Earth {
    private static final Sphere sphere = prepareEarth();
    private static final ImageView imageView = prepareImageView();
    //private final Circle orbitCircle;

    // Constructor takes a Satellite object to get altitude
 /*   public Earth(Satellite satellite) {
        this.sphere = prepareEarth();
        this.imageView = prepareImageView();
        this.orbitCircle = prepareOrbitCircle(satellite);
    }
*/
    private static Sphere prepareEarth() {
        Sphere sphere = new Sphere(200);
        PhongMaterial earthMaterial = new PhongMaterial();
        try {
            String earthPath = "file:C:/Computer Science Major/ProjectOrionV2/src/Images/earth-d.jpg";
            earthMaterial.setDiffuseMap(new Image(earthPath));
        } catch (Exception e) {
            System.err.println("Error loading Earth texture: " + e.getMessage());
            earthMaterial.setDiffuseColor(Color.BLUE); // Fallback color
        }

        sphere.setRotationAxis(Rotate.Y_AXIS);
        sphere.setMaterial(earthMaterial);
        return sphere;
    }

 /*   private Circle prepareOrbitCircle(Satellite satellite) {
        // Scale altitude: e.g., 400 km real -> 50 units visual
        double realAltitude = satellite.getAltitude(); // in meters
        double heightAboveSurface = (realAltitude / 400_000) * 50; // Proportional scaling
        double orbitRadius = sphere.getRadius() + heightAboveSurface;

        // Create a 2D circle
        Circle circle = new Circle(orbitRadius);
        circle.setFill(null); // Transparent fill
        circle.setStroke(Color.RED); // Red outline
        circle.setStrokeWidth(2.0); // Thickness of the line

        // Rotate 90 degrees around X-axis to align with equator
        circle.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        // Center it with the sphere
        circle.setTranslateX(0);
        circle.setTranslateY(0);
        circle.setTranslateZ(0);

        return circle;
    }
*/
    private static ImageView prepareImageView() {
        ImageView imageView = new ImageView();
        try {
            // Use file: protocol for absolute paths
            String galaxyPath = "file:C:/Computer Science Major/ProjectOrionV2/src/Images/galaxy.jpg";
            Image image = new Image(galaxyPath);
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.getTransforms().add(new Translate(-image.getWidth() / 2, -image.getHeight() / 2, 800));
        } catch (Exception e) {
            System.err.println("Error loading galaxy image: " + e.getMessage());
        }
        return imageView;
    }


    public static Sphere getSphere() {
        return sphere;
    }

  /*  public Circle getOrbitCircle() {
        return orbitCircle;
    }
*/
    public static ImageView getImageView() {
        return imageView;
    }
}