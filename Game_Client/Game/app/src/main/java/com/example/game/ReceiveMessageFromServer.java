package com.example.game;

import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ReceiveMessageFromServer implements Runnable {
    private MainActivity parent;
    private GameActivity gameAct; // druga aktivnost?
    private BufferedReader br;
    private PrintWriter pw;

    public ReceiveMessageFromServer(MainActivity parent) {
        this.parent = parent;
        this.br = parent.getBr();
        this.pw = parent.getPw();
    }

    public void setGameActivity(GameActivity gameAct) {
        this.gameAct = gameAct;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String line = this.br.readLine();
                if (line == null) {
                    Log.d("MY_APP", "Server closed connection");
                    break; // exit loop
                } else {
                    Log.d("MY_APP", "***Poruka od servera: " + line + " klijentu " + parent.getClientUsername());

                    if (line.startsWith("Users:")) {
                        String[] players = line.split(":");
                        if (players.length > 1 && !players[1].trim().isEmpty()) {
                            String[] names = players[1].trim().split(" ");
                            parent.runOnUiThread(() -> {
                                Spinner spinner = parent.getAvailablePlayers();
                                ArrayList<String> filteredNames = new ArrayList<>();
                                for (String name : names) {
                                    if (!name.trim().equals(parent.getClientUsername())) {
                                        filteredNames.add(name.trim());
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        parent,
                                        android.R.layout.simple_spinner_item,
                                        filteredNames
                                );
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                            });
                        }
                    }
                    else if (line.startsWith("Request from:")) {
                        String oppName = line.split(":")[1];
                        parent.runOnUiThread(() -> {
                            AlertDialog.Builder gameReq = new AlertDialog.Builder(parent);
                            gameReq.setTitle("Zahtev za novu igru");
                            gameReq.setMessage("Igrac " + oppName + " zeli da igra sa vama\nDa li prihvatate novu igru?");

                            gameReq.setPositiveButton("Da", (dialog, which) -> {
                                String msg = "Answer to:" + oppName + ":yes";
                                parent.sendMessage(msg);
                                String gameParam = parent.getClientUsername() + ":red";
                                parent.startGame(gameParam);
                            });
                            gameReq.setNegativeButton("Ne", (dialog, which) -> {
                                String msg = "Answer to:" + oppName + ":no";
                                parent.sendMessage(msg);
                            });

                            gameReq.create().show();
                        });
                    }
                    else if (line.startsWith("Answer from:")) {
                        Log.d("MY_APP", "odgovor");
                        String[] oppAnsw = line.split(":");

                        if (oppAnsw.length >= 3 && oppAnsw[2].equals("yes")) {
                            parent.runOnUiThread(() -> {
                                String gameParam = parent.getClientUsername() + ":blue";
                                parent.startGame(gameParam);
                            });
                        }
                    }
                    // handles initiator start
                    else if (line.startsWith("Game start")) {
                        Log.d("MY_APP", "Server signaled game start!");
                        parent.runOnUiThread(() -> {
                            String gameParam = parent.getClientUsername() + ":blue";
                            parent.startGame(gameParam);
                        });
                    }

                    else if (line.startsWith("My move")) {
                        Log.d("MY_APP", "Potez: " + line);
                        if (gameAct != null) {
                            String[] msg = line.trim().split(":");
                            gameAct.runOnUiThread(() -> {
                                gameAct.placeDisc(msg[1], gameAct.color);
                            });
                        }
                    }
                    else if (line.startsWith("Opponent's move")) {
                        Log.d("MY_APP", "Potez protivnika: " + line);
                        if (gameAct != null) {
                            String[] move = line.split(":");
                            gameAct.runOnUiThread(() -> {
                                String oppColor = gameAct.color.equals("blue") ? "red" : "blue";
                                gameAct.placeDisc(move[1], oppColor);
                            });
                        }
                    }
                    else if (line.startsWith("Move rejected")) {
                        Log.d("MY_APP", "Ne moze se odigrati taj potez: " + line);
                        if (gameAct != null) {
                            gameAct.runOnUiThread(() ->
                                    gameAct.showInfoDialog("Obavestenje", "Potez nije validan, pokusajte ponovo!")
                            );
                        }
                    }
                    else if (line.startsWith("Not your turn")) {
                        Log.d("MY_APP", "Nije na tebe red");
                        if (gameAct != null) {
                            gameAct.runOnUiThread(() ->
                                    gameAct.showInfoDialog("Obavestenje", " Sacekajte, protivnik je idalje na potezu!")
                            );
                        }
                    }
                    else if (line.startsWith("You won")) {
                        Log.d("MY_APP", "***Pobedio!***");
                        if (gameAct != null) {
                            gameAct.runOnUiThread(() -> {
                                gameAct.resetGame();
                                gameAct.btnCol.setEnabled(false);
                                gameAct.numberPicker.setEnabled(false);
                                AlertDialog.Builder gameReq = new AlertDialog.Builder(gameAct);
                                gameReq.setTitle("Kraj igre");
                                gameReq.setMessage("Cestitke na pobedi!!!\nZelite li da odigrate jos jednu partiju?");
                                gameReq.setPositiveButton("Da", (dialog, which) -> {
                                    String msg = "New game:yes";
                                    gameAct.sendMessage(msg);
                                });
                                gameReq.setNegativeButton("Ne", (dialog, which) -> {
                                    String msg = "New game:no";
                                    gameAct.sendMessage(msg);
                                });
                                gameReq.create().show();
                            });
                        }
                    }
                    else if (line.startsWith("You lost")) {
                        Log.d("MY_APP", "Izgubio od protivnika!");
                        if (gameAct != null) {
                            gameAct.runOnUiThread(() -> {
                                Toast.makeText(parent, "Izgubio", Toast.LENGTH_LONG).show();
                                gameAct.resetGame();
                                gameAct.btnCol.setEnabled(false);
                                gameAct.numberPicker.setEnabled(false);

                                AlertDialog.Builder gameReq = new AlertDialog.Builder(gameAct);
                                gameReq.setTitle("Kraj igre");
                                gameReq.setMessage("Izgubili ste, cestitamo!!!\nZelite li da odigrate jos jednu partiju?");
                                gameReq.setPositiveButton("Da", (dialog, which) -> {
                                    String msg = "New game:yes";
                                    gameAct.sendMessage(msg);
                                });
                                gameReq.setNegativeButton("Ne", (dialog, which) -> {
                                    String msg = "New game:no";
                                    gameAct.sendMessage(msg);
                                });
                                gameReq.create().show();
                            });
                        }
                    }
                    else if (line.startsWith("No new game"))  {
                        if (gameAct != null) {
                            gameAct.runOnUiThread(() -> {
                                Log.d("MY_APP", "**Gotova igrica**");
                                gameAct.finishGame();
                            });


                        }


                    }
                    else if (line.startsWith("Rematch"))  {
                        if (gameAct != null) {
                            gameAct.runOnUiThread(() -> {
                                Toast.makeText(gameAct, "Pocinje nova igra!", Toast.LENGTH_LONG).show();
                                if(gameAct.color.equals("blue"))
                                    gameAct.color = "red";
                                else if(gameAct.color.equals("red"))
                                    gameAct.color = "blue";

                                switch(gameAct.color) {
                                    case "blue":
                                        gameAct.playerColor.setImageResource(R.drawable.blue);
                                        break;
                                    case "red":
                                        gameAct.playerColor.setImageResource(R.drawable.red);
                                        break;
                                    default:
                                        gameAct.playerColor.setImageResource(R.drawable.blank);
                                }

                                gameAct.numberPicker.setEnabled(true);
                                gameAct.btnCol.setEnabled(true);
                            });
                        }
                    }
                    else {
                        Log.d("MY_APP", "Nepoznat format poruke");
                    }
                }
            } catch (IOException ex) {
                Log.e("MY_APP", "ReceiveMessFromServer-IOException");
            }
        }
    }
}
