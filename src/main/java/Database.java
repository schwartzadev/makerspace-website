import org.jooq.DSLContext;
import org.jooq.Record;

import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;


/**
 * Created by Andrew Schwartz on 2/1/18.
 */

import java.sql.*;
import java.util.TimerTask;

import static gwhs.generated.Tables.USERS;

public class Database {
    private String dbDriverClassName;
    private String dbUrl;
    private String dbPassword;
    private String dbUsername;

    public Database(Config config) {
        this.setDbDriverClassName("com.mysql.jdbc.Driver");
        this.setDbUsername(config.getSqlUsername());
        this.setDbPassword(config.getSqlPassword());
        this.setDbUrl(config.getDbUrl());
    }

    private Connection makeConnection() throws SQLException{
        return DriverManager.getConnection(this.getDbUrl(), this.getDbUsername(), this.getDbPassword());
    }

    class Ping extends TimerTask { // keeps connected to the database, makes the heroku app run faster
        public Ping() {
        }

        public void run() { // runs every n seconds to check a connection to the db
            PreparedStatement sql = null;
            Connection conn = null;
            try {
                conn = makeConnection();
                conn.prepareStatement("select 1;").executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("[DatabaseOld.ping()] failed");
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    } else {
                        System.out.println("[DatabaseOld.ping()] connection is null");
                    }
                } catch (SQLException e) {
                    System.out.println("[DatabaseOld.ping()] failed");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

    }

    void test() {
        // Connection is the only JDBC resource that we need
        // PreparedStatement and ResultSet are handled by jOOQ, internally
        try (DSLContext create = DSL.using(this.makeConnection(), SQLDialect.MYSQL)) {
            Result<Record> result = create.select().from(USERS).fetch();
            System.out.println(result);
            create.close();
        }

        // For the sake of this tutorial, let's keep exception handling simple
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDbDriverClassName() {
        return dbDriverClassName;
    }

    public void setDbDriverClassName(String dbDriverClassName) {
        this.dbDriverClassName = dbDriverClassName;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }
}