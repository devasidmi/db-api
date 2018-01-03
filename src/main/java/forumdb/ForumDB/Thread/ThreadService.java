package forumdb.ForumDB.Thread;

import forumdb.ForumDB.Post.Post;
import forumdb.ForumDB.Post.PostMapper;
import forumdb.ForumDB.Vote.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Thread getThreadBySlugOrId(String slugOrId) {
        try {
            int id = Integer.valueOf(slugOrId);
            return getThreadById(id);
        } catch (NumberFormatException e) {
            return getThreadBySlug(slugOrId);
        }
    }

    public Thread getThreadBySlug(String slug) {
        String getThreadSQL = "select * from threads where lower(slug) = lower(?)";
        return (Thread) jdbcTemplate.queryForObject(getThreadSQL, new Object[]{slug}, new ThreadMapper());
    }

    public Thread getThreadById(int id) {
        String getThreadSQL = "select * from threads where id = ?";
        return (Thread) jdbcTemplate.queryForObject(getThreadSQL, new Object[]{id}, new ThreadMapper());
    }

    public Thread updateThreadInfo(Thread oldThread, Thread thread) {
        if (thread.getMessage() == null && thread.getTitle() == null) {
            return oldThread;
        }
        List<Object> args = new ArrayList<>();
        String updateThreadInfoSql = "update threads set ";
//                (thread.getMessage() != null ? "message = '" + thread.getMessage() + "'," : "") +
//                (thread.getTitle() != null ? "title = '" + thread.getTitle() + "'," : "");

        if (thread.getMessage() != null) {
            args.add(thread.getMessage());
            updateThreadInfoSql += "message = ?,";
        }
        if (thread.getTitle() != null) {
            args.add(thread.getTitle());
            updateThreadInfoSql += "title = ?,";
        }

        if (!updateThreadInfoSql.endsWith(",")) {
            return oldThread;
        }
//        updateThreadInfoSql = updateThreadInfoSql.substring(0, updateThreadInfoSql.length() - 1) + " where id = " + String.valueOf(oldThread.getId());
        updateThreadInfoSql = updateThreadInfoSql.substring(0, updateThreadInfoSql.length() - 1) + " where id = ?";
        args.add(oldThread.getId());
        jdbcTemplate.update(updateThreadInfoSql, args.toArray());
        if (thread.getMessage() != null) oldThread.setMessage(thread.getMessage());
        if (thread.getTitle() != null) oldThread.setTitle(thread.getTitle());
        return oldThread;
    }

    private int getNextId() {
        return (int) jdbcTemplate.queryForObject("select nextval('votes_id_seq')", Integer.class);
    }

    private int checkUserVoted(String nickname, int threadId) {

        String checkUserVotedSQL = "select voice from votes v where v.nickname = ? and v.thread = ?";
        try {
            return jdbcTemplate.queryForObject(checkUserVotedSQL, new Object[]{nickname, threadId}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public Thread vote(Vote vote, Thread thread) {

        int nextId = getNextId();
        String createVoteSQL = "insert into votes(nickname,voice,id,thread) values(?,?,?,?)";
        String updateThreadAfterVoteSQL = "update threads set votes = votes + ? where id = ?";
        String lastUserVoteSQL = "update votes set voice = ? where nickname = ? and thread = ?";

        int voice = checkUserVoted(vote.getNickname(), thread.getId());
        if (voice == vote.getVoice()) {
            return thread;
        } else {
            if (voice == 0) {
                thread.setVotes(thread.getVotes() + vote.getVoice());
                jdbcTemplate.update(createVoteSQL, new Object[]{vote.getNickname(), vote.getVoice(), nextId, thread.getId()});
                jdbcTemplate.update(updateThreadAfterVoteSQL, new Object[]{vote.getVoice(), thread.getId()});
            } else {
                thread.setVotes(thread.getVotes() - voice + vote.getVoice());
                jdbcTemplate.update(lastUserVoteSQL, new Object[]{vote.getVoice(), vote.getNickname(), thread.getId()});
                jdbcTemplate.update(updateThreadAfterVoteSQL, new Object[]{vote.getVoice() - voice, thread.getId()});
            }

        }
        return thread;
    }

    public List<Post> TreeSort(Thread thread, int limit, int since, boolean desc) {
        String op = desc ? "<" : ">";
        String sort = desc ? "desc" : "asc";
        List<Object> args = new ArrayList<>();
        String getPostsTreeSQL = "select author, created, forum, id, message, thread, path, parent from posts p" +
                " where p.thread = ?";
        args.add(thread.getId());
        if (since != 0) {
            args.add(since);
            getPostsTreeSQL += " and p.path " + op + " (select path from posts where id = ?)";
        }
        getPostsTreeSQL += " order by p.path " + sort;
        args.add(limit);
        getPostsTreeSQL += " limit ?";
//        String getPostsTreeSQL = "select author, created, forum, id, message, thread, path, parent from posts p" +
//                " where p.thread = ?" + (since != 0 ? " and p.path " + op + " (select path from posts where id = " + since + ")" : "") +
//                " order by p.path " + sort + "" +
//                " limit ?";

        return jdbcTemplate.query(getPostsTreeSQL, args.toArray(), new PostTreeMapper());
    }

    public List<Post> ParentSort(Thread thread, int limit, int since, boolean desc) {
        String op = desc ? "<" : ">";
        String sort = desc ? "desc" : "asc";
        List<Object> args = new ArrayList<>();
        args.add(thread.getId());
        String subQuery = "select id from posts where thread = ? and parent = 0";
        args.add(thread.getId());
        if (since != 0) {
            args.add(since);
            subQuery += " and path " + op + " (select path from posts where id = ?)";
        }
        subQuery += "order by id " + sort + " limit ?";
        args.add(limit);
//        String subQuery = "select id from posts where thread = ? and parent = 0" + (since != 0 ? " and path " + op + " (select path from posts where id = " + since + ")" : "") + "order by id " + sort + " limit ?";
        String getPostsParentSql = "select author, created, forum, id, message, thread, path, parent from posts p" +
                " where p.thread = ? and path[1] in (" + subQuery + ")" +
                " order by p.path " + sort;

//        return jdbcTemplate.query(getPostsParentSql, new Object[]{thread.getId(), thread.getId(), limit}, new PostTreeMapper());
        return jdbcTemplate.query(getPostsParentSql, args.toArray(), new PostTreeMapper());
    }

    public List<Post> FlatSort(Thread thread, int limit, int since, boolean desc) {

        List<Object> args = new ArrayList<>();

        args.add(thread.getId());
        String getPostsFlatSQL = "select id, parent, thread, author, forum, isEdited, message, created " +
                "from posts p " +
                " where p.thread = ? ";
        if (since != 0) {
            args.add(since);
            getPostsFlatSQL += "and p.id " + (desc ? "<" : ">") + " ? ";
        }
        getPostsFlatSQL += "order by p.created " + (desc ? "desc" : "asc") + ", id " + (desc ? "desc" : "asc") + " " +
                "limit ?";
        args.add(limit);


//        String getPostsFlatSQL = "select id, parent, thread, author, forum, isEdited, message, created " +
//                "from posts p " +
//                " where p.thread = ? " + (since != 0 ? "and p.id " + (desc ? "<" : ">") + " " + since + " " : "") +
//                ("order by p.created " + (desc ? "desc" : "asc") + ", id " + (desc ? "desc" : "asc") + " " +
//                        "limit ?");

        List<Post> list = jdbcTemplate.query(getPostsFlatSQL, args.toArray(), new PostMapper());
//        List<Post> list = jdbcTemplate.query(getPostsFlatSQL, new Object[]{thread.getId(), limit}, new PostMapper());
        return list;
    }


    class PostTreeMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Post post = new Post();

            post.setAuthor(rs.getString("author"));
            post.setCreated(rs.getTimestamp("created"));
            post.setForum(rs.getString("forum"));
            post.setId(rs.getInt("id"));
            post.setMessage(rs.getString("message"));
            post.setParent(rs.getInt("parent"));
            post.setThread(rs.getInt("thread"));

            return post;
        }
    }

}
