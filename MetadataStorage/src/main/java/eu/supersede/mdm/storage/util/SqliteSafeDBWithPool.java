package eu.supersede.mdm.storage.util;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SqliteSafeDBWithPool implements Closeable {

    protected final int SUCESS = 1;

    protected final int FAILD = 0;

    protected static Object syncObj = new Object();

    private boolean startTrans = false;

    private String dbpath=ConfigManager.getProperty("sqlite_db");

/*    public SqliteSafeDBWithPool(String dbpath) throws SQLiteException {
        FileUtils.insureFileDirectory(dbpath);
        this.dbpath = dbpath;
    }*/

    public void beginTransaction() throws SQLiteException {
        if(!startTrans){
            synchronized(syncObj){
                if(startTrans){
                    return;
                }
                SQLiteConnection connection = this.getConnection();
                connection.exec("BEGIN");
                startTrans = true;
            }
        }
    }

    public void commitTransaction() throws SQLiteException {
        if(startTrans){
            synchronized(syncObj){
                if(!startTrans){
                    return;
                }
                SQLiteConnection connection = this.getConnection();
                connection.exec("COMMIT");
                startTrans = false;
            }
        }
    }

    /**
     * a thread pool used to store opened connections
     */
    private static ThreadLocal<SQLiteConnection> pools = new ThreadLocal<SQLiteConnection>();

    /**
     * get a sqlite connection with current thread
     * @return
     */
    public SQLiteConnection getConnection(){
        System.setProperty("sqlite4java.library.path", "lib/sqlite4java-392");
        SQLiteConnection connection = pools.get();
        if(connection==null){
            connection = new SQLiteConnection(new File(this.dbpath));
            try {
                connection.open(true);
                connection.setBusyTimeout(60000);
                pools.set(connection);
            } catch (SQLiteException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public SQLiteStatement query(SQLiteConnection db, String sql, List<Object> parameters) throws SQLiteException {
        SQLiteStatement st = null;
        st = db.prepare(sql);
        bindParameters(st, parameters);
        return st;
    }

    public SQLiteStatement query(SQLiteConnection db, String sql) throws SQLiteException {
        SQLiteStatement st = null;
        st = db.prepare(sql);
        return st;
    }

    public String queryOneString(SQLiteConnection db, String sql, List<Object> parameters){
        SQLiteStatement st = null;
        try {
            st = db.prepare(sql);
            bindParameters(st, parameters);
            if (st.step()) {
                return st.columnString(0);
            }
            return null;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (st != null) {
                st.dispose();
            }
        }
    }

    public boolean exists(SQLiteConnection db, String sql, List<Object> parameters) {
        SQLiteStatement st = null;
        try {
            st = db.prepare(sql);
            bindParameters(st, parameters);
            if (st.step()) {
                return true;
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (st != null) {
                st.dispose();
            }
        }
    }

    public int execute(SQLiteConnection db, String sql, List<Object> parameters) {
        SQLiteStatement st = null;
        try {
            st = db.prepare(sql);
            bindParameters(st, parameters);
            st.stepThrough();
            return SUCESS;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return FAILD;
        } finally {
            if (st != null) {
                st.dispose();
            }
        }
    }

    private void bindParameters(SQLiteStatement st, List<Object> parameters) throws SQLiteException {
        if (parameters != null) {
            int size = parameters.size();
            for (int i = 1; i <= size; i++) {
                int key = i;
                Object val = parameters.get(i);
                Class<? extends Object> valType = val.getClass();
                if (Integer.class.equals(valType)) {
                    st.bind(key, (Integer) val);
                } else if (Double.class.equals(valType)) {
                    st.bind(key, (Double) val);
                } else if (String.class.equals(valType)) {
                    st.bind(key, (String) val);
                } else if (Long.class.equals(valType)) {
                    st.bind(key, (Long) val);
                }
            }
        }
    }

    public void close() throws IOException {
        SQLiteConnection connection = getConnection();
        if(connection!=null){
            connection.dispose();
            pools.remove();
        }
    }
}