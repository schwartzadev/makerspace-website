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

    public List<Judge> getAllSimpleJudges() {
        return getSimpleJudges("SELECT id, firstname, lastname, affiliation, speed, flowing FROM judges WHERE archived=0 ORDER BY lastname;");
    }

    public List<Judge> getAllJudges() {
        return getJudges("SELECT * FROM judges;");
    }

    public List<Judge> getSimpleJudges(String query) { // get Judge objects with only the name, speed, flow, and affiliation values
        List<Judge> judges = new ArrayList<Judge>();
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = makeConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                String first = rs.getString("firstname");
                String last = rs.getString("lastname");
                String affiliation = rs.getString("affiliation");

                judges.add(new Judge(
                        rs.getInt("id"),
                        first,
                        last,
                        affiliation,
                        rs.getInt("speed"),
                        rs.getInt("flowing")
                ));
            }
            rs.close();
            return judges;
        } catch (ClassNotFoundException | SQLException e) {
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

    public List<Judge> getJudges(String query) {
        List<Judge> judges = new ArrayList<Judge>();
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = makeConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                String first = rs.getString("firstname");
                String last = rs.getString("lastname");
                String affiliation = rs.getString("affiliation");
                String paradigmLink = rs.getString("paradigmlink");
                String paradigm = rs.getString("paradigm");
                String votesOn = rs.getString("voteson");
                boolean isArchived = rs.getBoolean("archived");
                java.sql.Date lastUpdated = rs.getDate("lastupdated");

                judges.add(new Judge(
                        rs.getInt("id"),
                        first,
                        last,
                        affiliation,
                        rs.getInt("speed"),
                        rs.getInt("flowing"),
                        paradigmLink,
                        paradigm,
                        votesOn,
                        lastUpdated,
                        rs.getInt("updatedby"),
                        isArchived
                ));
            }
            rs.close();
            return judges;
        } catch (ClassNotFoundException | SQLException e) {
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

    public List<Judge> getJudges(PreparedStatement sql) {
        List<Judge> judges = new ArrayList<Judge>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            ResultSet rs = sql.executeQuery();
            while(rs.next()){
                String first = rs.getString("firstname");
                String last = rs.getString("lastname");
                String affiliation = rs.getString("affiliation");
                String paradigmLink = rs.getString("paradigmlink");
                String paradigm = rs.getString("paradigm");
                String votesOn = rs.getString("voteson");
                boolean isArchived = rs.getBoolean("archived");
                java.sql.Date lastUpdated = rs.getDate("lastupdated");

                judges.add(new Judge(
                        rs.getInt("id"),
                        first,
                        last,
                        affiliation,
                        rs.getInt("speed"),
                        rs.getInt("flowing"),
                        paradigmLink,
                        paradigm,
                        votesOn,
                        lastUpdated,
                        rs.getInt("updatedby"),
                        isArchived
                ));
            }
            rs.close();
            return judges;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateJudge(Judge judge) {
        PreparedStatement sql = null;
        Connection conn = null;
        try {
            conn = makeConnection();
            sql = conn.prepareStatement(
                    "UPDATE judges " +
                    "SET firstname=?, lastname=?, affiliation=?, speed=?, flowing=?, paradigmlink=?, paradigm=?, voteson=?, archived=?, lastupdated=NOW(), updatedby=? " +
                    "WHERE id=?;");
            sql.setString(1, judge.getFirstname());
            sql.setString(2, judge.getLastname());
            sql.setString(3, judge.getAffiliation());
            sql.setInt(4, judge.getSpeed());
            sql.setInt(5, judge.getFlowing());
            sql.setString(6, judge.getParadigmLink());
            sql.setString(7, judge.getParadigm());
            sql.setString(8, judge.getVotesOn());
            sql.setBoolean(9, judge.isArchived());
            sql.setInt(10, judge.getUpdatedBy());
            sql.setInt(11, judge.getId());
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

    public void addJudge(Judge judge) {
        PreparedStatement sql = null;
        Connection conn = null;
        try {
            conn = makeConnection();
            sql = conn.prepareStatement("INSERT into judges VALUES ( ? , ? , ? , ? , ? , ? , ? , ?, ?, NOW(), ?, ? );");
            sql.setInt(1, judge.getId());
            sql.setString(2, judge.getFirstname());
            sql.setString(3, judge.getLastname());
            sql.setString(4, judge.getAffiliation());
            sql.setInt(5, judge.getSpeed());
            sql.setInt(6, judge.getFlowing());
            sql.setString(7, judge.getParadigmLink());
            sql.setString(8, judge.getParadigm());
            sql.setString(9, judge.getVotesOn());
            sql.setBoolean(10, judge.isArchived());
            sql.setInt(11, judge.getUpdatedBy());
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

    public void unarchiveJudge(int id) {
        changeJudgeArchivedStatus(id, false);
    }

    public void archiveJudge(int id) {
        changeJudgeArchivedStatus(id, true);
    }

    public void changeJudgeArchivedStatus(int id, boolean archive) {
        PreparedStatement sql = null;
        Connection conn = null;
        try {
            conn = makeConnection();
            sql = conn.prepareStatement("update judges set archived = ? where id = ? ;" );
            sql.setBoolean(1, archive);
            sql.setInt(2, id);
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

    public Judge getJudgeByID(int id) {
        Connection connection = null;
        try {
            connection = makeConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM judges WHERE id=? LIMIT 1;");
            statement.setInt(1,id);
            return getJudges(statement).get(0);
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

    private void deleteDbOject(int id, String tbl) {
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

//    public User lookupUserByUsername(String username) {
//        PreparedStatement sql = null;
//        Connection conn = null;
//        try {
//            conn = makeConnection();
//            sql = conn.prepareStatement("select * from users where username = ?;");
//            sql.setString(1, username);
//            ResultSet rs = sql.executeQuery();
//            while (rs.next()) {
//                return new User(rs.getInt("id"), rs.getBoolean("isactive"), rs.getString("username"), rs.getString("password"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                conn.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
//    }

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