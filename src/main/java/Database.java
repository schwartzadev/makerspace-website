import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.RandomStringUtils;

import java.sql.*;
import java.util.*;

public class Database {
    private Config config;
    private BasicDataSource ds;

    private Connection makeConnection() throws SQLException{
        return this.ds.getConnection();
    }

    public Database(Config config) {
        this.config = config;
        this.ds = new BasicDataSource();
        this.ds.setDriverClassName("com.mysql.jdbc.Driver");
        this.ds.setUsername(config.getSqlUsername());
        this.ds.setPassword(config.getSqlPassword());
        this.ds.setUrl(config.getDbUrl());
    }

    class Ping extends TimerTask {
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
                System.out.println("[Database.ping()] failed");
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    } else {
                        System.out.println("[Database.ping()] connection is null");
                    }
                } catch (SQLException e) {
                    System.out.println("[Database.ping()] failed");
                    e.printStackTrace();
                }
            }
        }
    }

    public List<User> getUsers(PreparedStatement sql) {
        List<User> users = new ArrayList<User>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            ResultSet rs = sql.executeQuery();
            while(rs.next()) {
                String first = rs.getString("firstname");
                String last = rs.getString("lastname");
                String username = rs.getString("username");
                boolean isArchived = rs.getBoolean("archived");
                String password = rs.getString("password");

                users.add(new User(
                        rs.getInt("id"),
                        first,
                        last,
                        username,
                        rs.getBoolean("edit"),
                        rs.getBoolean("view"),
                        rs.getBoolean("create"),
                        rs.getBoolean("delete"),
                        isArchived,
                        password
                ));
            }
            rs.close();
            return users;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByName(String username) {
        Connection connection = null;
        try {
            connection = makeConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username=? LIMIT 1;");
            statement.setString(1, username);
            return getUsers(statement).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null; // means no such user found
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public User getUserByID(int id) {
        Connection connection = null;
        try {
            connection = makeConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id=? LIMIT 1;");
            statement.setInt(1, id);
            return getUsers(statement).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null; // means no such user found
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void executeQuery(PreparedStatement sql) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            sql.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getMaxID(String dbname) {
        int max = -1;
        Connection conn = null;
        try {
            conn = makeConnection();
            Class.forName("com.mysql.jdbc.Driver");
            PreparedStatement sql = conn.prepareStatement("select max(id) from " + dbname + ";");
            ResultSet rs = sql.executeQuery();
            while (rs.next()) {
                max = rs.getInt("max(id)");
            }
            rs.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return max;
    }

    public void addUser(User u) {
        PreparedStatement sql = null;
        u.hashPassword();
        Connection conn = null;
        try {
            conn = makeConnection();
            sql = conn.prepareStatement("INSERT into users VALUES ( ? , ? , ? , ? , ? , ? , ? , ? , 0 , ?);");
            sql.setInt(1, u.getId());
            sql.setString(2, u.getFirstName());
            sql.setString(3, u.getLastName());
            sql.setString(4, u.getUsername());
            sql.setBoolean(5, u.isEdit());
            sql.setBoolean(6, u.isView());
            sql.setBoolean(7, u.isCreate());
            sql.setBoolean(8, u.isDelete());
            sql.setString(9, u.getPassword());
            Database.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteLogin(int id) {
        deleteDbOject(id, "logins");
    }

    public void deleteLogin(Login l) {
        deleteDbOject(l.getId(), "logins");
    }

    private void deleteDbOject(int id, String tbl) { // should only be used by internal scripts
        // this should not be called with a user-inputted 'tbl' argument because of injection
        PreparedStatement sql = null;
        Connection conn = null;
        try {
            conn = makeConnection();
            sql = conn.prepareStatement("DELETE from " + tbl + " WHERE id = ? ;" ); // not great but this is insulated - users don't provide the table
            sql.setInt(1, id);
            Database.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String saveLogin(User u) {
        PreparedStatement sql = null;
        int id = getMaxID("logins")+1;
        String random = RandomStringUtils.random(20, true, true);
        String hashedusername = u.hashUsername();
        Connection conn = null;
        try {
            conn = makeConnection();
            sql = conn.prepareStatement("INSERT into logins VALUES ( ? , ? , ? , ?, NOW() );" );
            sql.setInt(1, id); // id
            sql.setInt(2, u.getId()); // user_id
            sql.setString(3, random); // random string
            sql.setString(4, hashedusername); // hashed username
            Database.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return random+hashedusername;
    }

    public List<Login> getAllLogins() {
        List<Login> logins = new ArrayList<>();
        Connection conn = null;
        try {
            conn = makeConnection();
            PreparedStatement statement = conn.prepareStatement("select * from logins;");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                logins.add(new Login(rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("random"),
                        rs.getString("name_hash"),
                        rs.getDate("date_created")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return logins;
    }

    public Login checkCookie(String cookie) {
        /**
         * returns the id of the signed in user
         * returns -1 if no user is signed in
         */
        for (Login l : getAllLogins()) {
            if ((l.getRandom() + l.getNameHash()).equals(cookie)) { // TODO add check if cookie is over n days old, return false
                return l;
            }
        }
        return null;
    }
}