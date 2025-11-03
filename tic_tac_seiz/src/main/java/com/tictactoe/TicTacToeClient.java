package com.tictactoe;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TicTacToeClient {
    private static final String SERVER_ADDRESS = "localhost"; // Indirizzo del server
    private static final int SERVER_PORT = 12345; // Porta del server
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static Scanner scanner;
    private static String playerSymbol = ""; // Simbolo del giocatore (X o O)
    private static boolean gameRunning = true;
    
    public static void main(String[] args) {
        try {
            // Connessione al server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            // Attendere la risposta iniziale dal server
            String serverResponse = in.readLine();
            if ("WAIT".equals(serverResponse)) {
                System.out.println("In attesa di un secondo giocatore...");
                serverResponse = in.readLine(); // Dopo che il secondo giocatore si è connesso
                if ("READY".equals(serverResponse)) {
                    System.out.println("Partita iniziata!");
                    // Giocatore 1 inizia sempre
                    playerSymbol = "X";
                    System.out.println("Sei il Giocatore 1 (X). Buona fortuna!");
                    startGame();
                }
            }

        } catch (IOException e) {
            System.out.println("Errore di connessione: " + e.getMessage());
        } finally {
            closeConnections();
        }
    }

    private static void startGame() {
        while (gameRunning) {
            try {
                // Mostra la griglia e il turno
                printGameBoard();
                System.out.println("Tocca a te, scegli una casella (0-8):");
                int move = scanner.nextInt();
                scanner.nextLine(); // Consuma la newline

                // Invia la mossa al server
                out.println(move);
                
                // Ricevi la risposta del server
                String response = in.readLine();
                handleServerResponse(response);

                // Se la partita è finita, chiudi la connessione
                if (!gameRunning) {
                    break;
                }
            } catch (IOException e) {
                System.out.println("Errore di comunicazione con il server: " + e.getMessage());
                gameRunning = false;
            }
        }
    }

    private static void printGameBoard() {
        System.out.println("\nGriglia di gioco:");
        String boardState = getBoardState();
        String[] cells = boardState.split(",");
        for (int i = 0; i < 9; i++) {
            String symbol = "";
            if (cells[i].equals("1")) symbol = "X";
            else if (cells[i].equals("2")) symbol = "O";
            System.out.print((symbol.isEmpty() ? "-" : symbol) + " ");
            if (i % 3 == 2) {
                System.out.println();
            }
        }
    }

    private static String getBoardState() {
        StringBuilder board = new StringBuilder();
        try {
            // Ricevi l'aggiornamento dalla parte server
            String update = in.readLine();
            if (update != null) {
                return update.split(",")[0]; // Ottieni solo le 9 celle della griglia
            }
        } catch (IOException e) {
            System.out.println("Errore nel ricevere l'aggiornamento della griglia: " + e.getMessage());
        }
        return "";
    }

    private static void handleServerResponse(String response) {
        switch (response) {
            case "OK":
                System.out.println("Mossa valida! Ora tocca all'avversario.");
                break;
            case "KO":
                System.out.println("Mossa non valida! La casella è già occupata o l'indice non è valido.");
                break;
            case "W":
                System.out.println("Hai vinto!");
                gameRunning = false;
                break;
            case "P":
                System.out.println("La partita è finita in pareggio!");
                gameRunning = false;
                break;
            default:
                System.out.println("Risposta del server non prevista: " + response);
                break;
        }
    }

    private static void closeConnections() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        } catch (IOException e) {
            System.out.println("Errore nella chiusura della connessione: " + e.getMessage());
        }
    }
}
