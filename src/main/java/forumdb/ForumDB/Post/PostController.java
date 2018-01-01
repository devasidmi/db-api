package forumdb.ForumDB.Post;

import forumdb.ForumDB.Error.ErrorMessage;
import forumdb.ForumDB.Forum.Forum;
import forumdb.ForumDB.Forum.ForumService;
import forumdb.ForumDB.Thread.Thread;
import forumdb.ForumDB.Thread.ThreadService;
import forumdb.ForumDB.User.User;
import forumdb.ForumDB.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@RestController
@RequestMapping("/api/post")
public class PostController {


    private UserService userService;
    private PostService postService;
    private ForumService forumService;
    private ThreadService threadService;


    @Autowired
    public PostController(UserService userService, PostService postService, ForumService forumService, ThreadService threadService) {
        this.userService = userService;
        this.postService = postService;
        this.forumService = forumService;
        this.threadService = threadService;
    }

    @GetMapping(path = "/{id}/details")
    public ResponseEntity getPostInfo(@PathVariable("id") int id, @RequestParam(value = "related", required = false) String[] related) {
        try {
            HashSet<String> relatedSet = related != null ? new HashSet<>(Arrays.asList(related)) : new HashSet<>();
            Post post = postService.getPostById(id);
            PostFull result = new PostFull(
                    post,
                    relatedSet.contains("user") ? userService.getProfile(post.getAuthor()) : null,
                    relatedSet.contains("forum") ? forumService.getForumBySlug(post.getForum()) : null,
                    relatedSet.contains("thread") ? threadService.getThreadById(post.getThread()) : null
            );
            return new ResponseEntity(result, HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity updatePostInfo(@PathVariable("id") int id, @RequestBody(required = false) Post post) {
        try {
            Post oldPost = postService.getPostById(id);
            Post updatedPost = postService.updatePost(id, post, oldPost);
            return new ResponseEntity(updatedPost, HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }


}
