package aalto.ELECC8204.cell;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.math.Vector3f;
import java.util.List;
import java.util.Set;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceResultException;

import com.jme3.scene.shape.Sphere;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaReference;

import aalto.ELECC8204.tuotekuvaus.TuotekuvausClient;
import aalto.ELECC8204.codegen.DigitalProductDescription.DigitalPartType;

import java.io.IOException;
import java.net.URISyntaxException;


/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    // 0 = two lego example
    // 1 = lego tower
    // 2 = cranfield
    public static int product = 0;

    public static boolean opcua = true;
    
//    public static Node pivot;
    public DigitalTwin twin;
    public static final float dim = 0.2f;
    static BitmapText helloText;
//    public static RobotArm assemblyArm;
    public static boolean freeze = false;
    public static Geometry markerGeom;
    public static Geometry markerGeom2;
//    public static float floorHeight = -5;
    Cell cell1;
    Node pivot = new Node();
    CADpart pr;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }
 
    @Override
    public void simpleInitApp() {
    	List<DigitalPartType> parts = null;
    	Set<UaReference> connections = null;
    	NodeId twinId = null;
    	boolean sorted = false;
			
//        rootNode.attachChild(pr.node);

        Sphere s = new Sphere(100,100, Main.dim*1.1f);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        markerGeom = new Geometry("Marker", s);
        markerGeom.setMaterial(mat);
//        rootNode.attachChild(markerGeom);
        markerGeom.setLocalTranslation(new Vector3f(0,10,0));

        Sphere s2 = new Sphere(100,100, Main.dim*1.1f);
        Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat2.setColor("Color", ColorRGBA.Green);
        markerGeom2 = new Geometry("Marker", s2);
        markerGeom2.setMaterial(mat2);
//        rootNode.attachChild(markerGeom2);
        markerGeom2.setLocalTranslation(new Vector3f(0,11,0));
        
        flyCam.setMoveSpeed(30);
//        pivot = new Node("pivot");
//        rootNode.attachChild(pivot);
        cell1 = new Cell(assetManager, rootNode);
        cell1.addAssemblyStation(5, -80f*Main.dim+5);
        if((product == 0) || (product == 1)) {
            cell1.addLegoBuffer("rectangle", 5, -500f*Main.dim, 10, 6);
            cell1.addLegoBuffer("square", 300f*Main.dim, -500f*Main.dim, 10, 6);
        } else if (product == 2) {
            cell1.addLegoBuffer("cranfield", 5, -500f*Main.dim, 6, 3);  // rowSize * columnsize should be divisible by 6
        }



        cell1.addMobileRobot(15, -250f*Main.dim);  // robot should be created only after lego buffers
        cell1.attachNodeToRoot(false);
        
        // create the floor with a custom Mesh
//        rootNode.attachChild(createFloor());
        
        PointLight lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.White);
        lamp_light.setRadius(400f);
        lamp_light.setPosition(new Vector3f(2f, 8.0f, 10.0f));
        rootNode.addLight(lamp_light);
        

                /** Display a line of text (default font from test-data) */
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        helloText = new BitmapText(guiFont, false);
        helloText.setColor(ColorRGBA.Cyan);
        helloText.setSize(60); //guiFont.getCharSet().getRenderedSize());
        helloText.setText("Virtual assembly");
        helloText.setLocalTranslation(500, helloText.getLineHeight(), 0);
        guiNode.attachChild(helloText);

        if(product == 0) {
            twinId = new NodeId(3,20);
        } else if(product == 1) {
        	twinId = new NodeId(3,21);          
        } else if (product == 2) {
        	twinId = new NodeId(3,22);
        	// DigitalTwin will fail to place parts correctly
        	// if it does not get the lowest part first.
        	sorted = true;
        }
        if(opcua) {
        	/*
        	 * Creating a instance of OPC UA client and starting it.
        	 * Works with a non-secure connection.
        	 */
    		try {
    			TuotekuvausClient uaClient = new TuotekuvausClient(TuotekuvausClient.APP_NAME, TuotekuvausClient.SERVER_ENDPOINT);
	        	parts = uaClient.getParts(twinId, sorted);
	        	connections = uaClient.getAllConnections(twinId);
				twin = new DigitalTwin(parts, connections, assetManager, cell1, uaClient);       
				uaClient.stop();
    		} catch (StatusException | ServiceException | SecureIdentityException | IOException | URISyntaxException
					| ServiceResultException | AddressSpaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            if(product == 0) {
                twin = new DigitalTwin("/Lego.txt", assetManager, cell1);            
            } else if(product == 1) {
                twin = new DigitalTwin("/Lego_example.txt", assetManager, cell1);            
            } else if (product == 2) {
                twin = new DigitalTwin("/Cranfield.txt", assetManager, cell1);
            }
        }
	        
        rootNode.attachChild(twin.node);

    }


    @Override
    public void simpleUpdate(float tpf) {
        cell1.execute();
        if (!freeze) {
           twin.execute(tpf);
        }
    }
    
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
