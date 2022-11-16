package model.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author ShuraBlack
 * @since 03-20-2022
 */
public class MySQLConnectionPool {

    private static final Logger LOGGER = LogManager.getLogger(MySQLConnectionPool.class);

    private final String databaseUrl;
    private final String userName;
    private final String password;
    private final int maxPoolSize;
    private int connNum = 0;

    private static final String SQL_VERIFYCONN = "select 1";

    Stack<Connection> freePool = new Stack<>();
    Set<Connection> occupiedPool = new HashSet<>();

    public MySQLConnectionPool(String databaseUrl, String userName,
                               String password, int maxSize) {
        this.databaseUrl = databaseUrl;
        this.userName = userName;
        this.password = password;
        this.maxPoolSize = maxSize;
        if (maxSize < 1) {
            LOGGER.error("Database Poolsize cant be lower than 1", new IllegalArgumentException());
            System.exit(1);
        }
    }

    public synchronized Connection getConnection() {
        Connection conn;

        if (isFull()) {
            LOGGER.error("The connection pool is full", new SQLException());
        }

        conn = getConnectionFromPool();
        if (conn == null) {
            conn = createNewConnectionForPool();
        }

        conn = makeAvailable(conn);
        return conn;
    }

    public synchronized void returnConnection(Connection conn) {
        if (conn == null) {
            throw new NullPointerException();
        }
        if (!occupiedPool.remove(conn)) {
            LOGGER.error("The connection is returned already or it isn't for this pool", new SQLException());
        }
        freePool.push(conn);
    }

    private synchronized boolean isFull() {
        return ((freePool.size() == 0) && (connNum >= maxPoolSize));
    }

    private Connection createNewConnectionForPool() {
        Connection conn = createNewConnection();
        connNum++;
        occupiedPool.add(conn);
        return conn;
    }

    private Connection createNewConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(databaseUrl, userName, password);
        } catch (SQLException e) {
            LOGGER.error("Couldnt establish connection <" + databaseUrl + ">");
        }
        return conn;
    }

    private Connection getConnectionFromPool() {
        Connection conn = null;
        if (freePool.size() > 0) {
            conn = freePool.pop();
            occupiedPool.add(conn);
        }
        return conn;
    }

    private Connection makeAvailable(Connection conn) {
        if (isConnectionAvailable(conn)) {
            return conn;
        }

        occupiedPool.remove(conn);
        connNum--;
        try {
         conn.close();
        } catch (SQLException e) {
            LOGGER.error("Couldnt close connection <" + databaseUrl + ">");
        }

        conn = createNewConnection();
        occupiedPool.add(conn);
        connNum++;
        return conn;
    }

    private boolean isConnectionAvailable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery(SQL_VERIFYCONN);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

}
