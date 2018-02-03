import gwhs.generated.Tables;
import gwhs.generated.tables.pojos.Certification;
import gwhs.generated.tables.pojos.User;
import gwhs.generated.tables.records.UserRecord;
import io.javalin.Context;
import io.javalin.Javalin;
import javafx.scene.control.Tab;
import org.apache.commons.lang.ObjectUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static gwhs.generated.tables.User.USER;


/**
 * Created by Andrew Schwartz on 8/9/17.
 */
public class Endpoints {
    private Javalin app;
    private Database db;
    private PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.FORMATTING);


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
//        getApp().post("/sign-in", this::loginHandler);
        getApp().get("/register", this::registerPage);
        getApp().post("/signup", this::registerHandler);
//        getApp().get("/logout", this::logOut);
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
        }
        try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
            List<Object> templateList = new ArrayList<>();
            int userId = Integer.parseInt(rawId);
//            SELECT user.firstname, user.lastname, user.email, user.username, user.id, user.created_date, certification.`type`, certification.`level`
//            FROM user
//            INNER JOIN certification ON user.id = certification.user_id; // TODO implement the below in one query, modelled on this line and the two above it
            templateList.add(
                    create.select(Tables.CERTIFICATION.TYPE, Tables.CERTIFICATION.LEVEL)
                            .from(Tables.CERTIFICATION)
                            .where(Tables.CERTIFICATION.USER_ID.eq(userId))
                            .fetch().into(Certification.class)
            );
            try {
                UserNew user = create.select()
                        .from(Tables.USER)
                        .where(Tables.USER.ID.eq(userId))
                        .limit(1)
                        .fetchOne().into(UserNew.class); // is null if the user id doesn't belong to a user
                templateList.add(user);
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

//    private void logOut(Context context) {
//        context.status(202);
//        getDb().deleteLogin(getDb().checkCookie(context.cookie("com.aschwartz.judgeprofiles")));
//        context.removeCookie("com.aschwartz.judgeprofiles");
//        context.redirect("/login");
//    }
//
//    private void loginHandler(Context ctx) {
//        UserOld user = getDb().getUserByName(ctx.formParam("username"));
//        if (BCrypt.checkpw(ctx.formParam("pwd"), user.getPassword())) {
////            correct password
//            ctx.cookie("com.aschwartz.judgeprofiles", getDb().saveLogin(user));
//            ctx.redirect("/index.html");
//            ctx.status(200);
//        }
//        else {
//            ctx.status(401);
//            ctx.redirect("/login");
//        }
//    }
//
    private void registerHandler(Context ctx) {
        UserNew user = new UserNew();

        user.setEmail(ctx.formParam("email")); // TODO ASAP FIX THE EMAIL ISSUES
//        Pattern emailCheck = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
//        if (!emailCheck.matcher(user.getEmail()).find()) { // TODO add client-side js email regex check
//            // bad email
//            ctx.status(400); // TODO update with user feedback that email is invalid
//        }

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
                }

                user.setFirstname(ctx.formParam("first"));
                user.setLastname(ctx.formParam("last"));
                user.hashPassword();
                user.setArchived(new Byte("0"));
                user.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                Record max = create.select(USER.ID.max())
                        .from(USER)
                        .fetchOne();
                user.setId((int)max.get(0)+1);
                System.out.println(user);
//                // insert into DB:
//                UserRecord userRecord = create.newRecord(USER, user);
//                create.executeInsert(userRecord);
                ctx.status(200);
                ctx.redirect("/login");
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
