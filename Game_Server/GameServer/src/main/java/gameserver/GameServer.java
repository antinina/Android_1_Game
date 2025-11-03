/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package gameserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameServer {

    private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedClient> clients;

    
    public ServerSocket getSsocket() {
        return ssocket;
    }

    public void setSsocket(ServerSocket ssocket) {
        this.ssocket = ssocket;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Prihvata u petlji klijente i za svakog novog klijenta kreira novu nit. Iz
     * petlje se moze izaci tako sto se na tastaturi otkuca Exit.
     */
    
    public void acceptClients() {
        Socket client = null;
        Thread thr;
        while (true) {
            try {
                System.out.println("Waiting for new clients..");
                client = this.ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client != null) {
            
                ConnectedClient clnt = new ConnectedClient(client, clients);
                clients.add(clnt);
                System.out.println("Client added");
                //nit koja opsluzuje klijenta
                thr = new Thread(clnt);                
                thr.start();
                
            } else {
                System.out.println("smt wrong");
                break;
            }
        }
    }

    /**
     * Konstruktor
     * @param port
     */
    public GameServer(int port) {
        
        this.clients = new ArrayList<>();
    
        try {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static void main(String[] args) {
        
        GameServer server = new GameServer(6001);

        System.out.println("Server pokrenut, slusam na portu 6001");

        //Prihvataj klijente u beskonacnoj petlji
        server.acceptClients();

    }

}
