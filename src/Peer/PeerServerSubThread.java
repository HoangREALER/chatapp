package Peer;

import javax.json.Json;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class PeerServerSubThread extends Thread {
    private PeerServerThread serverThread;
    private Socket socket;
    private boolean flag = true;

    public PeerServerSubThread(PeerServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            while(flag)
                serverThread.processJson(Json.createReader(reader).readObject().toString(), socket);
        } catch (Exception e) {
//            e.printStackTrace();
            stopServerSubThread();
        } finally {
            stopServerSubThread();
        }
    }

    public void stopServerSubThread() {
        try {
            flag = false;
            socket.close();
            serverThread.getSubThreads().remove(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


