package eu.supersede.mdm.storage.util;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class BdiSQLiteUtils {

    public static void createTable(String table, List<String> attributes) {
        SqliteSafeDBWithPool sqliteSafeDBWithPool = new SqliteSafeDBWithPool();

        SQLiteConnection conn = sqliteSafeDBWithPool.getConnection();

        String droptable = "DROP TABLE IF EXISTS " + table + ";";
        try {
            SQLiteStatement stmt = sqliteSafeDBWithPool.query(conn,droptable);
            stmt.step();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        StringBuilder SQL = new StringBuilder("CREATE TABLE " + table + " (");
        attributes.forEach(a -> SQL.append(a + " s,"));

        String createStatement = SQL.toString().substring(0, SQL.toString().length() - 1) + ");";

        try {
            SQLiteStatement stmt = sqliteSafeDBWithPool.query(conn,createStatement);
            stmt.step();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }


     /*   try {
            sqliteSafeDBWithPool.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static void insertData(String table, JSONArray data) {
        SqliteSafeDBWithPool sqliteSafeDBWithPool = new SqliteSafeDBWithPool();
        SQLiteConnection conn = sqliteSafeDBWithPool.getConnection();

        data.forEach(tuple -> {
            String SQL = "INSERT INTO " + table + " ";
            StringBuilder schema = new StringBuilder("(");
            StringBuilder values = new StringBuilder("(");
            ((JSONArray) tuple).forEach(datum -> {
                schema.append(((JSONObject) datum).getAsString("attribute") + ",");
                values.append("'" + ((JSONObject) datum).getAsString("value").replace("'", "") + "',");
            });
            SQL += schema.substring(0, schema.length() - 1) + ") VALUES " + values.substring(0, values.length() - 1) + ");";

            try {
                sqliteSafeDBWithPool.beginTransaction();
                SQLiteStatement stmt = sqliteSafeDBWithPool.query(conn,SQL);
                stmt.step();
                sqliteSafeDBWithPool.commitTransaction();
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        });
        try {
            sqliteSafeDBWithPool.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void executeQuery(String SQL) {
        SqliteSafeDBWithPool sqliteSafeDBWithPool = new SqliteSafeDBWithPool();
        SQLiteConnection conn = sqliteSafeDBWithPool.getConnection();
        try {
            sqliteSafeDBWithPool.beginTransaction();
            SQLiteStatement stmt = sqliteSafeDBWithPool.query(conn,SQL);
            stmt.step();
            sqliteSafeDBWithPool.commitTransaction();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        try {
            sqliteSafeDBWithPool.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static JSONArray executeSelect(String sql, List<String> features) {
        SqliteSafeDBWithPool sqliteSafeDBWithPool = new SqliteSafeDBWithPool();
        SQLiteConnection conn = sqliteSafeDBWithPool.getConnection();
        JSONArray data = new JSONArray();
        try {
            SQLiteStatement stmt = sqliteSafeDBWithPool.query(conn, sql);
            while (stmt.step()) {
                JSONArray arr = new JSONArray();
                for (int i = 0; i < features.size(); ++i) {
                    JSONObject datum = new JSONObject();
                    datum.put("feature", features.get(i));
                    datum.put("value", stmt.columnString(i));
                    arr.add(datum);
                }
                data.add(arr);
            }
        } catch (SQLiteException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            sqliteSafeDBWithPool.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }


}
