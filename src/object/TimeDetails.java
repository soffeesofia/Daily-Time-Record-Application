package object;

import java.io.Serializable;

public class TimeDetails implements Serializable {
    private String time;
    private String recordType;

    public TimeDetails(String time, String recordType) {
        this.time = time;
        this.recordType = recordType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
}

