package eu.supersede.mdm.storage.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import eu.supersede.mdm.storage.db.mongo.models.UserModel;
import eu.supersede.mdm.storage.db.mongo.repositories.UserRepository;
import eu.supersede.mdm.storage.db.mongo.utils.UtilsMongo;

/**
 * @author Sergi Nadal - 17/05/2016
 *
 * Users resource (exposed at "metadataDataLayer/users" path)
 */
@Path("metadataStorage")
public class UserResource {

    @Inject
    UserRepository usersR;

    @GET @Path("users")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_users() {
        System.out.println("[GET /users]");

        List<UserModel> list = usersR.findAll();
        String json = UtilsMongo.ToJsonString(list);
        return Response.ok(json).build();
    }

    @GET @Path("users/exists/{username}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response GET_exists(@PathParam("username") String username) {
        System.out.println("[GET /users/exists] username = "+username);

        return usersR.exist(username) ? Response.ok().build() : Response.status(404).build();
    }

    @GET @Path("users/findOne/{username}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response GET_findOne(@PathParam("username") String username) {
        System.out.println("[GET /users/findOne] username = "+username);

        UserModel user = usersR.findByUsername(username);
        if(user != null )
            return Response.ok(UtilsMongo.ToJsonString(user)).build();
        return Response.status(404).build();
    }

    @POST @Path("users")
    @Consumes("text/plain")
    public Response POST_users(String JSON_user) {
        System.out.println("[POST /users] JSON_user = "+JSON_user);

        usersR.create(JSON_user);
        return Response.ok().build();
    }
}
