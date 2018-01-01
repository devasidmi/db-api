package forumdb.ForumDB.Thread;

import forumdb.ForumDB.Error.ErrorMessage;
import forumdb.ForumDB.Post.Post;
import forumdb.ForumDB.Post.PostService;
import forumdb.ForumDB.User.UserService;
import forumdb.ForumDB.Vote.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thread")

public class ThreadController {

    private ThreadService threadService;
    private UserService userService;
    private PostService postService;


    @Autowired
    public ThreadController(ThreadService threadService, UserService userService, PostService postService) {

        this.threadService = threadService;
        this.userService = userService;
        this.postService = postService;

    }


    @GetMapping(path = "/{slug_or_id}/details")
    public ResponseEntity getThreadInfo(@PathVariable("slug_or_id") String slug_or_id) {
        try {
            int id = -1;
            try {
                id = Integer.valueOf(slug_or_id);
            } catch (NumberFormatException e) {

            }
            return new ResponseEntity(threadService.getThread(slug_or_id, id), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/{slug_or_id}/details")
    public ResponseEntity updateThreadInfo(@PathVariable("slug_or_id") String slug_or_id, @RequestBody Thread thread) {
        try {
            int id = -1;
            try {
                id = Integer.valueOf(slug_or_id);
            } catch (NumberFormatException e) {

            }
            Thread oldThread = threadService.getThread(slug_or_id, id);

            return new ResponseEntity(threadService.updateThreadInfo(oldThread, thread), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/{slug_or_id}/vote")
    public ResponseEntity voteThread(@PathVariable("slug_or_id") String slug_or_id, @RequestBody Vote vote) {
        try {
            int id = -1;
            try {
                id = Integer.valueOf(slug_or_id);
            } catch (NumberFormatException e) {

            }
            Thread thread = threadService.getThread(slug_or_id, id);
            userService.getProfile(vote.getNickname());
            return new ResponseEntity(threadService.vote(vote, thread), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/{slug_or_id}/create")
    public ResponseEntity createPost(@PathVariable("slug_or_id") String slug_or_id, @RequestBody List<Post> posts) {

        int id = -1;
        try {
            id = Integer.valueOf(slug_or_id);
        } catch (NumberFormatException e) {

        }
        try {
            Thread thread = threadService.getThread(slug_or_id, id);
            return new ResponseEntity(postService.createPosts(posts, thread), HttpStatus.CREATED);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.CONFLICT);
        }
    }


    @GetMapping(path = "/{slug_or_id}/posts")
    public ResponseEntity getPosts(@PathVariable(value = "slug_or_id", required = false) String slug_or_id,
                                   @RequestParam(value = "limit", required = false, defaultValue = "0") Integer limit,
                                   @RequestParam(value = "since", required = false, defaultValue = "0") Integer since,
                                   @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
                                   @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {

        int id = -1;
        try {
            id = Integer.valueOf(slug_or_id);
        } catch (NumberFormatException e) {

        }
        try {

            Thread thread = threadService.getThread(slug_or_id, id);
            switch (sort) {
                case "flat":
                    return new ResponseEntity(threadService.FlatSort(thread, limit, since, desc), HttpStatus.OK);
                case "tree":
                    return new ResponseEntity(threadService.TreeSort(thread, limit, since, desc), HttpStatus.OK);
                case "parent_tree":
                    return new ResponseEntity(threadService.ParentSort(thread, limit, since, desc), HttpStatus.OK);
            }

        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
    }

}
