import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andrew Schwartz on 2/2/18.
 */
public class UserNew extends gwhs.generated.tables.pojos.User {

    public void hashPassword() {
        System.out.println(this.getPassword());
        this.setPassword(BCrypt.hashpw(this.getPassword(), BCrypt.gensalt()));
    }

    public String getShortCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        return sdf.format(this.getCreatedDate());
    }
}
