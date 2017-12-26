package forumdb.ForumDB.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    private String about;
    private String email;
    private String fullname;
    private String nickname;

    @JsonCreator
    public User(@JsonProperty("email") String email, @JsonProperty("about") String about,
                @JsonProperty("fullname") String fullname) {

        this.about = about;
        this.email = email;
        this.fullname = fullname;

    }

    public User() {

    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
