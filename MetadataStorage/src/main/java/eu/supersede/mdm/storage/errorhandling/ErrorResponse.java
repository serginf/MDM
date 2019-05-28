package eu.supersede.mdm.storage.errorhandling;

public class ErrorResponse {

    private String type;
    private Integer code;
    private String details;
    private String location;
    private String moreInfo;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public String toString() {

        StringBuilder errorResponsesb = new StringBuilder();
        errorResponsesb.append("ErrorResponse [ ");

        if (type != null) {
            errorResponsesb.append("type=").append(type).append(" ");
        }

        if (code != null) {
            errorResponsesb.append("code=").append(code).append(" ");
        }

        if (details != null) {
            errorResponsesb.append("details=").append(details).append(" ");
        }

        if (location != null) {
            errorResponsesb.append("location=").append(location).append(" ");
        }

        if (moreInfo != null) {
            errorResponsesb.append("moreInfo=").append(moreInfo);
        }

        errorResponsesb.append(" ]");

        return errorResponsesb.toString();
    }
}
