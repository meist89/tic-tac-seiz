package com.tictactoe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame;
    private JButton[] buttons = new JButton[9];
    private boolean myTurn = false;
    private String playerSymbol;
    private String opponentSymbol;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToeClient().start());
    }

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            frame = new JFrame("Tic Tac Toe");
            frame.setLayout(new GridLayout(3, 3));
            frame.setSize(300, 300);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            for (int i = 0; i < 9; i++) {
                buttons[i] = new JButton("");
                buttons[i].setFont(new Font("Arial", Font.PLAIN, 60));
                buttons[i].setEnabled(false);
                int index = i;
                buttons[i].addActionListener(e -> makeMove(index));
                frame.add(buttons[i]);
            }

            frame.setVisible(true);
            new ServerListener().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeMove(int index) {
        if (myTurn && buttons[index].getText().equals("")) {
            out.println(index);
            myTurn = false;
        }
    }

    private class ServerListener extends Thread {
        @Override
        public void run() {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    if (response.equals("WAIT")) {
                        // Attendi il secondo giocatore
                    } else if (response.equals("READY")) {
                        myTurn = true;
                        playerSymbol = "1"; // O 2 dipende dalla posizione
                        opponentSymbol = playerSymbol.equals("1") ? "2" : "1";
                        for (JButton button : buttons) {
                            button.setEnabled(true);
                        }
                    } else {
                        processUpdate(response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processUpdate(String response) {
            String[] parts = response.split(",");
            for (int i = 0; i < 9; i++) {
                String symbol = parts[i];
                if (symbol.equals("1")) {
                    buttons[i].setText("X");
                } else if (symbol.equals("2")) {
                    buttons[i].setText("O");
                } else {
                    buttons[i].setText("");
                }
            }

            String result = parts[9];
            if (result.equals("L")) {
                JOptionPane.showMessageDialog(frame, "Hai perso!");
            } else if (result.equals("P")) {
                JOptionPane.showMessageDialog(frame, "Pareggio!");
            } else if (result.equals("W")) {
                JOptionPane.showMessageDialog(frame, "Hai vinto!");
            }
        }
    }
}
