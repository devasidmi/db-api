package forumdb.ForumDB.Thread;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class Thread {

    private String author;
    private Timestamp created;
    private String forum;
    private int id;
    private String message;
    private String slug;
    private String title;
    private int votes;


    @JsonCreator
    public Thread(@JsonProperty("slug") String slug, @JsonProperty("author") String author,
                  @JsonProperty("message") String message, @JsonProperty("title") String title,
                  @JsonProperty("created") Timestamp created) {

        this.author = author;
        this.message = message;
        this.slug = slug;
        this.title = title;

        if (created == null) {
            this.created = Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime());
        } else {
            this.created = created;
        }

    }

    public Thread() {

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created.toInstant().toString();
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    @JsonIgnore
    public Timestamp getCreatedTimestamp() {
        return created;
    }

}
