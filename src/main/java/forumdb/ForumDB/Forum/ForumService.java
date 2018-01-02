package forumdb.ForumDB.Forum;

import forumdb.ForumDB.Thread.Thread;
import forumdb.ForumDB.Thread.ThreadMapper;
import forumdb.ForumDB.User.User;
import forumdb.ForumDB.User.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ForumService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createForum(Forum forum) {
        String createForumSQL = "insert into forums(slug,title,\"user\") values(?,?,?)";
        jdbcTemplate.update(createForumSQL, new Object[]{
                forum.getSlug(), forum.getTitle(), forum.getUser()
        });
    }

    public Forum getForumBySlug(String slug) {
        String getForumBySlugSQL = "select * from forums f where lower(f.slug) = lower(?)";
        return (Forum) jdbcTemplate.queryForObject(getForumBySlugSQL, new ForumMapper(), new Object[]{slug});
    }

    public void updateForumThreadCount(String slug) {
        String updateFroumThreadCountSQL = "update forums set threads = threads+1 where slug = ?";
        jdbcTemplate.update(updateFroumThreadCountSQL, new Object[]{slug});
    }

    private int getNextId() {
        return (int) jdbcTemplate.queryForObject("select nextval('threads_id_seq')", Integer.class);
    }

    public Thread createNewThread(Forum forum, Thread thread) {
        int id = getNextId();
        String createNewBranchSQL = "insert into threads(author,created,forum,id,message,slug,title) values(?,?,?,?,?,?,?)";
        jdbcTemplate.update(createNewBranchSQL, new Object[]{
                thread.getAuthor(),
                thread.getCreatedTimestamp(),
                forum.getSlug(),
                id,
                thread.getMessage(),
                thread.getSlug(),
                thread.getTitle(),
        });
        thread.setId(id);
        thread.setForum(forum.getSlug());
        updateForumThreadCount(forum.getSlug());
            try {
                String findNewUsersSql = "SELECT exists(SELECT nickname FROM forum_users WHERE forum = '" + forum.getSlug() + "' and nickname = '" + thread.getAuthor() + "')";
                Boolean haveUser = jdbcTemplate.queryForObject(findNewUsersSql, Boolean.class);

                if (!haveUser) {
                    String updateForumUsers = "insert into forum_users(nickname, forum) values(?,?)";
                    jdbcTemplate.update(updateForumUsers, new Object[]{thread.getAuthor(), forum.getSlug()});
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        return thread;
    }

    public ResponseEntity getForumThreads(String slug, int limit, String since, Boolean desc) {
        String getForumBranchesSQL = "select * from threads t " +
                "where t.forum = ?" +
                (since != null ? " and t.created " + (desc ? "<= " : ">= ") + "'" + since + "'" : "") +
                " order by t.created " + (desc ? "desc" : "asc") +
                " limit ?";
        return new ResponseEntity(jdbcTemplate.query(getForumBranchesSQL, new ThreadMapper(), new Object[]{slug, limit}), HttpStatus.OK);
    }

    public List<User> getForumUsers(String slug, Integer limit, String since, Boolean desc) {
        String sort = desc ? "desc" : "asc";
        String sinceInternal = since != null ? " forum_users.nickname " + (desc ? "< " : "> ") + "'" + since + "' and " : "";
        String limitOp = limit != null ? " limit " + limit : "";
        String getForumUsersSql = "select distinct forum_users.nickname, fullname, email, about" +
                " from forum_users " +
                " join users on (forum_users.nickname = users.nickname)" +
                " where " + sinceInternal + " forum = '" + slug + "' order by forum_users.nickname " + sort + limitOp;
//        String getForumUsersSQL =
//                "select u.* from (" +
//                "select author from threads where " + sinceInternal + " forum = '" + slug + "'" +
//                " union " +
//                "select author from posts where " + sinceInternal +  "forum = '" + slug + "'" +
//                " order by author " + sort + " " + limitOp +
//                ") as authors " +
//                        "join users u on(u.nickname = authors.author) " +
//                " order by nickname " + sort +
//                        limitOp;
        return jdbcTemplate.query(getForumUsersSql, new UserMapper());
    }
}
