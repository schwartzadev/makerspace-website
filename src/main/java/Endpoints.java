import gwhs.generated.Tables;
import gwhs.generated.tables.pojos.Certification;
import gwhs.generated.tables.pojos.User;
import io.javalin.Context;
import io.javalin.Javalin;
import javafx.scene.control.Tab;
import org.apache.commons.lang.ObjectUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


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
        getApp().get("/user/:id", this::userPage);
        getApp().get("/login", this::loginPage);
//        getApp().post("/sign-in", this::loginHandler);
        getApp().get("/register", this::registerPage);
//        getApp().post("/signup", this::registerHandler);
//        getApp().get("/logout", this::logOut);
    }

    private void index(Context ctx) {
        try (DSLContext create = DSL.using(this.db.makeConnection(), SQLDialect.MYSQL)) {
            List<User> users = create.select(Tables.USER.ID, Tables.USER.FIRSTNAME, Tables.USER.LASTNAME, Tables.USER.USERNAME, Tables.USER.EMAIL, Tables.USER.LOGIN_DATE)
                    .from(Tables.USER)
                    .orderBy(Tables.USER.CREATED_DATE.desc())
                    .fetch().into(User.class);
            List<Object> templateList = new ArrayList<>();
            templateList.add(users);
            ctx.html(new Template().list(templateList, "src/main/resources/private/freemarker/index.ftl"));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500);
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
                User user = create.select()
                        .from(Tables.USER)
                        .where(Tables.USER.ID.eq(userId))
                        .limit(1)
                        .fetchOne().into(User.class); // is null if the user id doesn't belong to a user
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
//    private void registerHandler(Context ctx) {
//        String first = ctx.formParam("first");
//        String last = ctx.formParam("last");
//        String pass = ctx.formParam("pwd");
//        String user = ctx.formParam("username");
//        if (pass.length() >= 6 ||
//                pass.length() < 255 ||
//                user.length() < 255 ||
//                user.length() >= 4 ||
//                getDb().getUserByName(ctx.formParam("username")) != null) {
//            getDb().addUser(new UserOld(getDb().getMaxID("users") + 1, first, last, user, pass));
//            ctx.status(200);
//            ctx.redirect("/login");
//        } else {
//            ctx.redirect("/register");
//        }
//    }

    private void rootRedirect(Context ctx) {
        ctx.status(302);
        ctx.redirect("/index.html");
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
