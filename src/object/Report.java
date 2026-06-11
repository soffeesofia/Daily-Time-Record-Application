package object;

public class Report {
    String date;
    String username;
    String timeRendered;

    public Report(String date, String username, String timeRendered) {
        this.date = date;
        this.username = username;
        this.timeRendered = timeRendered;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimeRendered() {
        return timeRendered;
    }

    public void setTimeRendered(String timeRendered) {
        this.timeRendered = timeRendered;
    }
}
