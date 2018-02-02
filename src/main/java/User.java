import org.mindrot.jbcrypt.BCrypt;

/**
 * Created by Andrew Schwartz on 8/14/17.
 */
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private boolean edit;
    private boolean view;
    private boolean create;
    private boolean delete;
    private boolean archived;
    private String password;

    public User(int id, String firstName, String lastName, String username, boolean edit, boolean view, boolean create, boolean delete, boolean archived, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.edit = edit;
        this.view = view;
        this.create = create;
        this.delete = delete;
        this.archived = archived;
        this.password = password;
    }

    public User(int id, String firstName, String lastName, String username, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.edit = false;
        this.view = true;
        this.create = true;
        this.delete = false;
        this.archived = false;
    }

    public User() {} //todo delete this

    public void hashPassword() {
        this.password = BCrypt.hashpw(this.password, BCrypt.gensalt());
    }

    public String hashUsername() {
        return BCrypt.hashpw(username, BCrypt.gensalt());
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
