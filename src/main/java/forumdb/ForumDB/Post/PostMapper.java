package forumdb.ForumDB.Post;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostMapper implements RowMapper {
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        Post post = new Post();

        post.setAuthor(rs.getString("author"));
        post.setCreated(rs.getTimestamp("created"));
        post.setForum(rs.getString("forum"));
        post.setId(rs.getInt("id"));
        post.setEdited(rs.getBoolean("isEdited"));
        post.setMessage(rs.getString("message"));
        post.setParent(rs.getInt("parent"));
        post.setThread(rs.getInt("thread"));

        return post;
    }
}
