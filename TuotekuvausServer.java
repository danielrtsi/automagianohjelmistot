package aalto.ELECC8204.tuotekuvaus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Identifiers;
import org.xml.sax.SAXException;

import com.prosysopc.ua.ModelException;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServerException;

import aalto.ELECC8204.opcua.OPCUAServer;
import aalto.ELECC8204.codegen.DigitalProductDescription.BoltAngularType;
import aalto.ELECC8204.codegen.DigitalProductDescription.BoltType;
import aalto.ELECC8204.codegen.DigitalProductDescription.DigitalTwinType;
import aalto.ELECC8204.codegen.DigitalProductDescription.FacePlateBackType;
import aalto.ELECC8204.codegen.DigitalProductDescription.FacePlateFrontType;
import aalto.ELECC8204.codegen.DigitalProductDescription.PendulumType;
import aalto.ELECC8204.codegen.DigitalProductDescription.RectangleLegoType;
import aalto.ELECC8204.codegen.DigitalProductDescription.ShaftType;
import aalto.ELECC8204.codegen.DigitalProductDescription.SquareLegoType;

public class TuotekuvausServer extends OPCUAServer {
	public static int TCP_PORT = 52523;
	public static int HTTPS_PORT = 52446;
	public static String APP_NAME = "TuotekuvausServer";
	public static Map<String, SquareLegoType> squares = new HashMap<String, SquareLegoType>();
	public static Map<String, RectangleLegoType> rects = new HashMap<String, RectangleLegoType>();
	protected NodeManagerUaNode manager;
	protected UaObject objectsFolder;
	protected NodeId hasConnectionId;
	protected int namespaceIndex;
	private static final Logger logger = LogManager.getLogger(TuotekuvausServer.class);
	
	public TuotekuvausServer(int tcpPort, int httpsPort, String appName) throws SecureIdentityException, IOException, UaServerException, StatusException, ServiceException, SAXException, ModelException, URISyntaxException {
		super(tcpPort, httpsPort, appName);
		server.registerModel(aalto.ELECC8204.codegen.DigitalProductDescription.server.ServerInformationModel.MODEL);
		server.getAddressSpace()
				.loadModel(aalto.ELECC8204.codegen.DigitalProductDescription.server.ServerInformationModel.class
						.getResource("DigitalProductDescription.xml").toURI());
	}

	/**
	 * @param args command line arguments for the application
	 * 
	 */
	public static void main(String[] args) throws Exception {
		TuotekuvausServer tuotekuvausServer = new TuotekuvausServer(TCP_PORT, HTTPS_PORT, APP_NAME);
		tuotekuvausServer.createAddressSpace();
		tuotekuvausServer.run();
	}

	/**
	 * Load a generated address space
	 */
	@Override
	protected void createAddressSpace()
			throws SAXException, IOException, ModelException, URISyntaxException, StatusException, ServiceException {
		String namespaceUri = "http://www.aalto.com/OPCUA/KoodigenerointiAddressSpace";
		manager = new NodeManagerUaNode(server, namespaceUri);
		
		objectsFolder = server.getNodeManagerRoot().getObjectsFolder();
		hasConnectionId = manager.getNode(aalto.ELECC8204.codegen.DigitalProductDescription.Ids.HasConnection).getNodeId();
		namespaceIndex = manager.getNamespaceIndex();
		createProduct1();
		createProduct2();

		DigitalTwinType dtt = manager.createInstance(DigitalTwinType.class, "lego.txt", new NodeId(namespaceIndex, 20));
		manager.addNodeAndReference(objectsFolder, dtt, Identifiers.Organizes);

		for (int i = 1; i <= 13; i++) {
			String key = "Square" + String.valueOf(i);
			SquareLegoType slt = manager.createInstance(SquareLegoType.class, key, new NodeId(namespaceIndex, UUID.randomUUID()));
			slt.setColor("yellow");

			manager.addNodeAndReference(dtt.getDigitalPartsNode(), slt, Identifiers.HasComponent);
			squares.put(key, slt);
		}

		for (int i = 0; i <= 10; i++) {
			String key = "rect" + String.valueOf(i+1);
			String colour = i % 3 == 0 ? "green" : (i % 3 == 1 ? "pink" : "blue");
			String orientation = "east";

			if (i == 0 || i == 6 || i ==7) {
				orientation = "north";
			} else if (i == 2 || i == 3) {
				orientation = "south";
			} else if (i == 4 || i == 5) {
				orientation = "west";
			}

			RectangleLegoType rlt = manager.createInstance(RectangleLegoType.class, key, new NodeId(namespaceIndex, UUID.randomUUID()));

			rlt.setColor(colour);
			rlt.setOrientation(orientation);

			manager.addNodeAndReference(dtt.getDigitalPartsNode(), rlt, Identifiers.HasComponent);
			rects.put(key, rlt);

		}

		createProduct0();

		logger.info("Address space created.");
	}
	
	protected void createProduct0() throws StatusException, ServiceException {
		/**
		 * TODO Luo legotorni osoiteavaruuteen
		 */
		
		
		
		for (int i = 1; i < 13; i++) {
			UaNode sourceNode = squares.get("Square" + i).getBottomNode();
			UaNode targetNode = squares.get("Square" + (1 + i)).getTopNode();
			manager.addReference(sourceNode, targetNode, hasConnectionId, false);
		}
		for (int i = 1; i < 11; i++) {
			UaNode sourceNode = rects.get("rect" + (1 + i)).getTopANode();
			UaNode targetNode = rects.get("rect" + i).getBottomCNode();
			manager.addReference(sourceNode, targetNode, hasConnectionId, false);
		}
		UaNode sourceNode = rects.get("rect1").getBottomANode();
		UaNode targetNode = squares.get("Square1").getTopNode();
		manager.addReference(sourceNode, targetNode, hasConnectionId, false);
		
	}

	protected void createProduct1() throws StatusException {
		final NodeId digitalTwinId = new NodeId(namespaceIndex, 21);
		DigitalTwinType digitalTwin = manager.createInstance(DigitalTwinType.class, "lego tower", digitalTwinId);
		manager.addNodeAndReference(objectsFolder, digitalTwin, Identifiers.Organizes);
		Map<String, SquareLegoType> squares = new HashMap<String, SquareLegoType>();
		for (int i = 1; i < 24; i++) {
			final NodeId legoSquareId = new NodeId(namespaceIndex, UUID.randomUUID());
			SquareLegoType squareLego = manager.createInstance(SquareLegoType.class, "Square" + i, legoSquareId);
			if (i == 1 || i == 5 || i == 8)
				squareLego.setColor("pink");
			else if (i == 2 || i == 6 || i == 10)
				squareLego.setColor("green");
			else if (i == 3 || i == 7 || i == 11 || i == 12)
				squareLego.setColor("blue");
			else
				squareLego.setColor("yellow");
			manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), squareLego, Identifiers.HasComponent);
			squares.put("Square" + i, squareLego);
		}
		Map<String, RectangleLegoType> rects = new HashMap<String, RectangleLegoType>();
		for (int i = 1; i < 10; i++) {
			final NodeId legoRectId = new NodeId(namespaceIndex, UUID.randomUUID());
			RectangleLegoType rectLego = manager.createInstance(RectangleLegoType.class, "Rectangle" + i, legoRectId);
			if (i == 3)
				rectLego.setColor("blue");
			else if (i == 2)
				rectLego.setColor("pink");
			else if (i == 1 || i == 5 || i == 6 || i == 8 || i == 9)
				rectLego.setColor("yellow");
			else
				rectLego.setColor("green");
			if (i == 3) {
				rectLego.setOrientation("north");
			} else if (i == 4) {
				rectLego.setOrientation("south");
			} else {
				rectLego.setOrientation("east");
			}
			manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), rectLego, Identifiers.HasComponent);
			rects.put("Rectangle" + i, rectLego);
		}
		
		manager.addReference(squares.get("Square17").getBottomNode(), squares.get("Square16").getTopNode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle4").getTopANode(), squares.get("Square13").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square16").getBottomNode(), squares.get("Square15").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square14").getTopNode(), squares.get("Square15").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square13").getTopNode(), squares.get("Square14").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle3").getTopANode(), rects.get("Rectangle4").getBottomANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle4").getBottomCNode(), squares.get("Square4").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square4").getBottomNode(), squares.get("Square5").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square5").getBottomNode(), squares.get("Square6").getTopNode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle2").getTopANode(), rects.get("Rectangle3").getBottomANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle2").getBottomCNode(), squares.get("Square1").getTopNode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle1").getTopCNode(), rects.get("Rectangle2").getBottomANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle3").getBottomCNode(), squares.get("Square2").getTopNode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle2").getTopCNode(), squares.get("Square11").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square12").getBottomNode(), squares.get("Square11").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square2").getBottomNode(), squares.get("Square3").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square10").getBottomNode(), rects.get("Rectangle3").getTopCNode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle1").getTopANode(), squares.get("Square7").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square7").getTopNode(), squares.get("Square8").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square8").getTopNode(), squares.get("Square9").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square12").getTopNode(), rects.get("Rectangle5").getBottomANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle5").getTopANode(), squares.get("Square19").getBottomNode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle5").getTopCNode(), rects.get("Rectangle6").getBottomANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle6").getBottomCNode(), rects.get("Rectangle7").getTopANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle7").getTopCNode(), rects.get("Rectangle8").getBottomANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle8").getBottomCNode(), rects.get("Rectangle9").getTopANode(), hasConnectionId,
				false);
		manager.addReference(rects.get("Rectangle9").getBottomCNode(), squares.get("Square20").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square20").getBottomNode(), squares.get("Square21").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square21").getBottomNode(), squares.get("Square22").getTopNode(), hasConnectionId,
				false);
		manager.addReference(squares.get("Square22").getBottomNode(), squares.get("Square23").getTopNode(), hasConnectionId,
				false);
	}

	protected void createProduct2() throws StatusException {
		final NodeId digitalTwinId = new NodeId(namespaceIndex, 22);
		DigitalTwinType digitalTwin = manager.createInstance(DigitalTwinType.class, "cranfield", digitalTwinId);
		manager.addNodeAndReference(objectsFolder, digitalTwin, Identifiers.Organizes);

		FacePlateBackType back = manager.createInstance(FacePlateBackType.class, "back",
				new NodeId(namespaceIndex, UUID.randomUUID()));
		back.setColor("pink");
		back.setOrientation("0,0,0");
		back.getsquare_leftNode().setY(1.0);
		back.getsquare_rightNode().setY(1.0);
		back.getcircle_leftNode().setY(1.0);
		back.getcircle_rightNode().setY(1.0);
		back.getshaftNode().setY(1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), back, Identifiers.HasComponent);

		FacePlateFrontType front = manager.createInstance(FacePlateFrontType.class, "front",
				new NodeId(namespaceIndex, UUID.randomUUID()));
		front.setColor("pink");
		front.setOrientation("0,0,0");
		front.getbottomNode().setY(-1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), front, Identifiers.HasComponent);

		BoltAngularType boltA1 = manager.createInstance(BoltAngularType.class, "boltA1",
				new NodeId(namespaceIndex, UUID.randomUUID()));
		boltA1.setColor("green");
		boltA1.setOrientation("0,0,0");
		boltA1.getbottomNode().setY(-1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), boltA1, Identifiers.HasComponent);

		BoltAngularType boltA2 = manager.createInstance(BoltAngularType.class, "boltA2",
				new NodeId(namespaceIndex, UUID.randomUUID()));
		boltA2.setColor("green");
		boltA2.setOrientation("0,0,0");
		boltA2.getbottomNode().setY(-1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), boltA2, Identifiers.HasComponent);

		BoltType bolt1 = manager.createInstance(BoltType.class, "bolt1", new NodeId(namespaceIndex, UUID.randomUUID()));
		bolt1.setColor("green");
		bolt1.setOrientation("0,0,0");
		bolt1.getbottomNode().setY(-1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), bolt1, Identifiers.HasComponent);

		BoltType bolt2 = manager.createInstance(BoltType.class, "bolt2", new NodeId(namespaceIndex, UUID.randomUUID()));
		bolt2.setColor("green");
		bolt2.setOrientation("0,0,0");
		bolt2.getbottomNode().setY(-1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), bolt2, Identifiers.HasComponent);

		ShaftType shaft = manager.createInstance(ShaftType.class, "shaft1", new NodeId(namespaceIndex, UUID.randomUUID()));
		shaft.setColor("green");
		shaft.setOrientation("0,0,0");
		shaft.getbottomNode().setY(-1.0);
		shaft.getmiddleNode().setY(0.0);
		shaft.gettopNode().setY(1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), shaft, Identifiers.HasComponent);

		PendulumType pendulum = manager.createInstance(PendulumType.class, "pendulum1",
				new NodeId(namespaceIndex, UUID.randomUUID()));
		pendulum.setColor("yellow");
		pendulum.setOrientation("0,1,0");
		pendulum.getbottomNode().setY(-1.0);
		manager.addNodeAndReference(digitalTwin.getDigitalPartsNode(), pendulum, Identifiers.HasComponent);

		manager.addReference(back.getsquare_leftNode(), boltA1.getbottomNode(), hasConnectionId, true);
		manager.addReference(back.getsquare_rightNode(), boltA2.getbottomNode(), hasConnectionId, true);
		manager.addReference(back.getcircle_leftNode(), bolt1.getbottomNode(), hasConnectionId, true);
		manager.addReference(back.getcircle_rightNode(), bolt2.getbottomNode(), hasConnectionId, true);
		manager.addReference(back.getshaftNode(), shaft.getbottomNode(), hasConnectionId, true);
		manager.addReference(shaft.getmiddleNode(), pendulum.getbottomNode(), hasConnectionId, true);
		manager.addReference(shaft.gettopNode(), front.getbottomNode(), hasConnectionId, true);
	}
}