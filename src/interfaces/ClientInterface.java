package interfaces;

import exception.*;
//import object.TimeRecord;
import object.Report;
import object.TimeRecord;
import object.User;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public interface ClientInterface extends Remote {
    public User login(String username, String password) throws RemoteException, InvalidVerification, AlreadyTimedIn, NotYetApproved;
    public void register(String username, String password, String firstName, String lastName) throws RemoteException, UsernameExistsException;
    public void timeIn(String username) throws RemoteException, AlreadyTimedIn;
    public void timeOut(String username) throws RemoteException, AlreadyTimedOut;
    //Summarize should have a different return value
    public TimeRecord summarize(String username, Date date) throws RemoteException;
    public String getCurrentStatus(String username) throws RemoteException;
    public String getCurrentDateAndTime() throws RemoteException;
    //get rid of admin usability from the interface
    public User retrieveUser(String username) throws IOException;

}