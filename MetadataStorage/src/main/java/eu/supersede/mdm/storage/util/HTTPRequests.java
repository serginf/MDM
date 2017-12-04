package eu.supersede.mdm.storage.util;

import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * Created by snadal on 7/06/16.
 */
public class HTTPRequests {

    /**
     * Used to simplify requests to external resources
     */
    public static WebTarget request(String URI) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(URI);
        return target;

    }

}

