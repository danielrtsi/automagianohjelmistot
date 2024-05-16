package aalto.ELECC8204.tuotekuvaus;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.ReferenceDescription;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;

import aalto.ELECC8204.codegen.DigitalProductDescription.CoordinateType;
import aalto.ELECC8204.codegen.DigitalProductDescription.DigitalPartType;
import aalto.ELECC8204.codegen.DigitalProductDescription.DigitalTwinType;

import aalto.ELECC8204.opcua.OPCUAClient;


public class TuotekuvausClient extends OPCUAClient {
	public static String APP_NAME = "TuotekuvausClient";
	public static String SERVER_ENDPOINT = "opc.tcp://localhost:52523/OPCUA/TuotekuvausServer";
	protected NodeId hasConnectionPointId;
	protected NodeId hasConnectionId;
	private static final Logger logger = LogManager.getLogger(TuotekuvausClient.class);

	public TuotekuvausClient(String appName, String serverEndpoint) throws UnknownHostException, StatusException, ServiceException, URISyntaxException, SecureIdentityException, IOException, ServiceResultException, AddressSpaceException {
		super(appName, serverEndpoint);
		logger.debug("Registering a new model");
		client.registerModel(aalto.ELECC8204.codegen.DigitalProductDescription.client.ClientInformationModel.MODEL);
		hasConnectionPointId = client.getAddressSpace()
				.getNode(aalto.ELECC8204.codegen.DigitalProductDescription.Ids.HasConnectionPoint).getNodeId();
		hasConnectionId = client.getAddressSpace()
				.getNode(aalto.ELECC8204.codegen.DigitalProductDescription.Ids.HasConnection).getNodeId();
	}
	
	/**
	 * Retrieves DigitalTwin's parts from address space.
	 * Param1: NodeId of digitalTwin.
	 * Param2: boolean to decide whether the part are sorted or not
	 * 
	 * @param digitalTwinId - NodeId of digitalTwin
	 * @param sorted - boolean to decide whether the part are sorted or not
	 * @return parts of digitalTwin
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 * @throws StatusException
	 */
	public List<DigitalPartType> getParts(NodeId digitalTwinId, boolean sorted) throws ServiceException, AddressSpaceException, StatusException {
		DigitalTwinType dtt = (DigitalTwinType) super.client.getAddressSpace().getNode(digitalTwinId);
		UaNode[] components = dtt.getDigitalPartsNode().getComponents();
		ArrayList<DigitalPartType> digitalPartList = new ArrayList<DigitalPartType>();

		for (UaNode node : components) {
			digitalPartList.add((DigitalPartType) node);
			//System.out.println(node.getBrowseName());
		}



		return digitalPartList;
	}
	
	/**
	 * Compares part's type to generated namespace types for example 
	 * aalto.types.DigitalProductDescription.Ids.SquareLegoType
	 * 
	 * @param part which type is compared
	 * @param typesExpNodeId ExpandedNodeId of a type that's compared to
	 * @return true if types are a match, false otherwise.
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 */
	public boolean compareType(DigitalPartType part, ExpandedNodeId typesExpNodeId) throws ServiceException, AddressSpaceException {
		if(part.getTypeDefinition().getNodeId() == client.getAddressSpace().getNode(typesExpNodeId).getNodeId())
			return true;
		return false;
	}
	
	/**
	 * Retrieves all types that are subtypes of CadPartType
	 * @return subtypes of CadPartType
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 */
	public UaNode[] getCadTypes() throws ServiceException, AddressSpaceException {
		UaNode cadPartType = client.getAddressSpace().getNode(aalto.ELECC8204.codegen.DigitalProductDescription.Ids.CadPartType);
		UaReference[] refs = cadPartType.getForwardReferences(Identifiers.HasSubtype);
		UaNode[] types = new UaNode[refs.length];
		for(int i=0; i<refs.length; i++) {
			types[i] = refs[i].getTargetNode();
		}
		return types;
	}
	
	/**
	 * Gets parts connectionPoints.
	 * @param part
	 * @return ConnectionPoints of a part
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 */
	public CoordinateType[] getConnectionPoints(UaNode part) throws ServiceException, AddressSpaceException {
		UaReference[] forwardRefs = part.getForwardReferences(this.hasConnectionPointId);
		CoordinateType[] ct = new CoordinateType[forwardRefs.length];
		for (int i = 0; i < forwardRefs.length; i++) {
			ct[i] = (CoordinateType) forwardRefs[i].getTargetNode();
		}
		return ct; 
	}
	
	/**
	 * Gets connections that parts connectionPoints has to another parts connectionPoints
	 * @param part
	 * @return HasConnection references from parts connectioPoints
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 */
	public Set<UaReference> getConnections(DigitalPartType part) throws ServiceException, AddressSpaceException {
		CoordinateType[] ct = this.getConnectionPoints(part);
		Set<UaReference> connections = new HashSet<UaReference>();
		for (CoordinateType cp : ct) {
			UaReference ur = cp.getReference(hasConnectionId, false);
			if (ur != null) connections.add(ur);
			ur = cp.getReference(hasConnectionId, true);
			if (ur != null) connections.add(ur);
		}
		return connections;
	}
	
	/**
	 * Gets all connections between all connectionPoints
	 * @param twinId
	 * @return All HasConnection references between connectionPoints of parts that belong to DigitalTwin.
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 * @throws StatusException
	 */
	public Set<UaReference> getAllConnections(NodeId twinId) throws ServiceException, AddressSpaceException, StatusException{
		Set<UaReference> connections = new HashSet<UaReference>();
		this.getParts(twinId, false).forEach(dpt -> {
			try {
				connections.addAll(this.getConnections(dpt));
			} catch (ServiceException | AddressSpaceException e) {
				e.printStackTrace();
			}
		});

		for (UaReference connection : connections) {
			//System.out.println(connection.getTargetNode().getBrowseName());
		}

		return connections;
	}
	
	/**
	 * Gets part from its connectionPoint
	 * @param connectionPoint
	 * @return Part of connectionPoint
	 */
	public DigitalPartType getPart(UaNode connectionPoint) {
		UaReference ref = connectionPoint.getReference(hasConnectionPointId, true);
		if(ref != null) {
			return (DigitalPartType) ref.getSourceNode();
		}
		return null;
	}
	
	/**
	 * Goes through all parts and determines the coordinates of each part in relation to the center of the part it started.
	 * ConnectionPoints have the information about the location related to the center of the part.
	 * This function uses that information.
	 * 
	 * TODO orientation support is not implemented.
	 * 
	 * @param digitalTwinId
	 * @return
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 * @throws StatusException
	 */
	protected Map<DigitalPartType, Coordinate> getRelativeCoordinates(NodeId digitalTwinId) throws ServiceException, AddressSpaceException, StatusException {
		DigitalTwinType twin = (DigitalTwinType) client.getAddressSpace().getNode(digitalTwinId);
		UaNode partsNode = twin.getDigitalPartsNode();
		UaNode[] parts = partsNode.getComponents();
		if(parts.length == 0)
			return null;
		DigitalPartType part = (DigitalPartType) parts[0];
		Map<DigitalPartType, Coordinate> visited = new HashMap<DigitalPartType, Coordinate>();
		LinkedList<DigitalPartType> queue = new LinkedList<DigitalPartType>();
		Coordinate relativeCoordinate = new Coordinate(0,0,0);
		visited.put(part, relativeCoordinate);
		queue.add(part);
		DigitalPartType next;
		while (queue.size() != 0){
            next = queue.poll();
            Iterator<ConnectedPart> i = getConnectedParts(next).listIterator();
            while (i.hasNext()){
            	ConnectedPart p = i.next();
                DigitalPartType n = p.part;
                if (!visited.containsKey(n)){
                	relativeCoordinate = new Coordinate(visited.get(next));
                	relativeCoordinate.add(p.relativeCoordinate);
                	visited.put(n, relativeCoordinate);
                	queue.add(n);
                }
            }
        }
		return visited;
	}
	
	/**
	 * Gets parts that are connected to the part that is given as parameter.
	 * Uses helper class to store info of the relative locations of parts.
	 * @param part
	 * @return
	 * @throws ServiceException
	 * @throws AddressSpaceException
	 * @throws StatusException
	 */
	protected LinkedList<ConnectedPart> getConnectedParts(DigitalPartType part) throws ServiceException, AddressSpaceException, StatusException {
		LinkedList<ConnectedPart> nextParts = new LinkedList<ConnectedPart>();
		for(CoordinateType connectionPoint : getConnectionPoints(part)) {
			List<ReferenceDescription> connections;
			connections = client.getAddressSpace().browse(connectionPoint.getNodeId(), BrowseDirection.Forward,
					hasConnectionId);
			connections.addAll(client.getAddressSpace().browse(connectionPoint.getNodeId(), BrowseDirection.Inverse,
					hasConnectionId));
			if (!connections.isEmpty()) {
				CoordinateType nextNodeConnectionPoint = (CoordinateType) client.getAddressSpace()
						.getNode(connections.get(0).getNodeId());
				ConnectedPart connectedPart = new ConnectedPart(getPart(nextNodeConnectionPoint), new Coordinate(connectionPoint));
				connectedPart.relativeCoordinate.substract(nextNodeConnectionPoint);
				nextParts.add(connectedPart);
			}
		}
		return nextParts;
	}
	
	/**
	 * Helper class to store relational location info of connected parts.
	 */
	public class ConnectedPart{
		public DigitalPartType part;
		public Coordinate relativeCoordinate;
		
		public ConnectedPart(DigitalPartType part, Coordinate coordinate) {
			this.part = part;
			this.relativeCoordinate = coordinate;
		}
	}
	
	/**
	 * Helper class to store coordinate info.
	 */
	public class Coordinate{
		public double x;
		public double y;
		public double z;
		
		public Coordinate(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		public Coordinate(CoordinateType coordinate) {
			this.x = coordinate.getX();
			this.y = coordinate.getY();
			this.z = coordinate.getZ();
		}
		public Coordinate(Coordinate coordinate) {
			this.x = coordinate.x;
			this.y = coordinate.y;
			this.z = coordinate.z;
		}
		public void substract(CoordinateType coordinate) {
			this.x -= coordinate.getX();
			this.y -= coordinate.getY();
			this.z -= coordinate.getZ();
		}
		public void add(Coordinate coordinate) {
			this.x += coordinate.x;
			this.y += coordinate.y;
			this.z += coordinate.z;
		}
		public String toString() {
			return "(" + x + ", " + y + ", " + z + ")";
		}
	}
}
