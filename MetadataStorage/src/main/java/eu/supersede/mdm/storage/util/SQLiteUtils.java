package eu.supersede.mdm.storage.util;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.List;

public class SQLiteUtils {

    public static void createTable(String table, List<String> attributes) {
        SQLiteConnection conn = Utils.getSQLiteConnection();

        String droptable = "DROP TABLE IF EXISTS "+table+";";
        try {
            SQLiteStatement stmt = conn.prepare(droptable);
            stmt.step();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            conn.dispose();
        }

        conn = Utils.getSQLiteConnection();
        StringBuilder SQL = new StringBuilder("CREATE TABLE "+table+" (");
        attributes.forEach(a -> SQL.append(a+" text,"));

        String createStatement = SQL.toString().substring(0,SQL.toString().length()-1) + ");";

        try {
            SQLiteStatement stmt = conn.prepare(createStatement);
            stmt.step();
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            conn.dispose();
        }

    }

    public static void insertData(String table, JSONArray data) {
        data.forEach(tuple -> {
            String SQL = "INSERT INTO "+table+" ";
            StringBuilder schema = new StringBuilder("(");
            StringBuilder values = new StringBuilder("(");
            ((JSONArray)tuple).forEach(datum -> {
                schema.append(((JSONObject)datum).getAsString("attribute")+",");
                values.append("'"+((JSONObject)datum).getAsString("value")+"',");
            });
            SQL += schema.substring(0,schema.length()-1)+") VALUES "+values.substring(0,values.length()-1)+");";
            SQLiteConnection conn = Utils.getSQLiteConnection();
            try {
                SQLiteStatement stmt = conn.prepare(SQL);
                stmt.step();
            } catch (SQLiteException e) {
                e.printStackTrace();
            } finally {
                conn.dispose();
            }
        });
    }

    public static JSONArray executeSelect(String sql,List<String> features) {
        SQLiteConnection conn = Utils.getSQLiteConnection();
        JSONArray data = new JSONArray();
        try {
            SQLiteStatement stmt = conn.prepare(sql);
            while (stmt.step()) {
                JSONArray arr = new JSONArray();
                for (int i = 0; i < features.size(); ++i) {
                    JSONObject datum = new JSONObject();
                    datum.put("feature",features.get(i));
                    datum.put("value",stmt.columnString(i));
                    arr.add(datum);
                }
                data.add(arr);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            conn.dispose();
        }
        return data;
    }


}
