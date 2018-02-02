import io.javalin.Context;
import io.javalin.Javalin;
import org.mindrot.jbcrypt.BCrypt;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;


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
//        getApp().get("/delete/:id", this::deleteNote);

        getApp().get("index.html", this::index);
        getApp().get("/", this::rootRedirect);
        getApp().get("/login", this::loginPage);
        getApp().post("/sign-in", this::loginHandler);
        getApp().get("/register", this::registerPage);
        getApp().post("/signup", this::registerHandler);
        getApp().get("/logout", this::logOut);
        getApp().get("/status", this::status);
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

    private void logOut(Context context) {
        context.status(202);
        getDb().deleteLogin(getDb().checkCookie(context.cookie("com.aschwartz.judgeprofiles")));
        context.removeCookie("com.aschwartz.judgeprofiles");
        context.redirect("/login");
    }

    private void loginHandler(Context ctx) {
        User user = getDb().getUserByName(ctx.formParam("username"));
        if (BCrypt.checkpw(ctx.formParam("pwd"), user.getPassword())) {
//            correct password
            ctx.cookie("com.aschwartz.judgeprofiles", getDb().saveLogin(user));
            ctx.redirect("/index.html");
            ctx.status(200);
        }
        else {
            ctx.status(401);
            ctx.redirect("/login");
        }
    }

    private void registerHandler(Context ctx) {
        String first = ctx.formParam("first");
        String last = ctx.formParam("last");
        String pass = ctx.formParam("pwd");
        String user = ctx.formParam("username");
        if (pass.length() >= 6 ||
                pass.length() < 255 ||
                user.length() < 255 ||
                user.length() >= 4 ||
                getDb().getUserByName(ctx.formParam("username")) != null) {
            getDb().addUser(new User(getDb().getMaxID("users") + 1, first, last, user, pass));
            ctx.status(200);
            ctx.redirect("/login");
        } else {
            ctx.redirect("/register");
        }
    }

    private void rootRedirect(Context ctx) {
        ctx.status(200);
        ctx.redirect("/index.html");
    }

    private void index(Context ctx) {
        // check for login cookie
        String cookie = ctx.cookie("com.aschwartz.judgeprofiles");
        try {
            int loggedInUser = getDb().checkCookie(cookie).getUserId();  // checkCookie returns a null value if the user is not logged in
            boolean canview = true;
            if (cookie != null && (loggedInUser) != -1 && (canview = getDb().getUserByID(loggedInUser).isView())) {
                System.out.println("[" + ctx.ip() + "] " + loggedInUser + " is logged in");
                ctx.html("we're live");
                ctx.status(200);
            } else if (!canview) {
                ctx.status(403);
            } else {
                ctx.status(403);
                ctx.redirect("/login");
            }
        } catch (NullPointerException npe) {
            ctx.redirect("/login");
//            npe.printStackTrace();
        }
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
