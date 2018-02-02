import java.util.Timer;

/**
 * Created by Andrew Schwartz on 2/1/18.
 */

public class Main {
    public static void main(String[] args) {
        DatabaseOld database = new DatabaseOld(new SimpleConfig());
        new Endpoints(database);

        Timer timer = new Timer();
        timer.schedule(database.new Ping(), 0, 5000);
    }
}