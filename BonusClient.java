package aalto.ELECC8204.bonus;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.common.ServiceResultException;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;

import aalto.ELECC8204.koodigenerointi.KoodigenerointiClient;


public class BonusClient extends KoodigenerointiClient {
	public static String APP_NAME = "KoodigenerointiClient";
	public static String SERVER_ENDPOINT = "opc.tcp://localhost:52522/OPCUA/BonusServer";
	private static final Logger logger = LogManager.getLogger(BonusClient.class);

	public BonusClient(String appName, String serverEndpoint) throws UnknownHostException, StatusException, ServiceException, URISyntaxException, SecureIdentityException, IOException, ServiceResultException, AddressSpaceException {
		super(appName, serverEndpoint);
	}
	
	@Override
	public void print() {
		/**
		 * TODO
		 * Etsi osoiteavaruudesta kaikki oliot,
		 * joiden tyyppi on CoordinateType ja tulosta
		 * koordinaatit tyyliin x:0.0, y:0.0, z:0.0
		 */
	}
	
	public static void main(String[] args) throws UnknownHostException, StatusException, ServiceException, URISyntaxException, SecureIdentityException, IOException, ServiceResultException, AddressSpaceException {
		/**
		 * TODO
		 * Tulosta kaikki koordinaatit jos komento on print.
		 */
		BonusClient bonusClient = new BonusClient(APP_NAME, SERVER_ENDPOINT);
		bonusClient.start();
		do {
			System.out.println("Commands: read, write, quit");
			String action;
			try {
				action = BonusClient.readInput();
				if (action.equals("quit")) {
					bonusClient.stop();
					break;
				} else {
					NodeId nodeId = BonusClient.readNodeId();
					if (nodeId == null) {
						try {
							nodeId = bonusClient.findNodeId();
						} catch (Exception e) {
							logger.error("Failed to findNodeId");
							logger.error(e.getMessage());
						}
					}
					if (action.equals("read")) {
						try {
							bonusClient.read(nodeId);
						} catch (Exception e) {
							logger.error("Failed to read node");
							logger.error(e.getMessage());
						}
					} else if (action.equals("write")) {
						System.out.println("Enter new value: ");
						String value = BonusClient.readInput();
						try {
							bonusClient.write(nodeId, value);
						} catch (Exception e) {
							logger.error("Failed to write node");
							logger.error(e.getMessage());
						}
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		} while (true);
	}
}