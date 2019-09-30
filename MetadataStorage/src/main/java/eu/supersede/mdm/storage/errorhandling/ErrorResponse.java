package eu.supersede.mdm.storage.errorhandling;

import org.bson.Document;

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

        Document response = new Document();

//        StringBuilder errorResponsesb = new StringBuilder();
//        errorResponsesb.append("{\"ErrorResponse\" : { ");

        if (type != null) {
            response.put("type",type);
//            errorResponsesb.append("\"type\":").append("\""+type+"\",");
        }

        if (code != null) {
            response.put("code",code);
//            errorResponsesb.append("\"code\":").append("\""+code+"\",");
        }

        if (details != null) {
            response.put("details",details);
//            errorResponsesb.append("\"details\":").append("\""+details+"\",");
        }

        if (location != null) {
            response.put("location",location);
//            errorResponsesb.append("\"location\":").append("\""+location+"\",");
        }

        if (moreInfo != null) {
            response.put("moreInfo",moreInfo);
//            errorResponsesb.append("\"moreInfo\":").append("\""+moreInfo+"\"");
        }

//        errorResponsesb.append("} }");

        System.out.println(response.toJson());
        return response.toJson();
    }
}
