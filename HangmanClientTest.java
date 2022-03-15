import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class HangmanClientTest {
    //All global Variables are declared:
    static String players;
    static String toFind;
    static String[] choose = new String[200];
    static String choosing = "";
    static String multiChoose = "";
    static String fullWord = "";
    static char in;
    static char[] used = new char[26];
    static char[] toFindChars;
    static char[] alreadyFound;
    static int mode = 0;
    static int multiMode = 0;
    static int counter = 0;
    static int mistakes = 0;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);
    static boolean selectedCorrect = false;
    static boolean isOver = false;
    static boolean newRound = true;
    static boolean yourTurn = false;


    public static void main(String[] args) {
        //Which Mode?
        getMode();
        //If SinglePlayer:
        if (mode == 1) {
           singleRunning();

        } else {
            //If Multiplayer, select if Server or Client-Side
            getMultiMode();
            if (multiMode==1){
                try {
                    runServer();
                } catch (SocketException e){
                    System.out.println("Connection abort... beende das Programm...");
                    scanner.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    runClient();
                } catch (SocketException e){
                    System.out.println("Connection abort... beende das Programm...");
                    scanner.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }



    //Select if Single- or Multiplayer
    public static int getMode() {
        System.out.println("------======STARTING======------");
        System.out.println("------====== RUNNING======------");
        //While for correct User-Input
        while (!selectedCorrect) {
            System.out.println("Möchtest du Singleplayer oder Multiplayer spielen?");
            players = scanner.nextLine();
            switch (players) {
                case "Singleplayer", "singleplayer", "single", "Single", "s", "S" -> {
                    System.out.println("Singleplayermodus wird geladen!");
                    mode = 1;
                    selectedCorrect = true;
                }
                case "Multiplayer", "multiplayer", "Multi", "multi", "M", "m" -> {
                    System.out.println("Multiplayermodus wird geladen!");
                    selectedCorrect = true;
                    mode = 2;
                }
                default -> {
                    System.out.println("Bitte nochmal versuchen!");
                    selectedCorrect = false;
                }
            }
        }
        //Return which mode User wants
        return mode;
    }

    //Select if playing as Server or as Socket
    public static int getMultiMode(){
        selectedCorrect=false;
        System.out.println("Möchtest du als Server(S) oder Client(C) starten?");
        while (!selectedCorrect){
            multiChoose = scanner.next();
            switch (multiChoose) {
                case "Server", "server", "S", "s" -> {
                    System.out.println("Server wird gestartet...");
                    selectedCorrect = true;
                    multiMode = 1;
                }
                case "Client", "client", "C", "c" -> {
                    System.out.println("Client wird gestartet...");
                    selectedCorrect = true;
                    multiMode = 2;
                }
                default -> {
                    System.out.println("Nicht richtig eingegeben! Bitte erneut wählen!");
                    selectedCorrect = false;
                }
            }
        }
        return multiMode;
    }

    //Select Word and get ready for the game
    public static void gameStarter() {
        toFind = wordToFind();
        toFindChars = new char[toFind.length()];
        alreadyFound = new char[toFind.length()];
        fillArray(toFindChars, toFind);
        fillEmpty(alreadyFound, used);
    }

    public static void gameStarterClient(String a) {
        toFind = a;
        toFindChars = new char[toFind.length()];
        alreadyFound = new char[toFind.length()];
        fillArray(toFindChars, toFind);
        fillEmpty(alreadyFound, used);
    }

    //Running singlePlayer Game
    public static void singleRunning() {
        while (newRound) {
            gameStarter();
            counter = 0;
            yourTurn = false;
            while (true) {
                paintComponents();
                in = getUserInput();
                used[counter] = in;
                counter++;
                checkThrough(toFindChars, alreadyFound);
                isOver = winCondition(toFindChars, alreadyFound, yourTurn);
                if (isOver) {
                    System.out.println("Runde beendet!");
                    break;
                }
            }
            startNewRound();
        }
    }

    //Read the Word from the File and Randomize it
    public static String wordToFind() {
        try {
            Scanner s = new Scanner(new File("src/text.txt"));
            int count = 0;
            while (s.hasNext()) {
                //Change input to Upper-Case only
                choose[count] = s.next().toUpperCase();
                count++;
            }
        } catch (IOException e) {
            System.out.println("Error accessing input file!");
        }
        //Choose a random word from the list
        toFind = choose[random.nextInt(choose.length)];
        toFind = toFind.toUpperCase();
        return toFind;
    }

    //Fill array with random word
    public static char[] fillArray(char[] a, String c) {
        String d = c.toUpperCase();
        for (int i = 0; i < a.length; i++) {
            a[i] = d.charAt(i);
        }
        return a;
    }

    //empty array for used chars and the word to find
    public static char[] fillEmpty(char[] a, char[]b) {
        Arrays.fill(a, '_');
        Arrays.fill(b, '0');
        return a;
    }

    //get the char from the user
    public static char getUserInput() {
        boolean selectedOnce = false;
        System.out.println("Bitte versuche einen Buchstaben: (du kannst auch das ganze Wort eingeben!) ");
        char b = 0;
        while (!selectedOnce) {
            String a = scanner.next();
            if (a.equalsIgnoreCase(toFind)){
                System.out.println("Du hast das Wort erraten!");
                for (int i = 0; i < toFindChars.length; i++) {
                    alreadyFound[i] = toFindChars[i];
                }
                break;
            }
            a = a.toUpperCase();
            b = a.charAt(0);
            for (char c : used) {
                if (c == b) {
                    selectedOnce = false;
                    System.out.println("Dieser Buchstabe wurde bereits verwendet, bitte erneut versuchen!");
                    break;
                } else {
                    selectedOnce = true;
                }
            }
        }
        return b;
    }

    //Other Input-Chance for Multiplayer (only the Char is returned)
    public static char getMultiInput() {
        boolean selectedOnce = false;
        System.out.println("Bitte versuche einen Buchstaben: ");
        char b = 0;
        while (!selectedOnce) {
            String a = scanner.next();
            a = a.toUpperCase();
            b = a.charAt(0);
            for (char c : used) {
                if (c == b) {
                    selectedOnce = false;
                    System.out.println("Dieser Buchstabe wurde bereits verwendet, bitte erneut versuchen!");
                    break;
                } else {
                    selectedOnce = true;
                }
            }
        }
        return b;
    }

    //Return the Whole
    public static String getMultiWord() {
        System.out.println("Bitte versuche das Wort zu erraten!");
        String a = scanner.next();
        return a;
    }

    //check through the arrays if found a char
    public static char[] checkThrough(char[] a, char[] b) {
        boolean found = false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] == in) {
                b[i] = a[i];
                found = true;
            }
        }
        if (!found) {
            mistakes++;
        }
        return a;
    }

    //check through for the word - don't count up the Fails
    public static char[] checkThroughTwo(char[] a, char[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == in) {
                b[i] = a[i];
            }
        }
        return a;
    }

    //printing the Hangman
    public static void paintComponents() {
        System.out.println("-----------");
        System.out.println("     |");

        if (mistakes >= 1) {
            System.out.println("     O");
        }
        if (mistakes >= 2) {
            System.out.print("    \\");
            if (mistakes >= 3) {
                System.out.println("|/");
            }
        }
        if (mistakes >= 4) {
            System.out.println("     |");
        }
        if (mistakes >= 5) {
            System.out.print("    /");
            if (mistakes >= 6) {
                System.out.println(" \\");
            }
        }
        System.out.println("Bereits versucht:");
        for (int i = 0; i < used.length; i++) {
            if (used[i] != '0'){
                System.out.print(used[i] + " ");
            }
        }
        paintWord();
    }
    //Print out the Word (used in different Points)
    public static void paintWord(){
        System.out.println();
        System.out.println("Du hast noch " + (6-mistakes) + " Fehler übrig um folgendes zu erraten:");
        for (char c : alreadyFound) {
            System.out.print(c + " ");
        }
        System.out.println();
    }

    //check if won or lost
    public static boolean winCondition(char[] a, char[] b, boolean c) {
        boolean roundOver = false;
        if (Arrays.equals(a, b)){
            if (!c) {
                System.out.println("DU hast gewonnen! Gratuliere!");
            } else {
                System.out.println("Der andere Spieler hat das Wort erraten!");
            }
            roundOver = true;
        }
        if (mistakes == 6){
            System.out.println("Du hast leider keine Versuche mehr Verfügbar");
            System.out.println("Das gesuchte Wort wäre: " + toFind + " gewesen!");
            roundOver = true;
        }
        return roundOver;
    }

    //check if user wants a new Round or end the game
    public static boolean startNewRound() {
        selectedCorrect=false;
        while (!selectedCorrect) {
            System.out.println("Möchtest du eine neue Runde starten? Wähle ja oder nein!");
            choosing = scanner.next();
            switch (choosing) {
                case "ja", "Ja", "JA", "j", "J" -> {
                    System.out.println("Starte neue Runde...");
                    newRound = true;
                    selectedCorrect = true;
                    mistakes = 0;
                    return true;
                }
                case "nein", "Nein", "NEIN", "N", "n" -> {
                    System.out.println("Spiel wird beendet! Danke für's spielen!");
                    System.out.println("Bis zum nächsten mal!");
                    newRound = false;
                    selectedCorrect = true;
                    scanner.close();
                    return false;
                }
                default -> System.out.println("Bitte Ja(J) oder Nein (N) wählen!");
            }
        }
        return newRound;
    }

    //Main-Method for the Server
    public static void runServer() throws IOException {
        //Prints for nice Startup and initialize Server, Socket and Streams
        System.out.println("Server wird gestartet..");
        ServerSocket server = new ServerSocket(7777);
        System.out.println("Server wartet auf Client");
        Socket socket = server.accept();
        DataInputStream inStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Client verbunden");
        counter=0;

        while (true){
            while (newRound) {
                gameStarter();
                //Send the Word to the Client
                outStream.writeUTF(toFind);
                outStream.flush();
                while (true) {
                    paintComponents();
                    //If my Turn, get the Userinputs, send the char and the word to the Client
                    if (counter%2 == 0){
                        yourTurn = false;
                        in = getMultiInput();
                        //check and print only the word (without mistakes) before the chance to guess the full word
                        checkThroughTwo(toFindChars, alreadyFound);
                        paintWord();
                        fullWord = getMultiWord();
                        if (fullWord.equalsIgnoreCase(toFind)){
                            for (int i = 0; i < toFindChars.length; i++) {
                                alreadyFound[i] = toFindChars[i];
                            }
                        }
                        outStream.writeChar(in);
                        outStream.flush();
                        outStream.writeUTF(fullWord);
                    } else {
                        //If the Turn of the Client, get the Char and the word from the Client
                        yourTurn = true;
                        System.out.println("Der andere Spieler gibt seinen Tipp ab:");
                        in = inStream.readChar();
                        fullWord = inStream.readUTF();
                        //Print full given word of client and check if correct
                        System.out.println("Der andere Spieler hat versucht: " + fullWord);
                        if (fullWord.equalsIgnoreCase(toFind)){
                            for (int i = 0; i < toFindChars.length; i++) {
                                alreadyFound[i] = toFindChars[i];
                            }
                        }
                    }
                    //Save the used chars and get the counter up
                    used[counter] = in;
                    counter++;
                    //Check if char is in the searched word
                    checkThrough(toFindChars, alreadyFound);
                    //Check if User/Client won or if no more mistakes are available
                    isOver = winCondition(toFindChars, alreadyFound, yourTurn);
                    if(isOver){
                        System.out.println("Runde beendet!");
                        break;
                    }

                }
                startNewRound();

            }

        }

    }

    //Main-Method for the Client
    public static void runClient() throws IOException {
        //Print start-messages and get connection to the Server and set up Streams
        System.out.println("Client wird gestartet!");
        Socket socket = new Socket("localhost", 7777);
        DataInputStream inStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("Verbindung hergestellt");
        counter=0;
        while (true){
            while (newRound) {
                //Set mistakes back to 0 (especially for new Rounds)
                mistakes = 0;
                //get the Word to find from the Server
                toFind = inStream.readUTF();
                //Set up the Arrays and so on
                gameStarterClient(toFind);
                while (true) {
                    //Print out the Hangman and the Word
                    paintComponents();

                    //If my turn, get the inputs, check them and send word and char to the Server
                    if (counter%2 != 0){
                        yourTurn = false;
                        in = getMultiInput();
                        checkThroughTwo(toFindChars, alreadyFound);
                        paintWord();
                        fullWord = getMultiWord();
                        //If the given full word is right, save it in the array
                        if (fullWord.equalsIgnoreCase(toFind)){
                            for (int i = 0; i < toFindChars.length; i++) {
                                alreadyFound[i] = toFindChars[i];
                            }
                        }
                        outStream.writeChar(in);
                        outStream.flush();
                        outStream.writeUTF(fullWord);
                    } else {
                        //If turn of the server, get the char and the full word
                        yourTurn = true;
                        System.out.println("Der andere Spieler gibt seinen Tipp ab:");
                        in = inStream.readChar();
                        fullWord = inStream.readUTF();
                        //Print the full word that was given and check it, if correct set the arrays equal
                        System.out.println("Der andere Spieler hat versucht: " + fullWord);
                        if (fullWord.equalsIgnoreCase(toFind)){
                            for (int i = 0; i < toFindChars.length; i++) {
                                alreadyFound[i] = toFindChars[i];
                            }
                        }
                    }
                    //save the used chars
                    used[counter] = in;
                    counter++;
                    //Check if the given char is in the word
                    checkThrough(toFindChars, alreadyFound);
                    //Check win and loose conditions
                    isOver = winCondition(toFindChars, alreadyFound, yourTurn);
                    if (isOver) {
                        System.out.println("Runde beendet!");
                        break;
                    }
                }

            }
        }


    }
}
