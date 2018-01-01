package forumdb.ForumDB.Status;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public StatusJSON getDBStatus() {
        StatusJSON dbstatus = new StatusJSON();

        String forumCountSQL = "select count(*) from forums";
        String postCountSQL = "select count(*) from posts";
        String threadCountSQL = "select count(*) from threads";
        String userCountSQL = "select count(*) from users";

        dbstatus.setForumCount(jdbcTemplate.queryForObject(forumCountSQL, Integer.class));
        dbstatus.setPostCount(jdbcTemplate.queryForObject(postCountSQL, Integer.class));
        dbstatus.setThreadCount(jdbcTemplate.queryForObject(threadCountSQL, Integer.class));
        dbstatus.setUserCount(jdbcTemplate.queryForObject(userCountSQL, Integer.class));


        return dbstatus;
    }

    public void clearDB() {
        jdbcTemplate.execute("truncate table users, forums, threads, votes, posts cascade");
    }

}


class StatusJSON {
    @JsonProperty
    private int forum;
    @JsonProperty
    private int post;
    @JsonProperty
    private int thread;
    @JsonProperty
    private int user;


    public void setForumCount(int forum) {
        this.forum = forum;
    }

    public void setPostCount(int post) {
        this.post = post;
    }

    public void setThreadCount(int thread) {
        this.thread = thread;
    }

    public void setUserCount(int user) {
        this.user = user;
    }
}
