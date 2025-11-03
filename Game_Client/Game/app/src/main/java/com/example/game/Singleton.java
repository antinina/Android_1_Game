package com.example.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Singleton {
    private static Singleton single_instance = null;

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private boolean ready = false;

    //Imamo samo jednog reader-a
    private ReceiveMessageFromServer reader;

    private Singleton(String host , int port ) throws IOException {
        this.socket = new Socket(host, port);
        this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
        this.ready = true;
    }

    public static synchronized Singleton getInstance(String host , int port) {
        try {
            if (single_instance == null)
                single_instance = new Singleton(host,port);
        } catch (IOException e) {
            single_instance = null;
        }
        return single_instance;
    }

    public static synchronized Singleton getInstance() {
        if (single_instance == null) {
            throw new IllegalStateException("Singleton not initialized. Call getInstance(host, port) first.");
        }
        return single_instance;
    }

    public Socket getSocket() { return socket; }
    public BufferedReader getBr() { return br; }
    public PrintWriter getPw() { return pw; }
    public boolean isReady() { return ready; }

    //Dodatno
    public void setReader(ReceiveMessageFromServer reader) {
        this.reader = reader;
    }

    public ReceiveMessageFromServer getReader() {
        return reader;
    }
}
