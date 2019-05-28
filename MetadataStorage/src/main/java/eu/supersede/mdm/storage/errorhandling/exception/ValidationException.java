package eu.supersede.mdm.storage.errorhandling.exception;

import org.apache.http.HttpStatus;

public class ValidationException extends BaseException {

  /**
   * serialVersion UID.
   */
  private static final long serialVersionUID = 1L;

  public ValidationException(String details, String location, String moreInfo) {
    super("Bad Request", HttpStatus.SC_BAD_REQUEST, details, location, moreInfo);
  }

}
