package server;

import com.google.gson.*;
import interfaces.ClientInterface;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import object.RecordPair;
import object.Report;
import object.User;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class adminController {

    //TODO: LOGIN
    @FXML
    public Button loginButton;
    public Button exitButton;
    @FXML
    public Button backButton;
    @FXML
    public TableView <User> employeeStatus;
    @FXML
    public TableView <User> registrationTable;
    @FXML
    public DatePicker startDate;
    public DatePicker endDate;
    public Button genReportButton;
    @FXML
    private PasswordField passField;
    @FXML
    private TextField userField;
    public String start;
    public String end;


    @FXML
    void loginPressed(ActionEvent event) {
        String username = userField.getText();
        String password = passField.getText();
        try {
            RMIServer rmiServer = new RMIServer();
            User user = rmiServer.retrieveUser(username);
            if (rmiServer.loginAdmin(username, password) != null && user.isAdmin()) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/gui/AdminHome.fxml"));
                    Parent root = loader.load();
                    Stage stage = new Stage();
                    stage.setTitle("Admin");
                    stage.setScene(new Scene(root));
                    stage.show();

                    // Close the current window
                    ((Node) (event.getSource())).getScene().getWindow().hide();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void exitClicked(ActionEvent event) {
        System.exit(0);
    }
    @FXML
    public void logOutPressed(ActionEvent actionEvent) throws IOException {
        ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/gui/AdminLoginGUI.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Login");
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    public void regReq(ActionEvent actionEvent) throws IOException {
        ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/gui/Registrationreq.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Registration Requests");
        stage.setScene(new Scene(root));
        stage.show();
        TableView<User> registrationTable = (TableView<User>) root.lookup("#registrationTable");
        TableColumn<User, String> firstNameColumn = new TableColumn<>("First Name");
        TableColumn<User, String> lastNameColumn = new TableColumn<>("Last Name");
        TableColumn<User, String> userNameColumn = new TableColumn<>("Username");
        TableColumn<User, String> statusColumn = new TableColumn<>("Status");
        TableColumn<User, User> actionColumn = new TableColumn<>("Action");

        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isOnDuty() ? "On Duty" : "Off Duty"));

        registrationTable.getColumns().setAll(firstNameColumn, lastNameColumn, userNameColumn, statusColumn, actionColumn);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new FileReader("res/Users.json")) {
            User[] users = gson.fromJson(reader, User[].class);
            ObservableList<User> unregisteredUsers = FXCollections.observableArrayList(
                    Arrays.stream(users)
                            .filter(user -> !user.isRegistered())
                            .collect(Collectors.toList()));
            registrationTable.setItems(unregisteredUsers);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        actionColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button approveButton = new Button("Approve");
            private final Button rejectButton = new Button("Reject");

            {
                approveButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    user.setRegistered(true);
                    try (Reader reader = new FileReader("res/Users.json")) {
                        User[] users = gson.fromJson(reader, User[].class);
                        for (int i = 0; i < users.length; i++) {
                            if (users[i].getUsername().equals(user.getUsername())) {
                                users[i].setRegistered(true);
                                break;
                            }
                        }
                        try (Writer writer = new FileWriter("res/Users.json")) {
                            gson.toJson(users, writer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    registrationTable.getItems().remove(user);

                });

                rejectButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    // Remove the rejected user from the file
                    try (Reader reader = new FileReader("res/Users.json")) {
                        JsonArray usersArray = gson.fromJson(reader, JsonArray.class);
                        for (int i = 0; i < usersArray.size(); i++) {
                            JsonObject userObject = usersArray.get(i).getAsJsonObject();
                            if (userObject.get("username").getAsString().equals(user.getUsername())) {
                                usersArray.remove(i);
                                break;
                            }
                        }
                        try (Writer writer = new FileWriter("res/Users.json")) {
                            gson.toJson(usersArray, writer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Refresh table data
                    registrationTable.getItems().remove(user);
                });
            }

            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (user == null || empty) {
                    setGraphic(null);
                } else {
                    HBox buttonsContainer = new HBox();
                    buttonsContainer.setSpacing(10);
                    buttonsContainer.getChildren().addAll(approveButton, rejectButton);
                    setGraphic(buttonsContainer);
                }
            }
        });
    }


    @FXML
    public void employeeStat(ActionEvent actionEvent) throws IOException {
        ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/gui/EmployeeStatus.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Employee Status");
        stage.setScene(new Scene(root));
        stage.show();

        TableView<User> employeeStatus = (TableView<User>) root.lookup("#employeeStatus");

        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        TableColumn<User, String> statusColumn = new TableColumn<>("Status");

        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isOnDuty() ? "Working" : "Break"));

        employeeStatus.getColumns().setAll(usernameColumn, statusColumn);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Reader reader = new FileReader("res/Users.json")) {
            User[] users = gson.fromJson(reader, User[].class);
            ObservableList<User> unregisteredUsers = FXCollections.observableArrayList(
                    Arrays.stream(users)
                            .filter(User::isRegistered)
                            .collect(Collectors.toList()));
            employeeStatus.setItems(unregisteredUsers);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void accumHrs(ActionEvent actionEvent) throws IOException {
        ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/gui/TimeSelect.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Select Time");
        stage.setScene(new Scene(root));
        stage.show();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DatePicker s = (DatePicker) loader.getNamespace().get("startDate");
        DatePicker e = (DatePicker) loader.getNamespace().get("endDate");
        Button genReport = (Button)  root.lookup("#genReportButton");
        genReport.setOnAction(event -> {
            LocalDate start = s.getValue();
            LocalDate end = e.getValue();

            if (start != null && end != null) {
                this.start = dateFormat.format(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                this.end = dateFormat.format(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            Stage stage2 = (Stage) genReport.getScene().getWindow();
            genReport();
            stage2.close();
        });

    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    @FXML
    public void backPressed(ActionEvent actionEvent) {
        ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/gui/AdminHome.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Admin");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void genReport(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/gui/AccumulatedHours.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Admin");
            stage.setScene(new Scene(root));
            stage.show();
            RMIServer rmiServer = new RMIServer();
            List<Report> reportList = rmiServer.generateReport(getStart(), getEnd());

            if(reportList != null){
                TableView<Report> reportTableView = (TableView<Report>) root.lookup("#timeLogs");
                TableColumn<Report, String> dateCol = new TableColumn<>("Date");
                TableColumn<Report, String> empCol = new TableColumn<>("Employee");
                TableColumn<Report, String> timeCol = new TableColumn<>("Time Rendered");

                dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
                empCol.setCellValueFactory(new PropertyValueFactory<>("username"));
                timeCol.setCellValueFactory(new PropertyValueFactory<>("timeRendered"));

                reportTableView.getColumns().setAll(dateCol, empCol, timeCol);

                ObservableList<Report> reports = FXCollections.observableArrayList();

                reports.addAll(reportList);

                reportTableView.setItems(reports);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


}
