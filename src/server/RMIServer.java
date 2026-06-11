    package server;

    import exception.*;
    import com.google.gson.*;
    import com.google.gson.reflect.TypeToken;
    import interfaces.ClientInterface;
    import javafx.application.Platform;
    import javafx.scene.control.Alert;
    import object.*;
    import server.gui.RMIServerGUI;

    import java.io.*;
    import java.lang.reflect.Type;
    import java.rmi.Remote;
    import java.rmi.RemoteException;
    import java.rmi.registry.LocateRegistry;
    import java.rmi.registry.Registry;
    import java.rmi.server.UnicastRemoteObject;
    import java.sql.Time;
    import java.text.SimpleDateFormat;
    import java.time.Duration;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;

    public class RMIServer extends UnicastRemoteObject implements ClientInterface, Remote {
        public RMIServer() throws RemoteException {
        }

        /**
         * Verifies the login of a client
         * @param username of client
         * @param password of client
         * @return User object
         * @throws InvalidVerification if the entered credentials are incorrect
         * @throws NotYetApproved if user is not yet approved by the administrator
         */
        @Override
        public User login(String username, String password) throws InvalidVerification, NotYetApproved {
            JSONParser jsonParser = new JSONParser();
            ArrayList <User> userList = null;
            try {
                userList = jsonParser.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (User user : userList) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    if (user.getIsRegistered()){
                        return user;
                    }
                    else {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "User has not yet been approved by an admin!");
                            alert.showAndWait();
                        });
                        throw new NotYetApproved();
                    }
                }
            }
            Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid username or password!");
                            alert.showAndWait();
                        });
            throw new InvalidVerification();
        }

        /**
         * Creates a User object for the client registering and writes it to the Users.json file
         * @param username of client
         * @param password of client
         * @param firstName of client
         * @param lastName of client
         * @throws UsernameExistsException if the entered username already exists in Users.json
         */
        @Override
        public void register(String username, String password, String firstName, String lastName) throws UsernameExistsException {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            File file = new File("res/Users.json");
            User[] users = null;
            try (Reader reader = new FileReader(file)) {
                users = gson.fromJson(reader, User[].class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (users != null) {
                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Username already exists! Please choose a different one");
                            alert.showAndWait();
                        });
                        throw new UsernameExistsException();
                    }
                }
            }
            User newUser = new User(username, password, false, firstName, lastName);
            User[] updatedUsers = users != null ? Arrays.copyOf(users, users.length + 1) : new User[1];
            updatedUsers[updatedUsers.length - 1] = newUser;
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(updatedUsers, writer);
                System.out.println("User registered successfully!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Writes a record of 'time in' to the UserLogs.json file
         * @param username of the currently logged-in user
         * @throws AlreadyTimedIn if the current user's attribute of 'OnDuty' is set to true;
         */
        @Override
        public void timeIn(String username) throws AlreadyTimedIn {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Reader reader = new FileReader("res/Users.json")) {
                JsonElement json = gson.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonArray()) {
                    JsonArray jsonArray = json.getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        JsonObject userObject = element.getAsJsonObject();
                        if (userObject.get("username").getAsString().equals(username)) {
                            boolean onDuty = userObject.get("onDuty").getAsBoolean();
                            if (!onDuty) {
                                userObject.addProperty("onDuty", true);
                                JSONParser jsonParser = new JSONParser();
                                jsonParser.writeLog(username, new Date(), true);
                                retrieveUser(username).setOnDuty(true);
                                try (FileWriter writer = new FileWriter("res/Users.json")) {
                                    gson.toJson(jsonArray, writer);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "User is already timed in!");
                                    alert.showAndWait();
                                });
                                throw new AlreadyTimedIn();
                            }
                            return;
                        }
                    }
                    System.out.println("User not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Writes a record of 'time out' to the UserLogs.json file
         * @param username of the currently logged-in user
         * @throws AlreadyTimedOut if the current user's attribute of 'OnDuty' is set to true;
         */
        @Override
        public void timeOut(String username) throws AlreadyTimedOut{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Reader reader = new FileReader("res/Users.json")) {
                JsonElement json = gson.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonArray()) {
                    JsonArray jsonArray = json.getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        JsonObject userObject = element.getAsJsonObject();
                        if (userObject.get("username").getAsString().equals(username)) {
                            boolean onDuty = userObject.get("onDuty").getAsBoolean();
                            if (onDuty) {
                                userObject.addProperty("onDuty", false);
                                JSONParser jsonParser = new JSONParser();
                                jsonParser.writeLog(username, new Date(), false);
                                retrieveUser(username).setOnDuty(false);
                                try (FileWriter writer = new FileWriter("res/Users.json")) {
                                    gson.toJson(jsonArray, writer);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR, "User is already timed out!");
                                    alert.showAndWait();
                                });
                                throw new AlreadyTimedOut();
                            }
                            return;
                        }
                    }
                    System.out.println("User not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Generates a report of a User's time records for the current date
         * @param username of the currently logged-in user
         * @param dateToday pertaining to the current date
         * @return TimeRecord object, where the current date and list of TimeDetails objects are stored
         */
        @Override
        public TimeRecord summarize(String username, Date dateToday) {
            TimeRecord toSummarize = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            boolean foundUser = false;
            boolean foundDate = false;
            Gson gson = new Gson();
            Type userLogsType = new TypeToken<List<UserLogs>>() {}.getType();
            try (Reader reader = new FileReader("res/UserLogs.json")) {
                List<UserLogs> userLogs = gson.fromJson(reader, userLogsType);
                if (userLogs == null) {
                    userLogs = new ArrayList<>();
                }
                for (UserLogs log : userLogs) {
                    if (log.getUsername().equals(username)) {
                        List<TimeRecord> timeRecs = log.getTimeRecordList();
                        if (timeRecs == null){
                            timeRecs = new ArrayList<>();
                        }
                        for (int i = 0; i < timeRecs.size(); i++){
                            if(timeRecs.get(i).getDate().equals(dateFormat.format(dateToday))){
                                foundDate = true;
                                toSummarize = timeRecs.get(i);
                                break;
                            }
                        }
                        if(!foundDate){
                            System.out.println("This user has no records for " +dateFormat.format(dateToday));
                        }
                        foundUser = true;
                        break;
                    }
                }

                if(!foundUser){
                    System.out.println("This user has no existing records.");
                }

                return toSummarize;
            }catch (IOException e){
                System.out.println("File not Found!");
            }
            return null;
        }


        /**
         * Retrieves the attribute value of the User object's OnDuty variable
         * @param username of the currently logged-in user
         * @return String denoting whether the current user is 'Working' or 'On Break'
         */
        @Override
        public String getCurrentStatus(String username) {
            Gson gson = new Gson();
            try (Reader reader = new FileReader("res/Users.json")) {
                JsonElement json = gson.fromJson(reader, JsonElement.class);
                if (json != null && json.isJsonArray()) {
                    JsonArray userArray = json.getAsJsonArray();
                    for (JsonElement userElement : userArray) {
                        JsonObject userObject = userElement.getAsJsonObject();
                        if (userObject.get("username").getAsString().equals(username)) {
                            boolean onDuty = userObject.get("onDuty").getAsBoolean();
                            if (onDuty) {
                                return "Working";
                            } else {
                                return "On Break";
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "User not found.";
        }

        /**
         * @return String denoting the current date and time
         * @throws RemoteException when an error occurs
         */
        @Override
        public String getCurrentDateAndTime() throws RemoteException {
            return currentDateAndTime();
        }

        /**
         * @return String equivalent of the current date and time
         */
        public String currentDateAndTime(){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            return dtf.format(now);
        }

        /**
         * Retireves a User Object
         * @param username of the user to be retrieved
         * @return User object
         * @throws IOException if JSON file is not found
         */
        public User retrieveUser(String username) throws IOException {
            JSONParser jsonParser = new JSONParser();
            ArrayList<User> arrayList = jsonParser.read();
            for (int i = 0; i < arrayList.size(); i++) {
                User user = arrayList.get(i);
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
            return null;
        }

        /**
         * Genrates a List of Time Log records for a given start or end date
         * @param start denotes the starting date
         * @param end denotes the ending date
         * @return List of Report objects which contains the date, usernames, and total time rendered from the records
         */
        public List<Report> generateReport(String start, String end) {
            List<Report> reports = new ArrayList<>();
            String date = null;
            String username = null;
            String dur= null;
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Gson gson = new Gson();
            Type userLogsType = new TypeToken<List<UserLogs>>() {}.getType();
            try (Reader reader = new FileReader("res/UserLogs.json")) {
                List<UserLogs> userLogs = gson.fromJson(reader, userLogsType);
                for (UserLogs userLog : userLogs) {
                    username = userLog.getUsername();
                    List<TimeRecord> timeRecordList = userLog.getTimeRecordList();
                    for (TimeRecord timeRecord : timeRecordList) {
                        LocalDate dateFromJSON = LocalDate.parse(timeRecord.getDate(), dateFormat);
                        LocalDate startDate = LocalDate.parse(start, dateFormat);
                        LocalDate endDate = LocalDate.parse(end, dateFormat);

                        if (dateFromJSON.isAfter(startDate) && dateFromJSON.isBefore(endDate) || dateFromJSON.isEqual(startDate) || dateFromJSON.isEqual(endDate)) {
                            date = timeRecord.getDate();
                            List<TimeDetails> timeDetailsList = timeRecord.getTimeDetails();
                            LocalTime timeIn = null;
                            LocalTime timeOut = null;
                            for (TimeDetails timeDetails : timeDetailsList) {
                                if (timeDetails.getRecordType().equals("Time In")) {
                                    timeIn = LocalTime.parse(timeDetails.getTime());
                                } else if (timeDetails.getRecordType().equals("Time Out")) {
                                    timeOut = LocalTime.parse(timeDetails.getTime());
                                }
                            }

                            Duration duration = Duration.ZERO;
                            duration = duration.plus(Duration.between(timeIn, timeOut));
                            dur = "" + LocalTime.ofSecondOfDay(duration.getSeconds());

                            Report reportTuple = new Report(date, username, dur);
                            reports.add(reportTuple);
                        }
                    }
                }

                return reports;
            }catch (IOException e){
                System.out.println("File not Found!");
            }
            return null;
        }


        /**
         * Verify administrator's login
         * @param username of admin
         * @param password of admin
         * @return User object of admin
         * @throws InvalidVerification if admin credentials are incorrect
         */
        public User loginAdmin(String username, String password) throws InvalidVerification {
            JSONParser jsonParser = new JSONParser();
            //read contents of the json file
            ArrayList <User> userList = null;
            try {
                userList = jsonParser.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //look up the username is the userList
            for (User user : userList) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    return user;
                }
            }
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Username or Password!");
                alert.showAndWait();
            });
            throw new InvalidVerification();
        }

        public static void main(String[] args) {
            try {
                ClientInterface stub = new RMIServer();
                Registry registry = LocateRegistry.createRegistry(10001);
                registry.rebind("samplermi", stub);
                RMIServerGUI gui = new RMIServerGUI();
                gui.run(args);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
