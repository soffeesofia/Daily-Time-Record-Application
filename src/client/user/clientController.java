package client.user;

import exception.AlreadyTimedIn;
import exception.AlreadyTimedOut;
import exception.UsernameExistsException;
import interfaces.ClientInterface;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import object.*;
import server.RMIServer;

import java.util.*;


import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.util.Timer;

public class clientController {

    RMIClientGUI rmiClientGUI = new RMIClientGUI();
    @FXML
    private Label userName;
    @FXML
    private Button exitButton;
    @FXML
    private Button summarryButton;
    @FXML
    private Button timeInButton;
    @FXML
    private Label timeAndDate;
    @FXML
    private Button timeOutButton;
    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passField;

    @FXML
    private Button registerButton;

    @FXML
    private TextField userField;
    @FXML
    private TextField firstNameEntered;
    @FXML
    private TextField lastNameEntered;
    @FXML
    private TextField usernameEntered;
    @FXML
    private PasswordField passwordEntered;
    @FXML
    private Button requestClicked;
    @FXML
    private Button acceptButton;
    @FXML
    private Button rejectButton;
    @FXML
    private Label currentStatus;

    public TableView<RecordPair> timeSheet;

    public TableColumn<RecordPair, String> timeInCol;
    public TableColumn<RecordPair, String> timeOutCol;

    public Label timeRendered;
    String globalUsername;


    @FXML
    void loginPressed(ActionEvent eventa) {
        String username = userField.getText();
        String password = passField.getText();
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 10000);
            ClientInterface remote = (ClientInterface) registry.lookup("samplermi");
            if (remote.login(username, password) != null) {
                try {

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("clientgui/ClientHome.fxml"));
                    Parent root = loader.load();
                    Stage stage = new Stage();
                    stage.setTitle("Client");
                    stage.setScene(new Scene(root));
                    stage.show();
                    clientController controller = loader.getController();

                    controller.globalUsername = username;
                    controller.userName.setText(username);
                    controller.currentStatus.setText(remote.getCurrentStatus(username));
                    ((Node) (eventa.getSource())).getScene().getWindow().hide();

                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                        try {
                            controller.timeAndDate.setText(remote.getCurrentDateAndTime());
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                    timeline.setCycleCount(Animation.INDEFINITE);
                    timeline.play();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void registerPressed(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("clientgui/ClientRegister.fxml"));
            Parent root = loader.load();

            // Create a new stage to display the registration form
            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.setScene(new Scene(root));
            stage.show();

            ((Node) (event.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void backClicked(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientgui/ClientHome.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("Home Page");
        stage.setScene(scene);
    }

    @FXML
    void backClicked2(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientgui/ClientLogin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setTitle("Log-In Page");
        stage.setScene(scene);
    }


    @FXML
    void exitClicked(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    void logOutPressed(ActionEvent event) throws IOException {
        Node node = (Node) event.getSource();
        Stage thisStage = (Stage) node.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientgui/ClientLogin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        thisStage.setScene(scene);
    }

    @FXML
    void summaryClicked(ActionEvent event) throws IOException {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 10000);
            ClientInterface remote = (ClientInterface) registry.lookup("samplermi");
            TimeRecord timeRecord = remote.summarize(globalUsername, new Date());

            if (timeRecord != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("clientgui/ClientReport.fxml"));
                Parent parent = loader.load();
                Scene scene = new Scene(parent);

                Stage newStage = new Stage();
                newStage.setTitle("Summary Page");
                newStage.setScene(scene);
                newStage.show();

                ArrayList<String> timeInRecs = new ArrayList<>();
                ArrayList<String> timeOutRecs = new ArrayList<>();
                ArrayList<LocalTime> in = new ArrayList<>();
                ArrayList<LocalTime> out = new ArrayList<>();

                List<TimeDetails> timeDetailsList = timeRecord.getTimeDetails();
                for (TimeDetails d : timeDetailsList) {
                    if (d.getRecordType().equals("Time In")) {
                        timeInRecs.add(d.getTime());
                        in.add(LocalTime.parse(d.getTime()));
                    } else if (d.getRecordType().equals("Time Out")) {
                        timeOutRecs.add(d.getTime());
                        out.add(LocalTime.parse(d.getTime()));
                    }
                }

                TableView<RecordPair> time_sheet = (TableView<RecordPair>) parent.lookup("#timeSheet");
                TableColumn<RecordPair, String> inCol = new TableColumn<>("Time In");
                TableColumn<RecordPair, String> outCol = new TableColumn<>("Time Out");

                inCol.setCellValueFactory(new PropertyValueFactory<>("timeIn"));
                outCol.setCellValueFactory(new PropertyValueFactory<>("timeOut"));
                time_sheet.getColumns().setAll(inCol, outCol);

                ObservableList<RecordPair> records = FXCollections.observableArrayList();

                for (int i = 0; i < timeInRecs.size(); i++) {
                    try {
                        RecordPair record = new RecordPair(timeInRecs.get(i), timeOutRecs.get(i));
                        records.add(record);
                    } catch (IndexOutOfBoundsException e) {
                        RecordPair record = new RecordPair(timeInRecs.get(i), "");
                        records.add(record);
                    }
                }
                time_sheet.setItems(records);

                java.time.Duration duration = java.time.Duration.ZERO;

                for (int j = 0; j < in.size(); j++) {
                    LocalTime i = in.get(j);
                    LocalTime o = out.get(j);
                    duration = duration.plus(java.time.Duration.between(i, o));
                }

                Label timeRen = (Label) parent.lookup("#timeRendered");
                timeRen.setText(""+LocalTime.ofSecondOfDay(duration.getSeconds()));

            }
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    void timeInClicked(ActionEvent event) throws RemoteException {

        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 10000);
            ClientInterface remote = (ClientInterface) registry.lookup("samplermi");
            remote.timeIn(globalUsername);
            currentStatus.setText("Working");
        } catch (NotBoundException | AlreadyTimedIn e) {
            throw new RuntimeException(e);
        }


    }

    @FXML
    void timeOutClicked(ActionEvent event) throws RemoteException {
        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 10000);
            ClientInterface remote = (ClientInterface) registry.lookup("samplermi");
            remote.timeOut(globalUsername);
            currentStatus.setText("On Break");
        } catch (NotBoundException | AlreadyTimedOut e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    void requestPressed(ActionEvent event) {
        String firstName = firstNameEntered.getText();
        String lastName = lastNameEntered.getText();
        String username = usernameEntered.getText();
        String password = passwordEntered.getText();
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "All fields are required.");
            alert.showAndWait();
        } else {
            try {
                Registry registry = LocateRegistry.getRegistry("localhost", 10000);
                ClientInterface remote = (ClientInterface) registry.lookup("samplermi");
                remote.register(username,password,firstName,lastName);
            } catch (NotBoundException | RemoteException | UsernameExistsException e) {
                throw new RuntimeException(e);
            }
            requestClicked.setText("Registration request has been sent!");
        }
    }

}