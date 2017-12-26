package forumdb.ForumDB.Thread;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ThreadMapper implements RowMapper {
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Thread thread = new Thread();

        thread.setAuthor(rs.getString("author"));
        thread.setCreated(rs.getTimestamp("created"));
        thread.setForum(rs.getString("forum"));
        thread.setId(rs.getInt("id"));
        thread.setMessage(rs.getString("message"));
        thread.setSlug(rs.getString("slug"));
        thread.setTitle(rs.getString("title"));
        thread.setVotes(rs.getInt("votes"));

        return thread;
    }
}