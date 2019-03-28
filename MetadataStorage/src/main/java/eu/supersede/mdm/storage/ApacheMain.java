package eu.supersede.mdm.storage;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Created by snadal on 6/06/17.
 */
public class ApacheMain {

    public static String configPath;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Parameter missing: configuration file path");
        }
        configPath = args[0];

        ResourceConfig config = new ResourceConfig();
        config.packages("eu.supersede.mdm.storage.resources");
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        Server server = new Server(8082);
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
