package forumdb.ForumDB.Post;

import forumdb.ForumDB.Thread.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Post> createPosts(List<Post> posts, Thread thread) {
        String createSQL = "insert into posts(author, message, parent, thread, forum, created, id,path) values(?,?,?,?,?,?,?,"+
                "array_append((select path from posts where id = ?), currval('posts_id_seq')::INT))";

        final List<Long> parent_id = jdbcTemplate.query("select nextval('posts_id_seq') from generate_series(1, ?)", new Object[]{posts.size()}, (rs, rowNum) -> rs.getLong(1));

        Timestamp created = Timestamp.valueOf(ZonedDateTime.now().toLocalDateTime());
        jdbcTemplate.batchUpdate(createSQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                Post post = posts.get(i);

                ps.setString(1, post.getAuthor());
                ps.setString(2, post.getMessage());
                ps.setLong(3, post.getParent());
                ps.setInt(4, thread.getId());
                ps.setString(5, thread.getForum());
                ps.setTimestamp(6, created);
                ps.setLong(7, parent_id.get(i));
                if (post.getParent() != 0) {
                    ps.setLong(8, post.getParent());
                } else {
                    ps.setLong(8, parent_id.get(i));
                }

                post.setForum(thread.getForum());
                post.setCreated(created);
                post.setThread(thread.getId());
                post.setId(parent_id.get(i));


            }

            @Override
            public int getBatchSize() {
                return posts.size();
            }
        });

        String sqlUpdate = "update forums set posts = posts + ? where slug = ?";
        jdbcTemplate.update(sqlUpdate, posts.size(), thread.getForum());

        return posts;
    }


}
