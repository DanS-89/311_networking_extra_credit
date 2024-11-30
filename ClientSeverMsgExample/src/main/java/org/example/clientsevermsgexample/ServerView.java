package org.example.clientsevermsgexample;

import javax.imageio.event.IIOWriteProgressListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerView {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private final MainController controller;

    public ServerView(MainController controller) {
        this.controller = controller;
    }

    public void serverStart(int port) throws IOException {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                controller.receiveMessageFromClient("Server started on port " + port);
                clientSocket = serverSocket.accept();
                controller.receiveMessageFromClient("Client connected");
                output = new PrintWriter(clientSocket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String clientMessage;
                while ((clientMessage = input.readLine()) != null) {
                    controller.receiveMessageFromClient(clientMessage);
                    }
            } catch (IOException e) {
                e.printStackTrace();
                controller.receiveMessageFromClient("Client disconnected");
            }
        }).start();
    }

    public void sendMessage(String message) {
        if(output != null) {
            output.println(message);
        }
    }

    public void serverStop() throws IOException{
        try{
        if (input != null) {
            input.close();
        }

        if (output != null){
            output.close();
        }

        if (clientSocket != null){
            clientSocket.close();
        }

        if (serverSocket != null) {
            serverSocket.close();
            controller.receiveMessageFromClient("Server stopped");
        }
        } catch (IOException e) {
            controller.receiveMessageFromClient("Server stopped");
            }
    }
}
