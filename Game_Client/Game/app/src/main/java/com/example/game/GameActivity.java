package com.example.game;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class GameActivity extends AppCompatActivity {

    //Komunikacija sa serverom
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    //Thread serverThread;

    //info vazane za trenutnog igraca
    String username;
    boolean myTurn;
    String color;
    boolean playsFirst;

    //prikaz
    private final int rows = 6;
    private final int columns = 7;

    HashMap<String, ImageView> cell;
    LinearLayout layoutMatrix;
    TextView tvPlayer;
    ImageView playerColor;

    NumberPicker numberPicker;

    int currentMove;
    Button btnCol;
    //veza sa drugom aktivnoscu
    public static String RESPONSE_MESSAGE = "Response_text";

    //I ovo mi treba
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        intent = getIntent();
        String message = (String) intent.getExtras().getString(MainActivity.REQUEST_MESSAGE);
        //Log.d("GAME_APP", "dobijam od activity1 "+message);

/**
 *      Prikaz table
 * */
        //Na pocetku iz poruke izdvojimo ime za labelu, boju tj da li igra prvi
        String input []=message.split(":");
        if(input.length==2) {

            username = input[0];
            color = input[1];
            tvPlayer = (TextView) findViewById(R.id.playerName);
            tvPlayer.setText("User: " + username);

            playerColor = (ImageView) findViewById(R.id.playerColor);

            switch (color) {
                case "blue":
                    playerColor.setImageResource(R.drawable.blue);
                    break;
                case "red":
                    playerColor.setImageResource(R.drawable.red);
                    break;
                default:
                    playerColor.setImageResource(R.drawable.blank);


            }
        }

//--->Number picker za izbor kolone

        numberPicker = findViewById(R.id.numberPicker);
        // Set min and max values
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(6);
        //Poziv:
        //int value = numberPicker.getValue();

//--->Inicijalizacija table za igru:
        cell = new HashMap<String, ImageView>();
        layoutMatrix = findViewById(R.id.layoutMatrix);
        //po redovima
        for (int row = 0; row < rows; row++){
            LinearLayout llrow = new LinearLayout(this);
            llrow.setOrientation(LinearLayout.HORIZONTAL);

            //po kolonama
            for (int col = 0; col < columns; col++){
                ImageView iv = new ImageView(this);
                iv.setTag(row + "," + col + ",blank");
                cell.put(row + "," + col, iv);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,150);
                layoutParams.weight = 1;//svi jednaki
                layoutParams.setMargins(5,5,5,5);

                iv.setLayoutParams(layoutParams);
                iv.setImageResource(R.drawable.blank);
                llrow.addView(iv);
            }

            layoutMatrix.addView(llrow);
        }//

     //   placeDisc("0,5", "blue");

 //-->konektuj se na server

        connectToServer();
//--> Da ne bi doslo do konflikta, sve ide preko jednog reader-a

        ReceiveMessageFromServer reader = Singleton.getInstance().getReader();
        reader.setGameActivity(GameActivity.this);

        //ovo je proba
        sendMessage("Game start");


        btnCol = (Button)findViewById(R.id.btnCol);
//--->Posalji serveru potez
        btnCol.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                currentMove = numberPicker.getValue();
                String move = "Move:" + currentMove;
                sendMessage(move);

            }//kraj onclick


        });


    }//on-create

//Potrebno za komunikaciju sa serverom:
    public BufferedReader getBr() {
        return br;
    }

    //Komunikacija se odvija preko istog socketa ali u novom thread-u
    public void connectToServer(){
        //Log.d("GAME_APP", "Preuzmiod singleton-a");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Singleton singleton = Singleton.getInstance();
                if (singleton != null && singleton.isReady()){
                    GameActivity.this.socket = singleton.getSocket();
                    GameActivity.this.br = singleton.getBr();
                    GameActivity.this.pw = singleton.getPw();
                //    new Thread(new ReceiveMessageFromServerGame(GameActivity.this)).start();

                }
                else {

                    Log.d("GAME_APP", "GameActivity:Problem sa thread-om za komunikaciju");
                }

            }
        }).start();



    }

    //Slanje poruke serveru
    public void sendMessage(String message){

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (GameActivity.this.pw != null){
                    GameActivity.this.pw.println(message);

                }
            }
        }).start();
    }

    public void showInfoDialog(String title, String message) {
        runOnUiThread(() ->
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show()
        );
    }
    public void placeDisc(String position, String color) {
        ImageView targetCell = cell.get(position);

        if (targetCell != null) {
            int drawableRes = R.drawable.blank; // default

            // Map color names to drawable resources
            switch (color) {
                case "blue":
                    drawableRes = R.drawable.blue;
                    break;
                case "red":
                    drawableRes = R.drawable.red;
                    break;
                default:
                    drawableRes = R.drawable.blank;
                    //break;


            }

            targetCell.setImageResource(drawableRes);
            targetCell.setTag(position + "," + color.toLowerCase());
            cell.put(position,targetCell);
        } else {
            Log.w("GRID", "Cell " + position + " not found!");
        }
    }
    public void resetGame()
    {
        String key = "";
        for(int i = 0; i < rows; i++)
            for(int j = 0; j <columns;j++)
            {
                key = i + "," + j;
                this.placeDisc(key, "blank");

            }
    }

//kraj igre

public void finishGame() {
    Intent resultIntent = new Intent();
    resultIntent.putExtra(RESPONSE_MESSAGE, "Game over");
    setResult(RESULT_OK, resultIntent);
    Log.d("!!!", "Terminira igricu");
    finish(); // zatvara GameActivity
}

//New

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
        Singleton.getInstance().getReader().setGameActivity(null);
    }



}//class