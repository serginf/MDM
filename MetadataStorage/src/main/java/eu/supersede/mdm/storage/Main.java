package eu.supersede.mdm.storage;

//import org.eclipse.jetty.server.Server;
//import org.eclipse.jetty.servlet.ServletContextHandler;
//import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.io.IOException;
import java.net.URI;

/**
 * Created by snadal on 29/04/16.
 */
/*
public class Main extends Application {
    public static void main(String[] args) {
        ResourceConfig config = new ResourceConfig();
        config.packages("eu.supersede.mdm.storage.resources");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));


        Server server = new Server(8081);
        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");


        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
*/

public class Main extends ResourceConfig {

    public Main() {
        System.out.println("MetadataStorage started");
        packages("eu.supersede.mdm.storage.resources");
    }

}

/*
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8081/metadataStorage/";

    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in quarry.coal package
        final ResourceConfig rc = new ResourceConfig().packages("eu.supersede.mdm.storage.resources");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("CoAl started with WADL available at "
                + "%sapplication.wadl\n", BASE_URI));
        try {
            Object lock = new Object();
            synchronized (lock) {
                while (true) {
                    lock.wait();
                }
            }
        } catch (InterruptedException ex) {
        }
    }
}
*/