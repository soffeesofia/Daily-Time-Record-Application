package exception;

public class AlreadyTimedIn extends Exception{
    public AlreadyTimedIn(){
        super("User is already timed in!");
    }
}
