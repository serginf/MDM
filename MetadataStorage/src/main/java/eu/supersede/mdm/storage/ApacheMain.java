package eu.supersede.mdm.storage;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
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

        setLoggerConfig();

        ResourceConfig config = new ResourceConfig();
        config.packages("eu.supersede.mdm.storage.resources");
        config.packages("eu.supersede.mdm.storage.errorhandling");
        config.register(ApiListingResource.class);
        config.register(SwaggerSerializers.class);

        configSwagger();

        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        Server server = new Server(8082);

        ServletContextHandler context = new ServletContextHandler(server, "/*");
        context.addServlet(servlet, "/*");

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addHandler(buildSwaggerUI());
        contexts.addHandler(context);

        server.setHandler(contexts);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // http://localhost:8082/swagger.json
    public static void  configSwagger() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1");
        beanConfig.setTitle("MetadataStorage");
        beanConfig.setDescription("Description");
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setHost("localhost:8082");
        beanConfig.setBasePath("/");
        beanConfig.setResourcePackage("eu.supersede.mdm.storage.resources");
        beanConfig.setPrettyPrint(true);
        beanConfig.setScan(true);
    }


    // This starts the Swagger UI at http://localhost:8082/docs/
    private static ContextHandler buildSwaggerUI() throws Exception {
        ResourceHandler rh = new ResourceHandler();
        //  .getResource("META-INF/resources/webjars/swagger-ui/3.20.9")
        rh.setResourceBase(ApacheMain.class.getClassLoader()
                .getResource("swagger-ui")
                .toURI().toString());

        ContextHandler context = new ContextHandler();
        context.setContextPath("/docs/");
        context.setWelcomeFiles(new String[] { "index.html" });
        context.setHandler(rh);
        return context;
    }

    private static void setLoggerConfig(){
        InputStream stream = ApacheMain.class.getClassLoader().
                getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
