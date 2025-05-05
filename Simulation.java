package application;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;


public class Simulation {

    private static final float WIDTH = 1400;
    private static final float HEIGHT = 1000;
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private final Sphere sphere = Earth.getSphere();
    private Satellite satellite;
    private Group world;

   public Simulation(Satellite satellite) {
       this.satellite = satellite;
   }

    public SubScene getSubScene() {
        world = new Group();
        AmbientLight ambient = new AmbientLight(Color.WHITE);
        world.getChildren().add(ambient);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-1000);
        camera.setNearClip(1);
        camera.setFarClip(10000.0);

        world.getChildren().add(sphere);
        // earth = new Earth(satellite);
        world.getChildren().add(satellite.getBody());
        //Getting background
        Group backgroundGroup = new Group();
        backgroundGroup.getChildren().add(Earth.getImageView());

        Group root = new Group();
        root.getChildren().add(backgroundGroup); // static
        root.getChildren().add(world);           // rotates with mouse
        SubScene subScene = new SubScene(root, 1500, 1000, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        initMouseControl(world, camera, subScene);
        prepareAnimation();

        return subScene;
    }

    private void initMouseControl(Group world, Camera camera, SubScene subScene) {
        Rotate xRotate;
        Rotate yRotate;
        world.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        subScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = angleX.get();
            anchorAngleY = angleY.get();
        });

        subScene.setOnMouseDragged(event -> {
            angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
            angleY.set(anchorAngleY + anchorX - event.getSceneX());
        });
    }


    private void prepareAnimation() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                sphere.rotateProperty().set(sphere.getRotate() + 0.2);
                satellite.updatePosition();
            }
        };
        timer.start();
    }
}