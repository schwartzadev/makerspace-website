import java.util.Timer;

/**
 * Created by Andrew Schwartz on 7/30/17.
 */


// IN ORDER TO RUN:
// START MYSQL

public class Main {
    public static void main(String[] args) {
        Database database = new Database(new SimpleConfig());
        new Endpoints(database);

        Timer timer = new Timer();
        timer.schedule(database.new Ping(), 0, 5000);
    }
}