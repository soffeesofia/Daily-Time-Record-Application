package object;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TimeRecord implements Serializable {

    String date;
    List<TimeDetails> timeDetails;

    public TimeRecord(String date) {
        this.date = date;
        this.timeDetails = new ArrayList<>();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<TimeDetails> getTimeDetails() {
        return timeDetails;
    }

    public void setTimeDetails(List<TimeDetails> timeDetails) {
        this.timeDetails = timeDetails;
    }
}
