package exception;

public class NotYetApproved extends Exception{
    public NotYetApproved(){
        super("User has not yet been approved by admin");
    }
}
