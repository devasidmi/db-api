package forumdb.ForumDB.Thread;

import forumdb.ForumDB.Post.Post;
import forumdb.ForumDB.Post.PostMapper;
import forumdb.ForumDB.Vote.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Thread getThread(String slug, int id) {
        String getThreadSQL = "select * from threads where lower(slug) = lower(?) or id = ?";
        return (Thread) jdbcTemplate.queryForObject(getThreadSQL, new Object[]{slug, id}, new ThreadMapper());
    }

    public Thread updateThreadInfo(Thread oldThread, Thread thread) {
        ArrayList sqlparams = new ArrayList();
        if (thread.getMessage() == null && thread.getTitle() == null) {
            return oldThread;
        }
        StringBuilder updateThreadInfoSQL = new StringBuilder("update threads set");
        if (thread.getMessage() != null) {
            sqlparams.add(thread.getMessage());
            updateThreadInfoSQL.append(" message = ?,");
            oldThread.setMessage(thread.getMessage());
        }
        if (thread.getTitle() != null) {
            sqlparams.add(thread.getTitle());
            oldThread.setTitle(thread.getTitle());
            updateThreadInfoSQL.append(" title = ?,");
        }
        updateThreadInfoSQL.deleteCharAt(updateThreadInfoSQL.length() - 1);
        sqlparams.add(oldThread.getSlug());
        sqlparams.add(oldThread.getId());
        updateThreadInfoSQL.append(" where slug = ? or id = ?");
        jdbcTemplate.update(updateThreadInfoSQL.toString(), sqlparams.toArray());
        return oldThread;
    }

    private int getNextId(){
        return (int) jdbcTemplate.queryForObject("select nextval('votes_id_seq')", Integer.class);
    }

    private int checkUserVoted(String nickname, String slug){

        String checkUserVotedSQL = "select voice from votes v where v.nickname = ? and v.thread = ?";
        try{
             return jdbcTemplate.queryForObject(checkUserVotedSQL,new Object[]{nickname,slug},Integer.class);
        }catch (EmptyResultDataAccessException e){
            return 0;
        }
    }

    public Thread vote(Vote vote, Thread thread){

        int nextId = getNextId();
        String createVoteSQL = "insert into votes(nickname,voice,id,thread) values(?,?,?,?)";
        String updateThreadAfterVoteSQL = "update threads t set votes = ? where t.slug = ?";
        String lastUserVoteSQL = "update votes set voice = ? where nickname = ? and thread = ?";

        int voice = checkUserVoted(vote.getNickname(),thread.getSlug());
        if(voice == vote.getVoice()){
            return thread;
        }else {
            if(voice==0){
                thread.setVotes(thread.getVotes()+vote.getVoice());
                jdbcTemplate.update(createVoteSQL,new Object[]{vote.getNickname(),vote.getVoice(),nextId,thread.getSlug()});
                jdbcTemplate.update(updateThreadAfterVoteSQL,new Object[]{thread.getVotes(),thread.getSlug()});
            }else{
                thread.setVotes(thread.getVotes()-voice+vote.getVoice());
                jdbcTemplate.update(updateThreadAfterVoteSQL,new Object[]{thread.getVotes(),thread.getSlug()});
                jdbcTemplate.update(lastUserVoteSQL,new Object[]{vote.getVoice(),vote.getNickname(),thread.getSlug()});

            }

        }
        return thread;
    }

    public List<Post> TreeSort(Thread thread, int limit, int since, boolean desc) {

        String sortFlag = !desc ? "asc" : "desc";
        String includePostFlag = !desc ? ">" : "<";
        String getPostsTreeSQL = "select p.id, p.parent, f.slug, p.thread, p.author, p.isEdited, p.message, p.created " +
                "from posts p " +
                "join forums f on (f.slug = p.forum) " +
                "where p.thread = ? " + (since != 0 ? "and p.path " + includePostFlag + "(select p2.path from posts p2 where p2.id = " + since + ")" : "") +
                "order by p.path " + sortFlag + " limit ?";

        return jdbcTemplate.query(getPostsTreeSQL, new PostMapper(), thread.getId(),
                limit);

    }


}
