package eu.supersede.mdm.storage.errorhandling.exception;

public class BaseException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private final String type;
  private final int code;
  private final String details;
  private final String location;
  private final String moreInfo;

  /**
   * Constructor of the BaseException class.
   * 
   * @param type - type
   * @param code - code
   * @param details - details
   * @param moreInfo - moreInfo
   */
  public BaseException(String type, int code, String details, String location, String moreInfo) {
    this.type = type;
    this.code = code;
    this.details = details;
    this.location = location;
    this.moreInfo = moreInfo;
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String getType() {
    return type;
  }

  public int getCode() {
    return code;
  }

  public String getDetails() {
    return details;
  }

  public String getLocation() {
    return location;
  }

  public String getMoreInfo() {
    return moreInfo;
  }
}
