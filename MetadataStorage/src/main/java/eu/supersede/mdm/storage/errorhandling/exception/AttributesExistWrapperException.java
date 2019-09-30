package eu.supersede.mdm.storage.errorhandling.exception;

import org.apache.http.HttpStatus;

public class AttributesExistWrapperException extends BaseException{
    /**
     * serialVersion UID.
     */
    private static final long serialVersionUID = 913289723009874562L;

    public AttributesExistWrapperException(String details, String location, String moreInfo) {
        super("CONFLICT", HttpStatus.SC_CONFLICT, details, location, moreInfo);
    }
}
