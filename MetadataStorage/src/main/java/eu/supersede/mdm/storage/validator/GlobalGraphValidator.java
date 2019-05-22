package eu.supersede.mdm.storage.validator;

import eu.supersede.mdm.storage.errorhandling.exception.ValidationException;
import org.apache.commons.lang.StringUtils;

public class GlobalGraphValidator {

    public void validateGeneralBody(String body,String location){
        if (StringUtils.isEmpty(body))
            throw new ValidationException("Invalid or missing body",location,"body is missing");
    }

    public void validateGraphicalGraphBody(String body, String location){
        if (StringUtils.isEmpty(body))
            throw new ValidationException("Invalid or missing body",location,"graph cannot be empty");
    }

    public void validateBodyTriples(String body, String location){
        if (StringUtils.isEmpty(body))
            throw new ValidationException("Invalid or missing body",location,"triples are missing");
    }

}
