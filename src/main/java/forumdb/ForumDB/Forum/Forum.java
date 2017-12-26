package forumdb.ForumDB.Forum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {

    private long posts;
    private String slug;
    private int threads;
    private String title;
    private String user;

    @JsonCreator
    public Forum(@JsonProperty("slug") String slug, @JsonProperty("title") String title,
                 @JsonProperty("user") String user) {

        this.slug = slug;
        this.title = title;
        this.user = user;

    }

    public Forum() {

    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}

