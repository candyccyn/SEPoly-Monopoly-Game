package socketConnection;

import model.ServerMessage;
import model.Space;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
    private Socket socket = null;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    final private int SOCKETINPUTTIMEOUT = 10;

    Client(String address, int port) throws IOException {
        socket = new Socket(address, port);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void sendData(Object data) throws IOException {
        outputStream.writeUnshared(data);
        outputStream.reset();
        outputStream.flush();
    }

    public ServerMessage getData() throws SocketException {
        try {
            return (ServerMessage) inputStream.readUnshared();
        } catch (Exception e) {
            return null;
        }
    }
}

//override action perform and send update client info