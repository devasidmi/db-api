package forumdb.ForumDB.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public User createUser(User user) {
        String createUserSQL = "insert into users(about,email,fullname,nickname) values(?,?,?,?)";
        jdbcTemplate.update(createUserSQL, new Object[]{user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname()});
        return user;
    }

    public User getProfile(String nickname) {
        String getProfileSQL = "select * from users u where lower(u.nickname) = lower(?)";
        return (User) jdbcTemplate.queryForObject(getProfileSQL, new UserMapper(), new Object[]{nickname});
    }

    public void updateUser(User user) {
        String updateUserSql = "update users set ";
        List<Object> args = new ArrayList<>();
        if (user.getFullname() != null) {
            updateUserSql += "fullname = ?,";
            args.add(user.getFullname());
        }
        if (user.getEmail() != null) {
            updateUserSql += "email = ?,";
            args.add(user.getEmail());
        }
        if (user.getAbout() != null) {
            updateUserSql += "about = ?,";
            args.add(user.getAbout());
        }
        args.add(user.getNickname().toLowerCase());

        if (!updateUserSql.endsWith(",")) {
            return;
        }
        updateUserSql = updateUserSql.substring(0, updateUserSql.length() - 1) + " where lower(nickname) = ?";
        jdbcTemplate.update(updateUserSql.toString(), args.toArray());
    }
}
