package org.example.clientsevermsgexample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientView {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private final MainController controller;

    public ClientView(MainController controller) {
        this.controller = controller;
    }

    public  void connectToServer(String host, int port) throws IOException {
            new Thread(() -> {
                try {
                    socket = new Socket(host, port);
                    controller.receiveMessageFromServer("Connected to server at " + host + ":" + port);
                    output = new PrintWriter(socket.getOutputStream(), true);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println("Connected to server");

                    String serverMessage;
                    while ((serverMessage = input.readLine()) != null) {
                        controller.receiveMessageFromServer(serverMessage);
                    }
                } catch (IOException e) {
                    controller.receiveMessageFromServer(e.getMessage());
                }
        }).start();
    }

    public void sendMessage(String message){
        if(output != null){
            output.println(message);
        }
    }

    public void disconnect() throws IOException{
        if(input != null){
            input.close();
        }
        if(output != null){
            output.close();
        }
        if(socket != null){
            socket.close();
        }
    }
}
