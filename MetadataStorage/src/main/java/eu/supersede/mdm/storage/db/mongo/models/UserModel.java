package eu.supersede.mdm.storage.db.mongo.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public class UserModel {

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;
    private String username;
    private String password;
    private Long created_at;
    private Long last_modified;

    public UserModel(){}


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    public Long getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(Long last_modified) {
        this.last_modified = last_modified;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id.toString() +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", created_at=" + created_at +
                ", last_modified=" + last_modified +
                '}';
    }

//    public String toJson(){
//        JSONObject obj = new JSONObject();
//        //Note: mongodb id generates {_id:{"$oid":"58b1bf5bcba40a6a5671620c"}}, we keep just id to make things easier.
//        obj.put("id",id.toString());
//        obj.put("username",username);
//        obj.put("password",password);
//        obj.put("created_at",created_at);
//        obj.put("last_modified",last_modified);
//
//        return obj.toString();
//    }

}

