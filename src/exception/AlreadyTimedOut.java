package exception;


public class AlreadyTimedOut extends Exception{
    public AlreadyTimedOut(){
        super("User is already timed out!");
    }
}
