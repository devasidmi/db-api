package forumdb.ForumDB.Post;

import forumdb.ForumDB.Forum.Forum;
import forumdb.ForumDB.Thread.Thread;
import forumdb.ForumDB.User.User;

/**
 * Created by vasidmi on 01/01/2018.
 */
public class PostFull {

    private final Post post;
    private final User user;
    private final Forum forum;
    private final Thread thread;

    public PostFull(Post post, User user, Forum forum, Thread thread) {
        this.post = post;
        this.user = user;
        this.forum = forum;
        this.thread = thread;
    }

    public Post getPost() {
        return post;
    }

    public User getAuthor() {
        return user;
    }

    public Forum getForum() {
        return forum;
    }

    public Thread getThread() {
        return thread;
    }
}
