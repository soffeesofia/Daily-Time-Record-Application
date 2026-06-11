package object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserLogs implements Serializable {
    private String username;

    private List<TimeRecord> timeRecordList;

    public UserLogs(String username) {
        this.username = username;
        this.timeRecordList = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<TimeRecord> getTimeRecordList() {
        return timeRecordList;
    }

    public void setTimeRecordList(List<TimeRecord> timeRecordList) {
        this.timeRecordList = timeRecordList;
    }
}
