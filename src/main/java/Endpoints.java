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
        getApp().get("/profile/:id", this::detailView);
        getApp().get("/add", this::addView);
        getApp().post("/make-profile", this::createProfile);
//        getApp().get("/remove-profile", this::deleteProfile);
        getApp().get("/edit/:id", this::editView);
        getApp().post("/update-profile", this::updateProfile);
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

    private void editView(Context ctx) {
        String cookie = ctx.cookie("com.aschwartz.judgeprofiles");
        int loggedInUser = -1;
        try {
            if (cookie != null && (loggedInUser = getDb().checkCookie(cookie).getUserId()) != -1 && getDb().getUserByID(loggedInUser).isEdit()) {
                int judgeId = Integer.parseInt(ctx.param("id"));
                ctx.html(new Template().editView(getDb().getJudgeByID(judgeId)));
                System.out.println("[" + ctx.ip() + "] " + loggedInUser + " editing " + judgeId);
                ctx.status(200);
            } else if (!getDb().getUserByID(loggedInUser).isEdit()) {
                ctx.status(403);
            } else {
                ctx.status(403);
                ctx.redirect("/login");
            }
        } catch (NullPointerException npe) {
            ctx.redirect("/login");
            npe.printStackTrace();
        }
    }

    private void detailView(Context ctx) {
        String cookie = ctx.cookie("com.aschwartz.judgeprofiles");
        int loggedInUser = -1;
        try {
            if (cookie != null && (loggedInUser = getDb().checkCookie(cookie).getUserId()) != -1 && getDb().getUserByID(loggedInUser).isView()) {
                int judgeId = Integer.parseInt(ctx.param("id"));
                ctx.html(new Template().detailView(getDb().getJudgeByID(judgeId)));
                System.out.println("[" + ctx.ip() + "] " + loggedInUser + " viewing " + judgeId);
                ctx.status(200);
            } else {
                ctx.status(403);
                ctx.redirect("/login");
            }
        } catch (NullPointerException npe) {
            ctx.redirect("/login");
            npe.printStackTrace();
        }
    }

    private void loginPage(Context ctx) {
        ctx.html(new Template().login());
    }

    private void registerPage(Context ctx) {
        ctx.html(new Template().register());
    }

    private void addView(Context context) {
        context.html(new Template().addView());
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

//    private void deleteNote(Context ctx) {
//        System.out.println("[" + ctx.ip() + "] deleting " + ctx.param("id") + "...");
//        if (checkAuth(ctx)) {
//            try {
//                getDb().archiveNote(Integer.parseInt(ctx.param("id"))); // can throw nfe
//                ctx.status(201);
//            } catch (NumberFormatException nfe) {
//                ctx.status(500);
//                ctx.html("invalid request. Specify a note id to delete.<br><a href=\"/index.html\">return to home</a>");
//            } catch (Exception e) {
//                ctx.status(500);
//                e.printStackTrace();
//            }
//        } else {
//            ctx.status(403);
//        }
//    }

    private void index(Context ctx) {
        // check for login cookie
        String cookie = ctx.cookie("com.aschwartz.judgeprofiles");
        try {
            int loggedInUser = getDb().checkCookie(cookie).getUserId();  // checkCookie returns a null value if the user is not logged in
            boolean canview = true;
            if (cookie != null && (loggedInUser) != -1 && (canview = getDb().getUserByID(loggedInUser).isView())) {
                System.out.println("[" + ctx.ip() + "] " + loggedInUser + " is logged in");
                ctx.html(new Template().judgeList(getDb().getAllSimpleJudges()));
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

    private void updateProfile(Context ctx) {
        Judge judge = makeProfile(ctx);
        if (judge != null) {
            int loggedInUser = getDb().checkCookie(ctx.cookie("com.aschwartz.judgeprofiles")).getUserId();
            User current = getDb().getUserByID(loggedInUser);
            try {
                if (current.isEdit()) {
                    int judgeId = Integer.parseInt(policy.sanitize(ctx.formParam("id")));
                    judge.setId(judgeId);
                    getDb().updateJudge(judge);
                    System.out.println("[" + ctx.ip() + "] updated " + judgeId);
                } else {
                    ctx.status(403);
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                ctx.redirect("index.html");
            }
        } else {
            ctx.redirect("index.html");
        }
        ctx.redirect("/profile/" + judge.getId());
        ctx.status(200);
    }

    private void createProfile(Context ctx) {
        int loggedInUser = getDb().checkCookie(ctx.cookie("com.aschwartz.judgeprofiles")).getUserId();
        User current = getDb().getUserByID(loggedInUser);
        if (current.isCreate()) {
            Judge judge = makeProfile(ctx);
            if (judge != null) {
                getDb().addJudge(judge);
                ctx.redirect("/profile/" + judge.getId());
                ctx.status(200);
            } else {
                ctx.status(400);
            }
        } else {
            ctx.status(403);
        }
    }

    private Judge makeProfile(Context ctx) { // interim method to create the judge object from context
        String cookie = ctx.cookie("com.aschwartz.judgeprofiles");
        int loggedInUser = getDb().checkCookie(cookie).getUserId();
        User current = getDb().getUserByID(loggedInUser);

        try {
            int cleanSpeed = Integer.parseInt(policy.sanitize(ctx.formParam("speed")));
            int cleanFlowing = Integer.parseInt(policy.sanitize(ctx.formParam("flow")));

            if (cookie != null && (loggedInUser) != -1 && current.isCreate()) { // checks that user exists and has login privlidges
                System.out.println("[" + ctx.ip() + "] " + loggedInUser + " adding profile");
                String cleanFirst = policy.sanitize(ctx.formParam("first"));
                String cleanLast = policy.sanitize(ctx.formParam("last"));
                String cleanAffiliation = policy.sanitize(ctx.formParam("affiliation"));
                String cleanParadigmLink = policy.sanitize(ctx.formParam("paradigm-link"));
                String cleanParadigm  = policy.sanitize(ctx.formParam("paradigm"));
                String cleanVotesOn  = policy.sanitize(ctx.formParam("voteson"));

                Judge j = new Judge(
                        (getDb().getMaxID("judges") + 1),
                        cleanFirst,
                        cleanLast,
                        cleanAffiliation,
                        cleanSpeed,
                        cleanFlowing,
                        cleanParadigmLink,
                        cleanParadigm,
                        cleanVotesOn,
                        loggedInUser
                );


                if (j.getAffiliation().equals("")) {
                    j.setAffiliation(null);
                }

                if (j.getParadigmLink().equals("")) {
                    j.setParadigmLink(null);
                }

                return j;
            } else if (!current.isCreate()) {
                ctx.status(403);
            } else {
                ctx.status(403);
                ctx.redirect("/login");
            }
        } catch (NumberFormatException nfe) {
            ctx.status(400);
            ctx.html("<pre>HTTP 400 - Bad request." +
                    "<br>The request could not be understood by the server due to malformed syntax." +
                    "<br>The client should not repeat the request without modifications.");
            System.out.println("[" + ctx.ip() + "] on create: inputted number values aren't numbers (NUMBER FORMAT EXCEPTION)</pre>");
            return null;
        }
        return null;
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
