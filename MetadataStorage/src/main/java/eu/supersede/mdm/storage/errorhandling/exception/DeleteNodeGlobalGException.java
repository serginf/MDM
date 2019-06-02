package eu.supersede.mdm.storage.errorhandling.exception;

import org.apache.http.HttpStatus;

public class DeleteNodeGlobalGException extends BaseException {

    /**
     * serialVersion UID.
     */
    private static final long serialVersionUID = 9132893880052177472L;

    public DeleteNodeGlobalGException(String details, String location, String moreInfo) {
        super("CONFLICT", HttpStatus.SC_CONFLICT, details, location, moreInfo);
    }
}
