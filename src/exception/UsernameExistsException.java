package exception;

public class UsernameExistsException extends Exception {
    public UsernameExistsException(){
        super("Username already exists!");
    }
}
