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
        final StringBuilder updateUserSQL = new StringBuilder("update users set");
        final List<Object> updateFields = new ArrayList<>();

        if (user.getFullname() != null && !user.getFullname().isEmpty()) {
            updateUserSQL.append(" fullname = ?,");
            updateFields.add(user.getFullname());
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            updateUserSQL.append(" email = ?,");
            updateFields.add(user.getEmail());
        }

        if (user.getAbout() != null && !user.getAbout().isEmpty()) {
            updateUserSQL.append(" about = ?,");
            updateFields.add(user.getAbout());
        }

        if (updateFields.isEmpty()) {
            return;
        }

        updateUserSQL.deleteCharAt(updateUserSQL.length() - 1);
        updateUserSQL.append(" where lower(nickname) = ?");
        updateFields.add(user.getNickname().toLowerCase());
        jdbcTemplate.update(updateUserSQL.toString(), updateFields.toArray());
    }
}
