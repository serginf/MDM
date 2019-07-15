package eu.supersede.mdm.storage.model.omq.wrapper_implementations;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import eu.supersede.mdm.storage.model.omq.relational_operators.Wrapper;
import eu.supersede.mdm.storage.util.SQLiteUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SQL_Wrapper extends Wrapper {

    private String url_jdbc;
    private String query;

    public SQL_Wrapper(String name) {
        super(name);
    }

    public String getURL_JDBC() {
        return url_jdbc;
    }

    public void setURL_JDBC(String url_jdbc) {
        this.url_jdbc = url_jdbc;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public String inferSchema() throws Exception {

        DriverManager.registerDriver(new org.postgresql.Driver());

        List<String> attributes = new ArrayList<>() ;

        try (Connection conn = DriverManager.getConnection(
                this.url_jdbc)) {

            if (conn != null) {
                System.out.println("Connected to the database!");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery( this.query );
                ResultSetMetaData rsmd = rs.getMetaData();

                int columnCount = rsmd.getColumnCount();

                // The column count starts from 1
                for (int i = 1; i <= columnCount; i++ ) {
                    attributes.add(rsmd.getColumnName(i));
                }

                rs.close();
                stmt.close();
                conn.close();

            } else {
                System.out.println("Failed to make connection!");
                //TODO: return exception
            }

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }


        JSONObject res = new JSONObject();
        res.put("schema",new Gson().toJson(attributes.toArray()));

        return res.toJSONString();
    }

    public boolean testConnection(){
        try (Connection conn = DriverManager.getConnection(
                this.url_jdbc)) {

            if (conn != null) {
                conn.close();
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String preview(List<String> attributes) throws Exception {

        DriverManager.registerDriver(new org.postgresql.Driver());

        JSONArray data = new JSONArray();

        try (Connection conn = DriverManager.getConnection(
                this.url_jdbc)) {

            if (conn != null) {
                System.out.println("Connected to the database!");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery( this.query );
                ResultSetMetaData rsmd = rs.getMetaData();

                int i = 0;
                while(rs.next()){

                    if(i < 10){
                        JSONArray arr = new JSONArray();

                        for (int x=1;x<=rs.getMetaData().getColumnCount();x++){
                            JSONObject datum = new JSONObject();
                            datum.put("attribute",rsmd.getColumnName(x));
                            datum.put("value",rs.getString(x));
                            arr.add(datum);
                        }
                        data.add(arr);
                        i++;
                    }
                }

                rs.close();
                stmt.close();
                conn.close();

            } else {
                System.out.println("Failed to make connection!");
                //TODO: return exception
            }

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject res = new JSONObject(); res.put("data",data);
        return res.toJSONString();
    }

    @Override
    public void populate(String table, List<String> attributes) throws Exception {

        DriverManager.registerDriver(new org.postgresql.Driver());
        JSONArray data = new JSONArray();
        try (Connection conn = DriverManager.getConnection(this.url_jdbc)) {

            if (conn != null) {
                System.out.println("Connected to the database!");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery( this.query );
                ResultSetMetaData rsmd = rs.getMetaData();

                while(rs.next()){

                        JSONArray arr = new JSONArray();

                        for (int x=1;x<=rs.getMetaData().getColumnCount();x++){
                            JSONObject datum = new JSONObject();
                            datum.put("attribute",rsmd.getColumnName(x));
                            datum.put("value",rs.getString(x));
                            arr.add(datum);
                        }
                        data.add(arr);

                }
                rs.close();
                stmt.close();
                conn.close();

            } else {
                System.out.println("Failed to make connection!");
                //TODO: return exception
            }

        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SQLiteUtils.insertData(table,data);
    }

}
