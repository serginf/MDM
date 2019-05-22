package eu.supersede.mdm.storage.errorhandling.exceptionMappers;

import eu.supersede.mdm.storage.errorhandling.ErrorResponse;
import eu.supersede.mdm.storage.errorhandling.exception.ValidationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.text.MessageFormat;
import java.util.logging.Logger;


@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOGGER = Logger.getLogger(ValidationExceptionMapper.class.getName());
    private static final String LOG_MSG = "HttpResponse: Exception {0} thrown with message: [ {1} ]"
            + ". Returning with ErrorResponse: [ {2} ].";


    public Response toResponse(ValidationException ex) {

        ErrorResponse errorResponse = new ErrorResponse();

        errorResponse.setType(ex.getType());
        errorResponse.setCode(ex.getCode());
        errorResponse.setDetails(ex.getMoreInfo());
        errorResponse.setLocation(ex.getLocation());
//        errorResponse.setMoreInfo(ex.getRequestURI()) ;

        LOGGER.info(MessageFormat.format(LOG_MSG, ex.getClass().getName(), ex.getDetails(), errorResponse.toString()));

        return Response.status(ex.getCode())
                .entity(errorResponse.toString())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
