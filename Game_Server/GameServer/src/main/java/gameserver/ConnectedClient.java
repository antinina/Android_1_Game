/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gameserver;

import java.io.BufferedReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
//import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedClient implements Runnable {

    //ovo ostaje isto:
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedClient> allClients;
    private int state;
    
    
    //Dodatna polja:
    private String opponentName;
    private boolean available; //false if client is already playing, otherwise true
    private boolean playsFirst;//
    private boolean myTurn;//true - igrac na potezy, false - protivnikov potez
    //private boolean won;
 
    private boolean reqAnswered;
    private boolean rematch;
    
    
//    private HashMap < Coordinates, Integer> gameBoard;
    private HashMap < String, Integer> gameBoard;

    //getters and setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setState(int state)
    {this.state = state;}
    
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isPlaysFirst() {
        return playsFirst;
    }

    public void setPlaysFirst(boolean playsFirst) {
        this.playsFirst = playsFirst;
    }

    public void setOpponent(String opponentName) {
        this.opponentName = opponentName ;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }
    
    //Boolean polja   
    public boolean isReqAnswered() {
            return reqAnswered;
        }
    public boolean isRematch() {
            return rematch;
        }
    
   private void setReqAnswered(boolean b) {
       this.reqAnswered  = b;
    }

    private void setRematch(boolean b) {
        this.rematch = b;
    }
    
    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public BufferedReader getBr() {
        return br;
    }

    public void setBr(BufferedReader br) {
        this.br = br;
    }

    public PrintWriter getPw() {
        return pw;
    }

    public void setPw(PrintWriter pw) {
        this.pw = pw;
    }
    
    public void updateBoard(int row,int col,int playerNum)
    {
        String key = row+","+col;
        this.gameBoard.put(key, playerNum);
    
    }        
    
//----> Logika igre    ---------------------------------
    public void initEmptyBoard()
    {
        //ako je vrednost 0 znaci da nema diskova
        //1 - igrac1, 2 - igrac2
    //private HashMap < Coordinates, Integer> gameBoard;
        for(int i=0; i<6;i++)        
            for(int j=0; j<7; j++)            
                gameBoard.put(i+","+j, 0);           
        
    }
    
    public void printBoard()
    {
            for(int i=0; i<6;i++)        
            {    for(int j=0; j<7; j++)
                {    String key = i+","+j;
                    System.out.print(gameBoard.get(key)+" ");
                }       
                System.out.print("\n");
            }    
    }   
    
    //Postavlja disk u odgovarajucu kolonu koju je igrac trazio
    public int placeDisc(int col, int playerNum)
    {
        //test
        // System.out.println("Before");
       // this.printBoard();
        
//Naci najveci broj reda koji nije zauzet
        int row = 10;
        for(int i=5; i>=0; i--)//krece odozdo navise
        {
            String key = i + "," + col;//red,col
            if(gameBoard.get(key) == 0)
            {
                gameBoard.put(key,playerNum);
                System.out.println("Player num " + playerNum);
                row = i;
                break;
            }    
        
        }
        
//        System.out.println("After");
 //       this.printBoard();
 //       System.out.println("Place in row,col");
        return row;
        
    }
    //Proverava pravila igre i vraca da li je igrac na potezu pobedio tj da li je igra zavrsena
    public boolean checkRules(int row, int col, int playerNum) {
    boolean won = false;
    int rule = 0;
   
    // rows 0..5, cols 0..6
    do {
        int cnt = 1; // jer pocinjem vec od tog prvog koji je postavljen

        switch (rule) {
            case 0: // vertical
                // samo nadole proveravam
                for (int i = row + 1; i <= 5; i++)
                {
                    String key = i + "," + col;
                    
                    if (gameBoard.get(key) != null && gameBoard.get(key) == playerNum)
                    { 
                        cnt++;
                        //System.out.println("Found " + cnt);
                    } 
                    else break;
                }
                break;

            case 1: // horizontal
                // check left
                for (int i = col - 1; i >= 0; i--)
                {
                    String key = row + "," + i;
                    //Coordinates key = new Coordinates(row, i);
                    if (gameBoard.get(key) != null && gameBoard.get(key) == playerNum)
                    {
                        cnt++;
                    }
                    else break;
                }
                // check right
                for (int i = col + 1; i <= 6; i++)
                {
                    String key = row + "," + i;
                    //Coordinates key = new Coordinates(row, i);
                    if (gameBoard.get(key) != null && gameBoard.get(key) == playerNum) 
                    {
                        cnt++;
                    } else break;
                }
                break;

            case 2: // diagonal '\,
                // down-right
                for (int i = 1; (row + i) <= 5 && (col + i) <= 6; i++) 
                {
                    //Coordinates key = new Coordinates(row + i, col + i);
                    String key = (row+i) + "," + (col+i);
                    if (gameBoard.get(key) != null && gameBoard.get(key) == playerNum)
                    {
                        cnt++;
                    }
                    else break;
                }
                // up-left
                for (int i = 1; (row - i) >= 0 && (col - i) >= 0; i++)
                {
                    //Coordinates key = new Coordinates(row - i, col - i);
                    String key = (row-i) + "," + (col-i);
                    if (gameBoard.get(key) != null && gameBoard.get(key) == playerNum)
                    {
                        cnt++;
                    }
                    else break;
                }
                break;

            case 3: // diagonal ./'
                // down-left
                for (int i = 1; (row + i) <= 5 && (col - i) >= 0; i++)
                {
                    String key = (row+i) + "," + (col-i);
                    //Coordinates key = new Coordinates(row + i, col - i);
                    if (gameBoard.get(key) != null && gameBoard.get(key) == playerNum)
                    {
                        cnt++;
                    } else break;
                }
                // up-right
                for (int i = 1; (row - i) >= 0 && (col + i) <= 6; i++) 
                {
                    String key = (row-i) + "," + (col+i);
                    //Coordinates key = new Coordinates(row - i, col + i);
                    if (gameBoard.get(key) != null && gameBoard.get(key) == playerNum) {
                        cnt++;
                    } else break;
                }
                break;

            default:
                rule = 4; // stop loop
        }

        if (cnt == 4) {
            won = true;
            rule = 4; // tjt nasli smo
        } else {
            rule++;
        }

    } while (rule < 4);

    return won;
}

 
    
    
//Konstruktor klase, prima kao argument socket kao vezu sa uspostavljenim klijentom
    public ConnectedClient(Socket socket, ArrayList<ConnectedClient> allClients) {
  
        this.socket = socket;
        this.allClients = allClients;

        //iz socket-a preuzmi InputStream i OutputStream
        try {
            //posto se salje tekst, napravi BufferedReader i PrintWriter
            //kojim ce se lakse primati/slati poruke (bolje nego da koristimo Input/Output stream
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            //zasad ne znamo user name povezanog klijenta
            
            this.userName = "";
            this.opponentName =  "";
            this.available = true;
            this.playsFirst = false;
            this.myTurn = false;
    
            this.gameBoard = new HashMap();
            this.initEmptyBoard();
            this.reqAnswered = false;
            this.rematch = false;
            
            
        } catch (IOException ex) {
            Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda salje listu dostupnih igraca 
     */
    void connectedAvailableClientsUpdateStatus() {
        
       
        //Posalji svim korisnicima spisak trenutno dostupnih igraca 
        String availableUsers = "Users:";
        String connectedUsers = "Users:";
        
        for (ConnectedClient c : this.allClients) {
            connectedUsers += " " + c.getUserName() ;
            
            if(c.isAvailable())                    
            {  
                availableUsers += " " + c.getUserName();
            }
        }
      
        
        for (ConnectedClient svimaUpdateCB : this.allClients) {
           if(svimaUpdateCB.isAvailable())                    
            {  
                //availableUsers += " " + c.getUserName();
                svimaUpdateCB.pw.println(availableUsers);
            }
            
        }

        System.out.println("\tDostupni igraci" + availableUsers);
        System.out.println("\tPrikljuceni " + connectedUsers);
    }

    /**
     * 
     */
    private void removeClient() {


        //obrisi klijenta iz liste
        Iterator<ConnectedClient> it = this.allClients.iterator();
        while (it.hasNext()) {
            if (it.next().getUserName().equals(this.userName)) {
                it.remove();
            }
        }
        connectedAvailableClientsUpdateStatus();

        try {
            this.socket.close();
        } catch (IOException ex1) {
            System.getLogger(ConnectedClient.class.getName())
                  .log(System.Logger.Level.ERROR, (String) null, ex1);
        }
}
    
    
    
  //--------------->    LOGIKA PROGRAMA ---------------------->

@Override
public void run() {
    try {
        while (true) {
            String clientMsg = null;

            
            if (br.ready()) {
                clientMsg = br.readLine();
            }

            if (clientMsg != null) {
                if (clientMsg == null) {
                    // client closed connection
                    System.out.println("Disconnected user: " + this.userName);
                    removeClient();
                    break;
                }
                else {
                    System.out.println("--> Klijent: " + this.userName + " Poruka: "+ clientMsg);
                    
                    
                    if (clientMsg.startsWith("Game start")) {
                        System.out.println("msg:" + clientMsg);
                        state = 2;
                    
                    
                    
                    }
                    System.out.println("!!! state "+ state);
                    switch (state) {
                        /**
                         * Logovanje korisnika
                         */
                        case 0:
                            if (this.userName.equals("")) {
                                System.out.println("Login->  " + clientMsg);
                                this.userName = clientMsg;

                                this.available = true;
                                this.state = 1;
                                connectedAvailableClientsUpdateStatus();
                            }
                            break; // kraj case 0

                        /**
                         * Izbor protivnika
                         */
                        case 1:
                            
                            System.out.println("Odabir protivnika ->  " + clientMsg);
                            String received[] = clientMsg.split(":");
                            // Da li ja iniciram igru
                            if (received[0].equals("Choose opponent") && received.length == 2) {
                                System.out.println("Potencijalni protivnik " + received[1]);

                                String req = "Request from:" + this.userName;
                                for (ConnectedClient clientOpp : this.allClients) {
                                    if (clientOpp.getUserName().equals(received[1])) {
                                        clientOpp.pw.println(req);
                                        System.out.println("Request sent to client!");
                                        break;
                                    }
                                }

                            }
                            // Pitanje za igru stize do klijenta B
                            // prenesi odgovor klijentu A
                            else if (received[0].equals("Answer to") && received.length == 3) {
                                System.out.println("Igrac je odgovorio");
                                if (received[2].equalsIgnoreCase("yes")) {
                                    this.opponentName = received[1];
                                    System.out.println("User:" + this.userName + " Oponent name is: " + this.opponentName);
                                    this.available = false;
                                    this.playsFirst = false;
                                    this.myTurn = false;
                                    for (ConnectedClient clientOpp : this.allClients) {
                                        if (clientOpp.getUserName().equals(this.opponentName)) {
                                            clientOpp.setAvailable(false);
                                            clientOpp.setPlaysFirst(true);
                                            clientOpp.setMyTurn(true);
                                            clientOpp.setOpponent(this.userName);//!Ovo
                                            clientOpp.setState(2);
                                            String answ = "Answer from:" + userName + ":yes";
                                            clientOpp.pw.println(answ);
                                            System.out.println("Client " + userName + " accepted the game!\n"
                                                    + "Response sent to opponent: " + opponentName);
                                            break;
                                        }
                                    }

                                    state = 2;

                                } else if (received[2].equalsIgnoreCase("no")) {
                                    for (ConnectedClient clientOpp : this.allClients) {
                                        if (clientOpp.getUserName().equals(received[1])) {
                                            String answ = "Answer from:" + userName + ":no";
                                            clientOpp.pw.println(answ);
                                            System.out.println("Client " + userName + "rejected the game!\n"
                                                    + "Response sent to opponent: " + opponentName);
                                            break;
                                        }
                                    }

                                    state = 1;// nije neophodno
                                }

                            }

                            connectedAvailableClientsUpdateStatus();
                            break;

                        /**
                         *      Igra
                         */
                        case 2:
                            System.out.println("*Provera podataka:\n" + this.opponentName + "-> Available: " + this.available +
                                    " Plays_first: " + this.playsFirst + " Is_turn " + this.myTurn);

                            // Delovi igre
                            if (clientMsg.startsWith("Move:")) {
                                if (this.myTurn) {
                                    int col = 0;
                                    String game[] = clientMsg.split(":");
                                    if (game.length == 2) {
                                        col = Integer.parseInt(game[1]);
                                        int row = placeDisc(col, (this.playsFirst ? 1 : 2));

                                        if (row > 5) {
                                            this.pw.println("Move rejected");// ne moze se ubaciti u tu kolonu
                                        } else {
                                            // Validan potez
                                            int color = (this.playsFirst ? 1 : 2);
                                            boolean res = checkRules(row, col, color);

                                            System.out.println("Game over?" + res);
                                            if (res) {
                                                this.myTurn = false;
                                                for (ConnectedClient clientOpp : this.allClients) {
                                                    if (clientOpp.getUserName().equals(this.opponentName)) {
                                                        clientOpp.setMyTurn(false);
                                                        clientOpp.pw.println("You lost");
                                                        break;
                                                    }
                                                }
                                                this.pw.println("You won");
                                            } else {
                                                System.out.println("game continues...");
                                                this.myTurn = false;
                                                this.pw.println("My move:" + row + "," + col);
                                                for (ConnectedClient clientOpp : this.allClients) {
                                                    if (clientOpp.getUserName().equals(this.opponentName)) {
                                                        String move = "Opponent's move:" + row + "," + col;
                                                        clientOpp.setMyTurn(true);
                                                        clientOpp.pw.println(move);
                                                        clientOpp.updateBoard(row, col, (this.isPlaysFirst() ? 1 : 2));
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("Nije red na igraca " + this.userName);
                                    this.pw.println("Not your turn!");
                                }
                            }
                            // kraj igre i pocetak nove
                            else if (clientMsg.startsWith("New game")) {
                                System.out.println("Here I am");
                                reqAnswered = true;
                                String answ = clientMsg.split(":")[1];
                                if (answ.equals("yes"))
                                    rematch = true;
                                else if (answ.equals("no"))
                                    rematch = false;

                                boolean oppAnswered = false;
                                boolean oppRematch = false;

                                for (ConnectedClient clientOpp : this.allClients) {
                                    if (clientOpp.getUserName().equals(this.opponentName)) {
                                        oppAnswered = clientOpp.isReqAnswered();
                                        oppRematch = clientOpp.isRematch();
                                        break;
                                    }
                                }

                                if (oppAnswered) {
                                    System.out.println("Oba igraca odgovorila");
                                    System.out.println("igrac " + rematch + " protivnik " + oppRematch);
                                    if (oppRematch && rematch) {
                                        System.out.println("Nova igra");
                                        // Slucaj kada se sprema nova igra
                                        this.initEmptyBoard();
                                        this.playsFirst = !this.playsFirst;
                                        this.myTurn = this.playsFirst;
                                        this.reqAnswered = false;
                                        this.rematch = false;

                                        this.pw.println("Rematch");

                                        for (ConnectedClient clientOpp : this.allClients) {
                                            if (clientOpp.getUserName().equals(this.opponentName)) {
                                                clientOpp.initEmptyBoard();
                                                boolean temp = !(clientOpp.isPlaysFirst());
                                                clientOpp.setPlaysFirst(temp);
                                                clientOpp.setMyTurn(temp);
                                                clientOpp.setReqAnswered(false);
                                                clientOpp.setRematch(false);

                                                clientOpp.getPw().println("Rematch");
                                                break;
                                            }
                                        }

                                    } else {
                                        System.out.println("Nema nove igre");
                                        this.initEmptyBoard();

                                        this.reqAnswered = false;
                                        this.rematch = false;
                                        this.available = true;

                                        System.out.println("***Provera podataka:\n" + this.opponentName + " Available " + this.available +
                                                " Plays_first " + this.playsFirst + " Is_turn " + this.myTurn);

                                        this.pw.println("No new game");

                                        for (ConnectedClient clientOpp : this.allClients) {
                                            if (clientOpp.getUserName().equals(this.opponentName)) {
                                                clientOpp.initEmptyBoard();
                                                clientOpp.setReqAnswered(false);
                                                clientOpp.setRematch(false);
                                                clientOpp.setAvailable(true);
                                                clientOpp.getPw().println("No new game");
                                                clientOpp.setState(1);
                                                break;
                                            }
                                        }
                                        this.opponentName = "";
                                        state = 1;
                                        connectedAvailableClientsUpdateStatus();
                                    }
                                }
                            }
                            break;

                        default:
                            System.out.println("Error!");
                    }
                }
            }

  
        }
    } catch (IOException ex) {
        System.out.println("Disconnected user (IOException): " + this.userName);
        removeClient();
    } 
}//kraj run






}//kraj class