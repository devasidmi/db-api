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

import javax.swing.text.DateFormatter;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
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
            String findNewUsersSql = "SELECT exists(SELECT nickname FROM forum_users WHERE forum = ? and nickname = ?)";
            Boolean haveUser = jdbcTemplate.queryForObject(findNewUsersSql, Boolean.class, forum.getSlug(), thread.getAuthor());

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
        List<Object> args = new ArrayList<>();

        args.add(slug);
        String getForumBranchesSQL = "select * from threads t " +
                "where t.forum = ?";
        if (since != null) {
            args.add(since);
            getForumBranchesSQL += " and t.created " + (desc ? "<= " : ">= ") + "?::timestamptz";
        }
        getForumBranchesSQL += " order by t.created " + (desc ? "desc" : "asc") + " limit ?";
        args.add(limit);

        return new ResponseEntity(jdbcTemplate.query(getForumBranchesSQL, new ThreadMapper(), args.toArray()), HttpStatus.OK);
    }

    public List<User> getForumUsers(String slug, Integer limit, String since, Boolean desc) {
        String sort = desc ? "desc" : "asc";

        String getForumUsersSql = "select distinct forum_users.nickname, fullname, email, about" +
                " from forum_users " +
                " join users on (forum_users.nickname = users.nickname) where ";
        List<Object> args = new ArrayList<>();

        if (since != null) {
            getForumUsersSql += " forum_users.nickname " + (desc ? "< " : "> ") + "?::CITEXT and ";
            args.add(since);
        }
        getForumUsersSql += " forum = ?::CITEXT order by forum_users.nickname " + sort;
        args.add(slug);
        if (limit != null) {
            getForumUsersSql += " limit ?::INT";
            args.add(limit);
        }
        return jdbcTemplate.query(getForumUsersSql, args.toArray(), new UserMapper());
    }
}
