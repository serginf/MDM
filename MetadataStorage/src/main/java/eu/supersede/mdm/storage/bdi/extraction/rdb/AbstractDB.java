package eu.supersede.mdm.storage.bdi.extraction.rdb;

import java.sql.Connection;

public abstract class AbstractDB {
    private static String dbName;
    private String dbType;
    private String dbServer;
    private String dbUserName;
    private String dbPassword;
    private Connection dbConnection;

    public AbstractDB() {
    }

    public AbstractDB(String dbName, String dbType, String dbServer, String dbUserName, String dbPassword) {
        this.dbName = dbName;
        this.dbType = dbType;
        this.dbServer = dbServer;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.dbConnection = dbConnection;
    }


    public static String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbServer() {
        return dbServer;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public void setDbUserName(String dbUserName) {
        this.dbUserName = dbUserName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public abstract void createDbConnection();

    public abstract void extractDbSchema();
}
