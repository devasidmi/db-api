package forumdb.ForumDB.Post;

import forumdb.ForumDB.Error.ErrorMessage;
import forumdb.ForumDB.Thread.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Post getPostById(int id) {
        String getPostByIdSQL = "select * from posts where id = ?";
        return (Post) jdbcTemplate.queryForObject(getPostByIdSQL, new PostMapper(), id);
    }

    public List<Post> createPosts(List<Post> posts, Thread thread) {
        String createSQL = "insert into posts(author, message, parent, thread, forum, created, id, path) values(?,?,?,?,?,?,?," +
                "array_append((select path from posts where id = ?), ?::INT))";

        List<Long> ids = jdbcTemplate.query("select nextval('posts_id_seq') from generate_series(1, ?)", new Object[]{posts.size()}, (rs, rowNum) -> rs.getLong(1));
        Set<String> parents = posts.stream().filter(p -> p.getParent() != 0).map(Post::getParent).map(String::valueOf).collect(Collectors.toSet());
        if (!parents.isEmpty()) {
            String checkParents = "select count(id) from posts where thread = " + thread.getId() + " and id in (" + String.join(", ", parents) + ")";
            int parentsCount = jdbcTemplate.queryForObject(checkParents, Integer.class);
            if (parents.size() != parentsCount) {
                System.out.println();
            }
            Assert.isTrue(parents.size() == parentsCount, new ErrorMessage().getMessage());
        }
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
                ps.setLong(7, ids.get(i));
                ps.setLong(8, post.getParent());
                ps.setLong(9, ids.get(i));

                post.setForum(thread.getForum());
                post.setCreated(created);
                post.setThread(thread.getId());
                post.setId(ids.get(i));
            }

            @Override
            public int getBatchSize() {
                return posts.size();
            }
        });

        List<String> users = new ArrayList<>(posts.stream().map(Post::getAuthor).collect(Collectors.toSet()));

        synchronized (this) {
            try {

                if (!users.isEmpty()) {
                    String findNewUsersSql = "SELECT nickname FROM forum_users WHERE forum = '" + thread.getForum() + "' and nickname in (" +
                            String.join(",", users.stream().map(s -> "'" + s + "'").collect(Collectors.toList())) + ")";
                    HashSet<String> oldUsers = new HashSet<>(jdbcTemplate.queryForList(findNewUsersSql, String.class));
                    List<String> newUsers = users.stream().filter(n -> !oldUsers.contains(n)).collect(Collectors.toList());

                    if (!newUsers.isEmpty()) {
                        String createUserForumsSQL = "insert into forum_users(nickname, forum) values(?,?)";
                        jdbcTemplate.batchUpdate(createUserForumsSQL, new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setString(1, newUsers.get(i));
                                ps.setString(2, thread.getForum());
                            }

                            @Override
                            public int getBatchSize() {
                                return newUsers.size();
                            }
                        });
                    }
                }

            } catch (Exception e) {
                System.out.println(e.toString());
            }
            String sqlUpdate = "update forums set posts = posts + ? where slug = ?";
            jdbcTemplate.update(sqlUpdate, posts.size(), thread.getForum());
        }
        return posts;
    }

    public Post updatePost(int id, Post newPost, Post oldPost) {
        if (newPost.getMessage() != null && !newPost.getMessage().equals(oldPost.getMessage())) {
            String updatePostSQL = "update posts set isEdited = true, message = ? where id = ?";
            jdbcTemplate.update(updatePostSQL, newPost.getMessage(), id);

            oldPost.setMessage(newPost.getMessage());
            oldPost.setEdited(true);
            return oldPost;
        } else {
            return oldPost;
        }
    }

}
