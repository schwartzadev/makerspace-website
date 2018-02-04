import gwhs.generated.Tables;
import gwhs.generated.tables.pojos.User;
import gwhs.generated.tables.records.UserRecord;
import io.javalin.Context;
import io.javalin.Javalin;
import org.apache.commons.lang.RandomStringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.mindrot.jbcrypt.BCrypt;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static gwhs.generated.tables.Logins.LOGINS;
import static gwhs.generated.tables.User.USER;


/**
 * Created by Andrew Schwartz on 8/9/17.
 */
public class Endpoints {
    private Javalin app;
    private Database db;
    private PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.FORMATTING);

    private UserNew checkLogin(String cookie) {
        try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
            List<gwhs.generated.tables.pojos.Logins> loginsList = create.select(LOGINS.ID, LOGINS.USER_ID, LOGINS.NAME_HASH, LOGINS.RANDOM)
                    .from(LOGINS)
                    .fetchInto(gwhs.generated.tables.pojos.Logins.class);

            for (gwhs.generated.tables.pojos.Logins l : loginsList) {
                if ((l.getRandom() + l.getNameHash()).equals(cookie)) {
                    UserNew user = create.select()
                            .from(Tables.USER)
                            .where(Tables.USER.ID.eq(l.getUserId()))
                            .limit(1)
                            .fetchOne().into(UserNew.class);
                    Timestamp time = new Timestamp(System.currentTimeMillis());
                    user.setLoginDate(time);
                    UserRecord userRecord = create.newRecord(USER, user);
                    create.executeUpdate(userRecord);
                    return user;
//                    yes -- user is authenticated
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // if user is not authenticated
    }

    public Endpoints(Database db) {
        this.db = db;
        Javalin newApp = io.javalin.Javalin.create()
                .port(getHerokuAssignedPort())
                .enableStaticFiles("/public")
                .start();
        setApp(newApp);
        registerEndpoints();
    }

    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 7777;
    }

    private void registerEndpoints() {
        getApp().get("/status", this::status);
        getApp().get("index.html", this::index);
        getApp().get("/", this::rootRedirect);
        getApp().get("/landing", this::landingPage);
        getApp().get("/user/:id", this::userPage);
        getApp().get("/login", this::loginPage);
        getApp().post("/login", this::loginHandler);
        getApp().get("/register", this::registerPage);
        getApp().post("/register", this::registerHandler);
        getApp().get("/logout", this::logOut);
    }

    private void index(Context ctx) {
        try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
            List<User> users = create.select(Tables.USER.ID, Tables.USER.FIRSTNAME, Tables.USER.LASTNAME, Tables.USER.USERNAME, Tables.USER.EMAIL, Tables.USER.LOGIN_DATE)
                    .from(Tables.USER)
                    .orderBy(Tables.USER.CREATED_DATE.desc())
                    .fetch().into(UserNew.class);
            List<Object> templateList = new ArrayList<>();
            templateList.add(users);
            ctx.html(new Template().list(templateList, "src/main/resources/private/freemarker/index.ftl"));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }
    }

    private void landingPage(Context ctx) {
        try {
            ctx.html(new String(Files.readAllBytes(Paths.get("src/main/resources/public/landing.html"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void status(Context context) {
        context.html("<h1>online!</h1>");
    }

    private void loginPage(Context ctx) {
        ctx.html(new Template().login());
    }

    private void registerPage(Context ctx) {
        ctx.html(new Template().register());
    }

    private void userPage(Context ctx) {
        String rawId;
        if ((rawId = ctx.param("id")) == null) {
            ctx.status(404);
            ctx.redirect("/");
        }
        try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
            List<Object> templateList = new ArrayList<>();
            int userId = Integer.parseInt(rawId);
//            SELECT user.firstname, user.lastname, user.email, user.username, user.id, user.created_date, certification.`type`, certification.`level`
//            FROM user
//            INNER JOIN certification ON user.id = certification.user_id; // TODO implement the below in one query, modelled on this line and the two above it
            templateList.add( // item1
                    create.select(Tables.HOLDERS.TYPE, Tables.HOLDERS.LEVEL)
                            .from(Tables.HOLDERS)
                            .where(Tables.HOLDERS.USER_ID.eq(userId))
                            .fetch().into(gwhs.generated.tables.pojos.Holders.class)
            );
            try {
                UserNew user = create.select()
                        .from(Tables.USER)
                        .where(Tables.USER.ID.eq(userId))
                        .limit(1)
                        .fetchOne().into(UserNew.class); // is null if the user id doesn't belong to a user
                templateList.add(user); // item2
                UserNew loggedIn = checkLogin(ctx.cookie("gwhs.makerspace"));
                templateList.add(loggedIn); // item3 -- will be null if nobody is logged in
                if (loggedIn != null) {
                    templateList.add(loggedIn.getId().equals(userId)); // item4 -- boolean if this is the user's own page
                } else {
                    templateList.add(false); // item4 -- false if nobody is logged in
                }
                ctx.html(new Template().list(templateList, "src/main/resources/private/freemarker/user.ftl"));
            } catch (NullPointerException npe) {
                ctx.status(400); // no such user
                ctx.html("bad request -- no such user");
            }
        } catch (NumberFormatException ne) {
            ctx.status(404); // means the specified id isn't a number
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
        }

    }

    private void logOut(Context context) {
        context.status(202);
        try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
            create.delete(LOGINS)
                    .where(LOGINS.ID.eq(checkLogin(context.cookie("gwhs.makerspace")).getId()))
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        context.removeCookie("gwhs.makerspace");
        context.redirect("/login");
    }

    private void loginHandler(Context ctx) {
        try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
            UserNew attemptedLogin = create.select(USER.USERNAME, USER.PASSWORD, USER.ID)
                    .from(USER)
                    .where(USER.USERNAME.eq(ctx.formParam("username")))
                    .limit(1)
                    .fetchOne().into(UserNew.class);
            if (BCrypt.checkpw(ctx.formParam("pwd"), attemptedLogin.getPassword())) {
//            correct password
                int loginId = (int) create.select(LOGINS.ID.max())
                        .from(LOGINS)
                        .fetchOne().get(0) + 1;
                gwhs.generated.tables.pojos.Logins login = new gwhs.generated.tables.pojos.Logins(
                        loginId,
                        attemptedLogin.getId(),
                        RandomStringUtils.random(50, true, true),
                        BCrypt.hashpw(
                                attemptedLogin.getUsername(), BCrypt.gensalt()
                        ),
                        new Timestamp(System.currentTimeMillis())
                );

                create.executeInsert(
                        create.newRecord(LOGINS, login)
                );

                ctx.cookie("gwhs.makerspace", (login.getRandom()+login.getNameHash()));
                ctx.redirect("/index.html");
                ctx.status(200);
            }
            else {
                ctx.status(401);
                ctx.redirect("/login");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.redirect("/login");
        }
    }

    private void registerHandler(Context ctx) {
        UserNew user = new UserNew();

        user.setEmail(ctx.formParam("email"));
        Pattern emailCheck = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        if (!emailCheck.matcher(user.getEmail()).find()) { // TODO add client-side js email regex check
            // bad email
            ctx.status(400); // TODO update with user feedback that email is invalid
            ctx.redirect("/register");
        }

        user.setPassword(ctx.formParam("pwd"));
        user.setUsername(ctx.formParam("username"));
        if (user.getPassword().length() >= 6 ||
                user.getPassword().length() < 255 ||
                user.getUsername().length() < 255 ||
                user.getUsername().length() >= 4) {
            try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
                List<Integer> s = create.select(Tables.USER.ID)
                        .from(Tables.USER)
                        .where(Tables.USER.USERNAME.eq(user.getUsername()))
                        .fetch(USER.ID);
                if (s.size() != 0) { // if username exists
                    System.out.println("username exists");
                    ctx.status(400); // TODO update with user feedback that name already exists
                    ctx.redirect("/register");
                } else {
                    user.setFirstname(ctx.formParam("first"));
                    user.setLastname(ctx.formParam("last"));
                    user.hashPassword();
                    user.setArchived(new Byte("0"));
                    user.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                    Record max = create.select(USER.ID.max())
                            .from(USER)
                            .fetchOne();
                    user.setIsAdmin(new Byte("0"));
                    user.setId((int) max.get(0) + 1);
                    // insert into DB:
                    UserRecord userRecord = create.newRecord(USER, user);
                    create.executeInsert(userRecord);
                    ctx.status(200);
                    ctx.redirect("/login");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            ctx.redirect("/register");
        }
    }

    private void rootRedirect(Context ctx) {
        ctx.status(302);
        // TODO add cookie check (if logged in, redirect to main page, if not, redirect to landing
        ctx.redirect("/landing");
    }

    public void setApp(Javalin app) {
        this.app = app;
    }

    public Database getDb() {
        return db;
    }

    public Javalin getApp() {
        return app;
    }
}
