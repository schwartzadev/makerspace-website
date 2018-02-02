/**
 * Created by Andrew Schwartz on 8/7/17.
 */
public class Judge {
    private int id;
    private String firstname;
    private String lastname;
    private String affiliation;
    private int speed;
    private int flowing;
    private String paradigmLink;
    private String paradigm;
    private String votesOn;
    private java.sql.Date lastUpdated; //TODO should this be date or..?
    private int updatedBy; // userid of person who last updated this profile
    private boolean archived;

    public Judge(int id, String firstname, String lastname, String affiliation, int speed, int flowing, String paradigmLink, String paradigm, String votesOn, int updatedBy) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.affiliation = affiliation;
        this.speed = speed;
        this.flowing = flowing;
        this.paradigmLink = paradigmLink;
        this.paradigm = paradigm;
        this.votesOn = votesOn;
        this.updatedBy = updatedBy;
    }

    public Judge(int id, String firstname, String lastname, // full constructor
                 String affiliation, int speed, int flowing,
                 String paradigmLink, String paradigm, String votesOn,
                 java.sql.Date lastUpdated, int updatedBy, boolean archived) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.affiliation = affiliation;
        this.speed = speed;
        this.flowing = flowing;
        this.paradigmLink = paradigmLink;
        this.paradigm = paradigm;
        this.votesOn = votesOn;
        this.lastUpdated = lastUpdated;
        this.updatedBy = updatedBy;
        this.archived = archived;
    }

    public Judge(int id, String firstname, String lastname, String affiliation, int speed, int flowing) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.affiliation = affiliation;
        this.speed = speed;
        this.flowing = flowing;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getFlowing() {
        return flowing;
    }

    public void setFlowing(int flowing) {
        this.flowing = flowing;
    }

    public String getParadigmLink() {
        return paradigmLink;
    }

    public void setParadigmLink(String paradigmLink) {
        this.paradigmLink = paradigmLink;
    }

    public String getParadigm() {
        return paradigm;
    }

    public void setParadigm(String paradigm) {
        this.paradigm = paradigm;
    }

    public String getVotesOn() {
        return votesOn;
    }

    public void setVotesOn(String votesOn) {
        this.votesOn = votesOn;
    }

    public java.sql.Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(java.sql.Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

}
