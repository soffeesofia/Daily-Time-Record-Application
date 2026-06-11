package server;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import object.TimeDetails;
import object.TimeRecord;
import object.User;
import object.UserLogs;

public class JSONParser {

    //reads the JSON file of users and returns an Array List of User objects
    public ArrayList<User> read() throws IOException {
        String filename = "res/Users.json";
        ArrayList<User> userList = new ArrayList<>();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();

        JsonElement json = parser.parse(new FileReader(filename));

        for (JsonElement element : json.getAsJsonArray()) {
            User user = gson.fromJson(element, User.class);
            userList.add(user);
        }
        return userList;
    }



    //In order to clear the Users.json file and append the new User properly
    public void clearUsersFile() {
        File file = new File("res/Users.json");
        try {
            file.delete();
            FileWriter writer = new FileWriter(file);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //writes a new Time Record for a user under the UserLog.json file
    public void writeLog(String username, Date date, Boolean isTimeIn) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        boolean foundUser = false;
        boolean foundDate = false;

        String recType = isTimeIn ? "Time In" : "Time Out";
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        TimeDetails newDetails = new TimeDetails(timeFormat.format(date), recType);

        Type userLogsType = new TypeToken<List<UserLogs>>() {}.getType();
        try (Reader reader = new FileReader("res/UserLogs.json")) {
            //gets the array of User Logs from JSON
            List<UserLogs> userLogs = gson.fromJson(reader, userLogsType);
            if (userLogs == null) {
                userLogs = new ArrayList<>();
            }
            for (UserLogs log : userLogs) {
                if (log.getUsername().equals(username)) {
                    List<TimeRecord> recFromJSON = log.getTimeRecordList();
                    if (recFromJSON == null){
                        recFromJSON = new ArrayList<>();
                    }
                    for (TimeRecord r :recFromJSON){
                        if(r.getDate().equals(dateFormat.format(date))){
                            List<TimeDetails> detailsFromJSON = r.getTimeDetails();
                            if(detailsFromJSON == null){
                                detailsFromJSON = new ArrayList<>();
                            }
                            detailsFromJSON.add(newDetails);
                            foundDate = true;
                            break;
                        }
                    }
                    if(!foundDate){
                        TimeRecord toAdd = new TimeRecord(dateFormat.format(date));
                        toAdd.getTimeDetails().add(newDetails);
                        recFromJSON.add(toAdd);
                    }
                    foundUser = true;
                    break;
                }
            }
            //if the given username has no records, a new record is created
            if(!foundUser){
                TimeRecord toAddRec = new TimeRecord(dateFormat.format(date));
                toAddRec.getTimeDetails().add(newDetails);
                UserLogs toAddUser = new UserLogs(username);
                toAddUser.getTimeRecordList().add(toAddRec);
                userLogs.add(toAddUser);
            }

            try (FileWriter writer = new FileWriter("res/UserLogs.json")){
                gson.toJson(userLogs, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
