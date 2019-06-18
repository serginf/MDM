package eu.supersede.mdm.storage.bdi.extraction.rdb;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Kashif-Rabbani in June 2019
 */
public class MySqlDB extends AbstractDB {
    private String mySqlDriver = "com.mysql.jdbc.Driver";
    private DatabaseMetaData databaseMetaData = null;
    private List<String> tables = new ArrayList<>();
    private RelationalToRDFS relationalToRDFS = null;

    public MySqlDB(String dbUserName, String dbPassword, String dbName, String dbServer, String dbType) {
        super(dbName, dbType, dbServer, dbUserName, dbPassword);
        createDbConnection();
    }

    public String getRelationalOutputFilePath(){
        return relationalToRDFS.getOutputFilePath();
    }

    public String getRelationalIRI(){
        return relationalToRDFS.getIRI();
    }

    @Override
    public void createDbConnection() {
        try {
            Class.forName(mySqlDriver);
            setDbConnection(DriverManager.getConnection(getDbType() + "://" + getDbServer() + "/" + getDbName() +
                    "?" + "user=" + getDbUserName() + "&password=" + getDbPassword())); //"jdbc:mysql://localhost/dbName?user=root&password="
            extractDbSchema();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extractDbSchema() {
        try {
            databaseMetaData = getDbConnection().getMetaData();
            relationalToRDFS = new RelationalToRDFS();
            extractTables();
            for (String table : tables) {
                System.out.println("-------------------------------------");
                System.out.println("Table Name: " + table);
                System.out.println("-------------------------------------");
                relationalToRDFS.tablesToRDFS(table);
                extractColumns(table);
                extractPrimaryKey(table);
                extractForeignKey(table);
            }
            relationalToRDFS.schemaModelToFile();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void extractTables() {
        try {
            ResultSet res = databaseMetaData.getTables(null, null, null, null);
            System.out.println("List of Tables: ");
            while (res.next()) {
                System.out.println(
                        "  " + res.getString("TABLE_CAT")
                                + ", " + res.getString("TABLE_NAME")
                                + ", " + res.getString("TABLE_TYPE")
                                + ", " + res.getString("REMARKS")
                );
                if (res.getString("TABLE_TYPE").equals("TABLE"))
                    tables.add(res.getString("TABLE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void extractColumns(String table) {
        try {
            ResultSet res = databaseMetaData.getColumns(null, null, table, null);
            System.out.println("List of columns for " + table);
            while (res.next()) {
                System.out.println(
                        "  " + res.getString("TABLE_CAT")
                                + ", " + res.getString("TABLE_NAME")
                                + ", " + res.getString("COLUMN_NAME")
                                + ", " + res.getString("TYPE_NAME")
                                + ", " + res.getInt("COLUMN_SIZE")
                                + ", " + res.getString("SQL_DATA_TYPE")
                                + ", " + res.getInt("NULLABLE"));
                relationalToRDFS.columnsToRDFS(
                        res.getString("TABLE_NAME"),
                        res.getString("COLUMN_NAME"),
                        res.getString("TYPE_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void extractPrimaryKey(String table) {
        try {
            ResultSet res = databaseMetaData.getPrimaryKeys(null, null, table);
            System.out.println("List of columns PK: " + table);
            while (res.next()) {
                System.out.println(
                        "  " + res.getString("TABLE_CAT")
                                + ", " + res.getString("TABLE_NAME")
                                + ", " + res.getString("COLUMN_NAME")
                                + ", " + res.getShort("KEY_SEQ")
                                + ", " + res.getString("PK_NAME"));
                //relationalToRDFS.primaryKeysToRDFS(res.getString("TABLE_NAME"), res.getString("COLUMN_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void extractForeignKey(String table) {
        try {
            ResultSet res = databaseMetaData.getExportedKeys(null, null, table);
            System.out.println("List of columns FK: " + table);
            while (res.next()) {
                System.out.println(
                        //"  " + res.getString("PKTABLE_CAT") + ", " +
                        res.getString("PKTABLE_NAME")
                                + ", " + res.getString("PKCOLUMN_NAME")
                                //+ ", " + res.getString("FKTABLE_CAT")
                                + ", " + res.getString("FKTABLE_NAME")
                                + ", " + res.getString("FKCOLUMN_NAME")
                );
                relationalToRDFS.foreignKeysToRDFS(
                        res.getString("PKTABLE_NAME"),
                        res.getString("PKCOLUMN_NAME"),
                        res.getString("FKTABLE_NAME"),
                        res.getString("FKCOLUMN_NAME"));
            }
            res.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
