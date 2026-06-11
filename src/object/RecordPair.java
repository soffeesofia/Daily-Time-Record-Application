package object;

import java.io.Serializable;

public class RecordPair implements Serializable {
    String timeIn;
    String timeOut;

    public RecordPair(String timeIn, String timeOut) {
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public void setTimeIn(String timeIn) {
        this.timeIn = timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }
}
