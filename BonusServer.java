package aalto.ELECC8204.bonus;


import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.prosysopc.ua.ModelException;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.server.UaServerException;

import aalto.ELECC8204.koodigenerointi.KoodigenerointiServer;

public class BonusServer extends KoodigenerointiServer {
	public static int TCP_PORT = 52523;
	public static int HTTPS_PORT = 52446;
	public static String APP_NAME = "BonusServer";
	private static final Logger logger = LogManager.getLogger(BonusServer.class);
	
	public BonusServer(int tcpPort, int httpsPort, String appName) throws SecureIdentityException, IOException, UaServerException, StatusException,
			ServiceException, SAXException, ModelException, URISyntaxException {
		super(tcpPort, httpsPort, appName);
	}
	
	@Override
	protected void createAddressSpace() {
		/**
		 * TODO
		 * Lisää osoiteavaruuteen 5 CoordinateType oliota.
		 * Muuta niiden x, y tai z koordinaattia
		 * automaattisesti generoidulla set-metodilla
		 */
	}
	
	public static void main(String[] args) throws Exception {
		logger.info("Starting BonusServer");
		BonusServer server = new BonusServer(TCP_PORT, HTTPS_PORT, APP_NAME);
		server.createAddressSpace();
		server.run();
	}

}
