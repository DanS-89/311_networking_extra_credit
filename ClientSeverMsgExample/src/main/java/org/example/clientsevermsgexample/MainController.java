package org.example.clientsevermsgexample;



import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class MainController implements Initializable {

    @FXML
    private ComboBox dropdownPort;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dropdownPort.getItems().addAll("7",     // ping
                "13",     // daytime
                "21",     // ftp
                "23",     // telnet
                "71",     // finger
                "80",     // http
                "119",     // nntp (news)
                "161"      // snmp);
        );
    }

    @FXML
    private Button clearBtn;
    @FXML
    private TextArea resultArea;
    @FXML
    private Label server_lbl;
    @FXML
    private Button testBtn;
    @FXML
    private Label test_lbl;
    @FXML
    private TextField urlName;
    @FXML
    private Button clientSendButton;
    @FXML
    private Button serverSendButton;
    @FXML
    private TextField clientMessageTextField;
    @FXML
    private TextField serverMessageTextField;
    @FXML
    private TextArea clientChatArea;
    @FXML
    private TextArea serverChatArea;

    private final ServerView serverView;
    private final ClientView clientView;

    public MainController(){
        clientView = new ClientView(this);
        serverView = new ServerView(this);
    }

    Socket socket1;
    Label lb122, lb12;
    TextField msgText;

    @FXML
    void checkConnection(ActionEvent event) {
        String host = urlName.getText().isEmpty() ? "localhost" : urlName.getText();
        int port = Integer.parseInt(dropdownPort.getValue().toString());
        try {
            Socket sock = new Socket(host, port);
            resultArea.appendText(host + " listening on port " + port + "\n");
            sock.close();
        } catch (Exception e) {
            resultArea.appendText(host + " not listening on port "
                    + port + "\n");
        }
    }

    @FXML
    void clearBtn(ActionEvent event) {
        resultArea.setText("");
        urlName.setText("");
    }

    @FXML
    void startServer(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();
        Label lb11 = new Label("Server");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);

        lb12 = new Label("info");
        lb12.setLayoutX(100);
        lb12.setLayoutY(200);
        root.getChildren().addAll(lb11, lb12);
        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        lb12.setText("Server is running and waiting for a client...");

        stage.setTitle("Server");
        stage.show();

            try {
                int port = Integer.parseInt(dropdownPort.getValue().toString());
                serverView.serverStart(port);
                receiveMessageFromClient("Server started and waiting on client");
            } catch (IOException e) {
                receiveMessageFromClient("Error starting server: " + e.getMessage());
            }
    }

    String message;

    private void runServer() {
        try {

            ServerSocket serverSocket = new ServerSocket(6666);
            updateServer("Server is running and waiting for a client...");
            while (true) { // Infinite loop
                try {
                    Socket clientSocket = serverSocket.accept();
                    updateServer("Client connected!");

                    new Thread(() -> {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

                    message = dis.readUTF();
                    updateServer("Message from client: " + message);

                    // Sending a response back to the client
                    dos.writeUTF("Received: " + message);

                    dis.close();
                    dos.close();

                } catch (IOException e) {
                    updateServer("Error: " + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (message.equalsIgnoreCase("exit")) break;

            }
        } catch (IOException e) {
            updateServer("Error: " + e.getMessage());
        }
    }

    private void updateServer(String message) {
        // Run on the UI thread
        javafx.application.Platform.runLater(() -> lb12.setText(message + "\n"));
    }

    @FXML
    void startClient(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();
        Button connectButton = new Button("Connect to server");
        connectButton.setLayoutX(100);
        connectButton.setLayoutY(300);
        connectButton.setOnAction(this::connectToServer);
        // new Thread(this::connectToServer).start();

        Label lb11 = new Label("Client");
        lb11.setLayoutX(100);
        lb11.setLayoutY(100);
        msgText = new TextField("msg");
        msgText.setLayoutX(100);
        msgText.setLayoutY(150);

        lb122 = new Label("info");
        lb122.setLayoutX(100);
        lb122.setLayoutY(200);
        root.getChildren().addAll(lb11, lb122, connectButton, msgText);


        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.setTitle("Client");
        stage.show();

        try {
            String host = "localhost";
            int port = Integer.parseInt(dropdownPort.getValue().toString());
            clientView.connectToServer(host, port);
        } catch (IOException e) {
            receiveMessageFromClient("Error connecting to server: " + e.getMessage());
        }
    }

    private void connectToServer(ActionEvent event) {
        try {
            int port = Integer.parseInt(dropdownPort.getValue().toString());
            String host = "localhost";
            clientView.connectToServer(host, port);

            DataOutputStream dos = new DataOutputStream(socket1.getOutputStream());
            DataInputStream dis = new DataInputStream(socket1.getInputStream());

            dos.writeUTF(msgText.getText());
            String response = dis.readUTF();
            updateTextClient("Server response: " + response + "\n");

            dis.close();
            dos.close();
            socket1.close();
        } catch (Exception e) {
            updateTextClient("Error: " + e.getMessage() + "\n");
        }
    }

    private void updateTextClient(String message) {
        // Run on the UI thread
        javafx.application.Platform.runLater(() -> {
            lb122.setText(lb122.getText() + message + "\n");
        });
    }

    @FXML
    void sendClientMessage(ActionEvent event) {
        String message = clientMessageTextField.getText();
        if (!message.isEmpty()) {
            clientView.sendMessage(message);
            clientChatArea.appendText(message + "\n");
            clientMessageTextField.clear();
        }
    }

    @FXML
    void sendServerMessage(ActionEvent event) {
        String message = serverMessageTextField.getText();
        if (!message.isEmpty()) {
            serverView.sendMessage(message);
            serverChatArea.appendText(message + "\n");
            serverMessageTextField.clear();
        }
    }

    public void receiveMessageFromServer(String message) {
        Platform.runLater(() ->
                clientChatArea.appendText("Server: " + message + "\n"));
    }

    public void receiveMessageFromClient(String message){
        Platform.runLater(() ->
                serverChatArea.appendText("Client: " + message + "\n"));
    }
}
