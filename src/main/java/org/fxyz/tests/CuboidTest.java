package org.fxyz.tests;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxyz.cameras.CameraTransformer;
import org.fxyz.geometry.Point3D;
import org.fxyz.shapes.Spheroid;
import org.fxyz.shapes.primitives.CuboidMesh;
import org.fxyz.shapes.primitives.SegmentedTorusMesh;
import org.fxyz.shapes.primitives.SpheroidMesh;
import org.fxyz.utils.Axes;
import org.fxyz.utils.DensityFunction;

/**
 *
 * @author jpereda
 */
public class CuboidTest extends Application {
    private PerspectiveCamera camera;
    private final double sceneWidth = 600;
    private final double sceneHeight = 600;
    private final CameraTransformer cameraTransform = new CameraTransformer();
    
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private SpheroidMesh cuboid;
    private Rotate rotateY;
    
    private DensityFunction<Point3D> dens = p-> (double)p.x;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Group sceneRoot = new Group();
        Scene scene = new Scene(sceneRoot, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);
        camera = new PerspectiveCamera(true);        
     
        //setup camera transform for rotational support
        cameraTransform.setTranslate(0, 0, 0);
        cameraTransform.getChildren().add(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1300);
        cameraTransform.ry.setAngle(-45.0);
        cameraTransform.rx.setAngle(-10.0);
        //add a Point Light for better viewing of the grid coordinate system
        PointLight light = new PointLight(Color.WHITE);
        cameraTransform.getChildren().add(light);
        light.setTranslateX(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateY());
        light.setTranslateZ(camera.getTranslateZ());        
        scene.setCamera(camera);
        
        rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        Group group = new Group();
        group.getChildren().add(cameraTransform);    
//        cuboid = new Spheroid(64,300d,300d, Color.GREEN);
//        cuboid = new SegmentedTorusMesh(50, 42, 0, 500d, 300d); 
        cuboid = new SpheroidMesh(64, 300,300);
         PhongMaterial matTorus = new PhongMaterial(Color.ORANGE);
         cuboid.setMaterial(matTorus);
        cuboid.setDrawMode(DrawMode.FILL);
//        cuboid.setCullFace(CullFace.NONE);
    // NONE
//        cuboid.setTextureModeNone(Color.ROYALBLUE);
        // IMAGE
//        cuboid.setTextureModeImage(getClass().getResource("res/netCuboid.png").toExternalForm());
    // DENSITY
//        cuboid.setTextureModeVertices3D(256*256,p->(double)p.x*p.y*p.z);
    // FACES
//        cuboid.setTextureModeFaces(1530);

        
        cuboid.getTransforms().addAll(new Rotate(0,Rotate.X_AXIS),rotateY);
        group.getChildren().add(cuboid);
        
        sceneRoot.getChildren().addAll(group);        
        
        //First person shooter keyboard movement 
        scene.setOnKeyPressed(event -> {
            double change = 10.0;
            //Add shift modifier to simulate "Running Speed"
            if(event.isShiftDown()) { change = 50.0; }
            //What key did the user press?
            KeyCode keycode = event.getCode();
            //Step 2c: Add Zoom controls
            if(keycode == KeyCode.W) { camera.setTranslateZ(camera.getTranslateZ() + change); }
            if(keycode == KeyCode.S) { camera.setTranslateZ(camera.getTranslateZ() - change); }
            //Step 2d:  Add Strafe controls
            if(keycode == KeyCode.A) { camera.setTranslateX(camera.getTranslateX() - change); }
            if(keycode == KeyCode.D) { camera.setTranslateX(camera.getTranslateX() + change); }
        });        
        
        scene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);
            
            double modifier = 10.0;
            double modifierFactor = 0.1;
            
            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 50.0;
            }
            if (me.isPrimaryButtonDown()) {
                cameraTransform.ry.setAngle(((cameraTransform.ry.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // +
                cameraTransform.rx.setAngle(((cameraTransform.rx.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);  // -
            } else if (me.isSecondaryButtonDown()) {
                double z = camera.getTranslateZ();
                double newZ = z + mouseDeltaX * modifierFactor * modifier;
                camera.setTranslateZ(newZ);
            } else if (me.isMiddleButtonDown()) {
                cameraTransform.t.setX(cameraTransform.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                cameraTransform.t.setY(cameraTransform.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
            }
        });
        
        
        final Timeline bannerEffect = new Timeline();
        bannerEffect.setCycleCount(Timeline.INDEFINITE);
        final KeyValue kv1 = new KeyValue(rotateY.angleProperty(), 360);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(10000), kv1);
        bannerEffect.getKeyFrames().addAll(kf1);
        bannerEffect.play();
        
        lastEffect = System.nanoTime();
        AtomicInteger count=new AtomicInteger();
        AnimationTimer timerEffect = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (now > lastEffect + 1_000_000l) {
//                    dens = p->(float)(p.x*Math.cos(count.get()%100d*2d*Math.PI/50d)+p.y*Math.sin(count.get()%100d*2d*Math.PI/50d));
//                    torus.setDensity(dens);
                    
//                    if(count.get()%100<50){
//                        torus.setDrawMode(DrawMode.LINE);
//                    } else {
//                        torus.setDrawMode(DrawMode.FILL);
//                    }
//                    torus.setColors((int)Math.pow(2,count.get()%16));
//                    torus.setMajorRadius(500+100*(count.get()%10));
//                    torus.setMinorRadius(150+10*(count.get()%10));
//                    torus.setMinorRadius(torus.getMinorRadius() + 1);
//                    torus.setPatternScale();
                    count.getAndIncrement();
                    cuboid.updateFigure();
                    lastEffect = now;
                }
            }
        };
        
        primaryStage.setTitle("F(X)yz - Cuboid Test");
        primaryStage.setScene(scene);
        primaryStage.show();        
        timerEffect.start();
    }
    private long lastEffect;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }    
}
