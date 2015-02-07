package jmini3d.demo;

import java.util.Arrays;
import java.util.Random;

import jmini3d.Color4;
import jmini3d.CubeMapTexture;
import jmini3d.Object3d;
import jmini3d.Texture;
import jmini3d.Vector3;
import jmini3d.geometry.BoxGeometry;
import jmini3d.geometry.Geometry;
import jmini3d.geometry.SkyboxGeometry;
import jmini3d.geometry.VariableGeometry;
import jmini3d.material.Material;

public class RubikSceneFlickering extends ParentScene {

    long initialTimeMovement;
    long initialTime;
    Vector3 direction = new Vector3(0, 1, 0);
    Vector3 side = new Vector3(1, 0, 0);
    Vector3 up = new Vector3(0, 0, 1);

    Object3d o3d;
    Object3d rotationGroup = new Object3d();
    int nextRotationAxis = -1;
    int nextRotationSection;
    float nextRotationAngle;
    Random random = new Random();
    Vector3 axis[] = {
            new Vector3(1, 0, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, 0, 1),
    };
    Vector3 positions[] = new Vector3[9];

    public RubikSceneFlickering() {
        super("Rubik demo, flickering");

        CubeMapTexture envMap = new CubeMapTexture(new String[]{"posx.png", "negx.png", "posy.png", "negy.png", "posz.png", "negz.png"});

        VariableGeometry skyboxGeometry = new SkyboxGeometry(300);
        Material skyboxMaterial = new Material();
        skyboxMaterial.setEnvMap(envMap, 0);
        skyboxMaterial.setUseEnvMapAsMap(true);
        Object3d skybox = new Object3d(skyboxGeometry, skyboxMaterial);
        addChild(skybox);

		Texture map = new Texture("cube.png");
        Material material1 = new Material(map);
        material1.setUseVertexColors(true);
        Geometry geometry = new BoxGeometry(1);

        Color4 colors[][] = {
                {new Color4(200, 0, 0, 255), // x+
                        new Color4(200, 200, 0, 255)}, // x-
                {new Color4(200, 200, 200, 255), // y+
                        new Color4(0, 175, 0, 255)}, // y-
                {new Color4(0, 0, 175, 255), // z+
                        new Color4(200, 100, 0, 255)}, // z-
        };

        o3d = new Object3d();
        for (int iz = -1; iz <= 1; ++iz) {
            for (int iy = -1; iy <= 1; ++iy) {
                for (int ix = -1; ix <= 1; ++ix) {
                    Object3d piece = new Object3d(geometry, material1);
                    piece.setScale(0.5f);
                    piece.setPosition(ix * 1f, iy * 1f, iz * 1f);
                    o3d.addChild(piece);

                    float color[] = new float[6 * 4 * 4];
                    Arrays.fill(color, 0f);
                    int vertexIndex = 0;
                    int index[] = {ix, iy, iz};
                    for (int i = 0; i < geometry.vertex().length; ) {
                        for (int coordinate = 0; coordinate < 3; ++coordinate) {
                            if ((index[coordinate] > 0) && (geometry.vertex()[i] > 0) && (geometry.normals()[i] > 0)) {
                                color[vertexIndex] = colors[coordinate][0].r;
                                color[vertexIndex + 1] = colors[coordinate][0].g;
                                color[vertexIndex + 2] = colors[coordinate][0].b;
                                color[vertexIndex + 3] = colors[coordinate][0].a;
                            }
                            if ((index[coordinate] < 0) && (geometry.vertex()[i] < 0) && (geometry.normals()[i] < 0)) {
                                color[vertexIndex] = colors[coordinate][1].r;
                                color[vertexIndex + 1] = colors[coordinate][1].g;
                                color[vertexIndex + 2] = colors[coordinate][1].b;
                                color[vertexIndex + 3] = colors[coordinate][1].a;
                            }
                            ++i;
                        }
                        vertexIndex += 4;
                    }
                    piece.setVertexColors(color);
                }
            }
        }

        o3d.setPosition(0, 0, 0);
        o3d.setScale(0.35f);

        addChild(o3d);

        initialTime = System.currentTimeMillis();

        for (int i = 0; i < positions.length; ++i) {
            positions[i] = new Vector3(0, 0, 0);
        }
    }

    private void rotate(int axis, float angle, Vector3 v) {
        v.rotateAxis(this.axis[axis], angle);
    }

    private void rotate(int axis, float angle, Vector3 direction, Vector3 up, Vector3 side) {
        rotate(axis, angle, direction);
        rotate(axis, angle, side);
        rotate(axis, angle, up);
    }

    public void update() {

        direction.setAllFrom(axis[1]);
        side.setAllFrom(axis[0]);
        up.setAllFrom(axis[2]);
        long ellapsedTime = System.currentTimeMillis() - initialTime;
        rotate(2, (float) Math.toRadians(ellapsedTime / 10f), direction, up, side);
        rotate(0, (float) Math.toRadians(ellapsedTime / 21f), direction, up, side);
        o3d.setRotationMatrix(direction, up, side);

        if (nextRotationAxis == -1) {

            initialTimeMovement = System.currentTimeMillis();

            // decide next movement (axis, section and angle)
            nextRotationAxis = random.nextInt(3); // 0,1,2
            nextRotationSection = random.nextInt(3) - 1; // -1,0,+1
            int nextRotation = random.nextInt(4) - 1;
            if (nextRotation <= 0) {
                --nextRotation; // -2,-1,+1,+2
            }
            nextRotationAngle = 90.0f*nextRotation; // -180,-90,+90,+180
            rotationGroup.getChildren().clear();
            int i = 0;
            int j = 0;
            for (Object3d piece : o3d.getChildren()) {
                if (Math.abs(Vector3.dot(piece.getPosition(), axis[nextRotationAxis]) - nextRotationSection) < 1e-2) {
                    rotationGroup.addChild(piece);
                    positions[j++].setAllFrom(piece.getPosition());
                }
            }
            // and add them to a new object
            for (Object3d piece : rotationGroup.getChildren()) {
                o3d.removeChild(piece);
            }
            o3d.addChild(rotationGroup);
        } else if (nextRotationAxis >= 0) {

            float step = (System.currentTimeMillis() - initialTimeMovement) / 3f;
            float angle = step*Math.signum(nextRotationAngle);

            if (Math.abs(angle) >= Math.abs(nextRotationAngle)) {
                angle = nextRotationAngle;
                rotate(nextRotationAxis, (float) Math.toRadians(angle), direction, up, side);
                o3d.removeChild(rotationGroup);
                for (Object3d piece : rotationGroup.getChildren()) {
                    piece.getRotationMatrix(direction, up, side);
                    rotate(nextRotationAxis, (float) Math.toRadians(angle), direction, up, side);
                    piece.setRotationMatrix(direction, up, side);
                    rotate(nextRotationAxis, (float) Math.toRadians(angle), piece.getPosition());
                    o3d.addChild(piece);
                }
                rotationGroup.getChildren().clear();
            } else {
                direction.setAllFrom(axis[1]);
                side.setAllFrom(axis[0]);
                up.setAllFrom(axis[2]);
                rotate(nextRotationAxis, (float) Math.toRadians(angle), direction, up, side);
                rotationGroup.setRotationMatrix(direction, up, side);
            }

            if (angle == nextRotationAngle) {
                nextRotationAxis = -1;
            }
        }
    }
}
