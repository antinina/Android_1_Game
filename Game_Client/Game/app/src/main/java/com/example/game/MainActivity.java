package com.example.game;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {


    public static String REQUEST_MESSAGE = "Request_key";

    //UI
    EditText etUsername;
    EditText etIP;
    EditText etPort;
    Button btnConnect;
    Spinner spnAvailablePlayers;
    Button btnStart;
    TextView tvPlayers;

    //Komunikacija
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;

    private String host;
    private int port;
    //Ime klijenta
    private String clientUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//UI komponente - init
        etUsername = findViewById(R.id.etUsername);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvPlayers = findViewById(R.id.tvPlayers);
        btnConnect = findViewById(R.id.btnConnect);
        btnStart = findViewById(R.id.btnStart);
        spnAvailablePlayers = findViewById(R.id.spAvailablePlayers);

//Prikaz
        tvPlayers.setVisibility(View.INVISIBLE);
        btnStart.setVisibility(View.INVISIBLE);
        btnStart.setEnabled(false);
        spnAvailablePlayers.setEnabled(false);
        spnAvailablePlayers.setVisibility(View.INVISIBLE);

//Connect to Server
        btnConnect.setOnClickListener(view -> {
            host = etIP.getText().toString();
            String portStr = etPort.getText().toString();
            clientUsername = etUsername.getText().toString();

            if (host.isEmpty() || portStr.isEmpty() || clientUsername.isEmpty()) {
                Toast.makeText(MainActivity.this, "Za konekciju potrebno uneti username, IP adresu i port", Toast.LENGTH_SHORT).show();
                return;
            }

            port = Integer.parseInt(portStr);
            connectToServer();



        });

//Izbor protivnika, zapocni igru?
        btnStart.setOnClickListener(view -> {
            Object selected = spnAvailablePlayers.getSelectedItem();
            Log.d("MY_APP", "Selected opponent: " + selected); // debug

            if (selected == null) {
                Toast.makeText(MainActivity.this, "Nema odabranog protivnika!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pw != null) {
                String req = "Choose opponent:" + selected.toString();
                Log.d("MY_APP", "Sending request: " + req); // debug
                sendMessage(req);  // pw must be valid!
            } else {
                Log.d("MY_APP", "pw is null! Cannot send request");
                Toast.makeText(MainActivity.this, "Connection lost, reconnect first!", Toast.LENGTH_SHORT).show();
            }
        });
//
//
//
//        btnStart.setOnClickListener(view -> {
//
//
//            Object selected = spnAvailablePlayers.getSelectedItem();
//            if (selected == null) {
//                Toast.makeText(MainActivity.this, "Nema odabranog protivnika!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (pw != null) {
//                String req = "Choose opponent:" + selected.toString();
//
//                sendMessage(req);  // pw must be valid!
//            } else {
//                Toast.makeText(MainActivity.this, "Connection lost, reconnect first!", Toast.LENGTH_SHORT).show();
//            }
//        });

    }//kraj onCreate

    //Getteri
    public BufferedReader getBr(){ return this.br; }
    public PrintWriter getPw(){ return this.pw; }
    public Spinner getAvailablePlayers(){ return this.spnAvailablePlayers; }
    public String getClientUsername(){ return this.clientUsername; }

    //	Prozor koji iskace korisniku sa nekim obavestenjem
    public void showInfoAlert(String title, String message) {
        runOnUiThread(() ->
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show()
        );
    }

    /**
     *	Metoda za konekciju sa serverom, u pozadini pravi thread za komunikaciju
     */
    public void connectToServer(){
        new Thread(() -> {
            Singleton singleton = Singleton.getInstance(host,port);

            if (singleton != null) {
                socket = singleton.getSocket();
                br = singleton.getBr();
                pw = singleton.getPw();

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Client connected!", Toast.LENGTH_SHORT).show();
                    btnConnect.setEnabled(false);
                    etUsername.setEnabled(false);
                    etIP.setEnabled(false);
                    etPort.setEnabled(false);

                    spnAvailablePlayers.setEnabled(true);
                    spnAvailablePlayers.setVisibility(View.VISIBLE);
                    tvPlayers.setVisibility(View.VISIBLE);
                    btnStart.setVisibility(View.VISIBLE);
                    btnStart.setEnabled(true);
                });

                // start the single reader thread


                  ReceiveMessageFromServer reader = new ReceiveMessageFromServer(MainActivity.this);
                new Thread(reader).start();
//                Ovo potrebno jer imamo jednog readera a 2 niti ce zeleti da citaju
                singleton.setReader(reader);

                pw.println(clientUsername);


            }
            else {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Socket cannot be created", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }


    //	Slanje poruka serveru
    public void sendMessage(String message){
        new Thread(() -> {
            if (pw != null){
                pw.println(message);
            }
        }).start();
    }

    //	Launcher za drugu aktivnost
//    ActivityResultLauncher<Intent> activity2Launcher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == RESULT_OK) {
//
//                }
//            }
//    );

    ActivityResultLauncher<Intent> activity2Launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String msg = data.getStringExtra(GameActivity.RESPONSE_MESSAGE);
                        Log.d("MY_APP", "Returned from GameActivity: " + msg);

                    }
                }
            }
    );



    //	Metod aza pocetak igre, prelaz u aktivnost za igru
    public void startGame(String userColor) {
        Log.d("MY_APP", "Ovo saljem u drugi activity " + userColor );
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra(REQUEST_MESSAGE, userColor);
        activity2Launcher.launch(intent);
    }
}
