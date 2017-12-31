package forumdb.ForumDB.Status;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
public class StatusController {

    private StatusService statusService;

    @Autowired
    public StatusController(StatusService statusService){
        this.statusService = statusService;
    }

    @GetMapping(path = "/status")
    public ResponseEntity dbstatus(){
        return new ResponseEntity(statusService.getDBStatus(), HttpStatus.OK);
    }

    @PostMapping(path = "/clear")
    public ResponseEntity dbclear(){
        statusService.clearDB();
        return new ResponseEntity(HttpStatus.OK);
    }

}
