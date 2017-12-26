package forumdb.ForumDB.User;

import forumdb.ForumDB.Error.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private UserService userService;


    @Autowired
    JdbcTemplate jdbcTemplate;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/{nickname}/create")
    public ResponseEntity createUser(@PathVariable("nickname") String nickname, @RequestBody User user) {
        try {
            user.setNickname(nickname);
            return new ResponseEntity(userService.createUser(user), HttpStatus.CREATED);
        } catch (DuplicateKeyException e) {
            String lastUsersSQL = "select * from users where lower(nickname) = lower(?) or lower(email) = lower(?)";
            List<User> users = jdbcTemplate.query(lastUsersSQL, new Object[]{user.getNickname(), user.getEmail()}, new UserMapper());
            return new ResponseEntity(users, HttpStatus.CONFLICT);
        }
    }

    @GetMapping(path = "/{nickname}/profile")
    public ResponseEntity getProfile(@PathVariable("nickname") String nickname) {
        try {
            return new ResponseEntity(userService.getProfile(nickname), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        } catch (EmptyResultDataAccessException e){
            return new ResponseEntity(new ErrorMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path = "/{nickname}/profile")
    public ResponseEntity updateUser(@PathVariable("nickname") String nickname, @RequestBody User user) {
        try {
            user.setNickname(nickname);
            userService.updateUser(user);
            return new ResponseEntity(userService.getProfile(nickname),HttpStatus.OK);
        } catch (DuplicateKeyException e){
            return new ResponseEntity(new ErrorMessage(),HttpStatus.CONFLICT);
        } catch (EmptyResultDataAccessException e){
            return new ResponseEntity(new ErrorMessage(),HttpStatus.NOT_FOUND);
        }
    }

}
