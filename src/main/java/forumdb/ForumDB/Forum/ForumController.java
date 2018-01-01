package forumdb.ForumDB.Forum;


import forumdb.ForumDB.Error.ErrorMessage;
import forumdb.ForumDB.Thread.Thread;
import forumdb.ForumDB.Thread.ThreadService;
import forumdb.ForumDB.User.User;
import forumdb.ForumDB.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private ForumService forumService;
    private UserService userService;
    private ThreadService threadService;

    @Autowired
    public ForumController(ForumService forumService, UserService userService, ThreadService threadService) {
        this.forumService = forumService;
        this.userService = userService;
        this.threadService = threadService;
    }

    @PostMapping(path = "/create")
    public ResponseEntity createUser(@RequestBody Forum forum) {
        try {
            forum.setUser(userService.getProfile(forum.getUser()).getNickname());
            forumService.createForum(forum);
            return new ResponseEntity(forum, HttpStatus.CREATED);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        } catch (DuplicateKeyException e) {
            try {
                return new ResponseEntity(forumService.getForumBySlug(forum.getSlug()), HttpStatus.CONFLICT);
            } catch (EmptyResultDataAccessException e1) {
                return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
            }

        }
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity getForumDetails(@PathVariable("slug") String slug) {
        try {
            return new ResponseEntity(forumService.getForumBySlug(slug), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/{slug}/create")
    public ResponseEntity createNewThread(@PathVariable("slug") String slug, @RequestBody Thread thread) {
        try {
            User user = userService.getProfile(thread.getAuthor());
            Forum forum = forumService.getForumBySlug(slug);
            thread.setAuthor(user.getNickname());
            thread = forumService.createNewThread(forum, thread);
            return new ResponseEntity(thread, HttpStatus.CREATED);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        } catch (DuplicateKeyException e) {
            try {
                return new ResponseEntity(threadService.getThread(thread.getSlug(), -1), HttpStatus.CONFLICT);
            } catch (EmptyResultDataAccessException e1) {
                return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
            }
        }

    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity getForumThreads(@PathVariable(value = "slug", required = false) String slug,
                                          @RequestParam(value = "limit", required = false, defaultValue = "0") Integer limit,
                                          @RequestParam(value = "since", required = false) String since,
                                          @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {

        try {
            Forum forum = forumService.getForumBySlug(slug);
            return forumService.getForumThreads(forum.getSlug(), limit, since, desc);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(path = "/{slug}/users")
    public ResponseEntity getForumUsers(@PathVariable("slug") String slug,
                                        @RequestParam(value = "limit", required = false) Integer limit,
                                        @RequestParam(value = "since", required = false) String since,
                                        @RequestParam(value = "desc", required = false, defaultValue = "false") boolean desc) {

        try {
            Forum forum = forumService.getForumBySlug(slug);
            return new ResponseEntity(forumService.getForumUsers(slug, limit, since, desc), HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
