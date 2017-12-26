package forumdb.ForumDB.Error;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ErrorMessage {
    private String message;

    @JsonCreator
    public ErrorMessage() {
        this.message = "error";
    }

    public String getMessage() {
        return message;
    }
}
