package forumdb.ForumDB.Post;

import forumdb.ForumDB.Thread.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createPosts(String slug, int id, List<Post> posts, Thread thread) {
        
    }


}
