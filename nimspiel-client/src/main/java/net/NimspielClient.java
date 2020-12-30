package net;

import gui.fxcontroller.NimspielMatch;
import gui.fxcontroller.lobby.Lobby;
import javafx.application.Platform;

import java.util.concurrent.TimeoutException;

/**
 * The NP (Nimspiel Protocol):
 * Possible Client request:
 * <ul>
 * <li> login (username)
 *    --&gt; signs the user up / assigns the username to its IP and port </li>
 * <li> join (gameid)
 *    --&gt; joins a game with the UUID (gameid) </li>
 * <li> leave
 *    --&gt; leaves the current game
 * <li> create (gamename)
 *    --&gt; creates a new game called (gamename) </li>
 * <li> pick (amount)
 *    --&gt; picks a certain amount of sticks in a game </li>
 * </ul>
 * Possible Server answers/notifications:
 * <ul>
 * <li> gamelobbies (gamename 1) (gameid 1) (creator 1) ... (gamename n) (gameid n) (creator n)
 *    --&gt; gives a list of open games, is sent after a user's 'login' request </li>
 * <li> ok | error
 *    --&gt; response to a 'leave' or 'pick' request
 * <li> ok (start) | error
 *    --&gt; response to a 'join' request, (start) is either 0 (opponent starts) or 1 (you start) </li>
 * <li> ok (gameid)
 *    --&gt; response to a 'create' request, (gameid) is an UUID </li>
 * <li> addGame (gamename) (gameid) (creator)
 *    --&gt; when a user created a new game to display in the lobby </li>
 * <li> delGame (gameid)
 *    --&gt; when a game is full or everyone left, so it doesn't have to be listed any longer </li>
 * <li> updatefield (amount)
 *    --&gt; when the opponent picked sticks </li>
 * <li> playerjoinedmatch (opponent) (start)
 *    --&gt; when (opponent) joined your game, (start) is either 0 (opponent starts) or 1 (you start) </li>
 * </ul>
 */
public class NimspielClient extends Client {

    protected String serverResponse = null;

    protected NimspielMatch currentGame;
    protected Lobby lobbyObject;

    public NimspielClient(String pServerIP, int pServerPort) { super(pServerIP, pServerPort); }

    public void setCurrentGame(NimspielMatch game) { this.currentGame = game; }
    public void setLobbyObject(Lobby lobbyObject) { this.lobbyObject = lobbyObject; }

    @Override
    public synchronized void processMessage(String pMessage) {

        System.out.println("[CLIENT] Received " + pMessage);
        switch(pMessage.split(" ", 2)[0]) {

            case "updatefield": if(currentGame != null) Platform.runLater(() -> currentGame.opponentPicked(Integer.parseInt(pMessage.split(" ", 2)[1]))); break;
            case "opponentjoined": if(currentGame != null) Platform.runLater(() -> currentGame.opponentJoined(pMessage.split(" ", 3)[1], Byte.parseByte(pMessage.split(" ", 3)[2]))); break;
            case "opponentleft": if(currentGame != null) Platform.runLater(() -> currentGame.opponentLeft()); break;
            case "addgame": if(lobbyObject != null) Platform.runLater(() -> lobbyObject.addGameEntry(pMessage.split(" ", 4)[1], pMessage.split(" ", 4)[2], pMessage.split(" ", 4)[3])); break;
            case "delgame": if(lobbyObject != null) Platform.runLater(() -> lobbyObject.removeGameEntry(pMessage.split(" ", 2)[1])); break;
            case "error": new UnknownError(pMessage).printStackTrace();
            default: serverResponse = pMessage; notify();
        }
    }

    public synchronized String signup(String username) {

        serverResponse = null;

        send("login " + username);

        try {
            wait(5000);
        } catch(InterruptedException ignored) {}

        if(serverResponse == null)
            new TimeoutException("Couldn't sign up: Timeout").printStackTrace();

        String returnString = serverResponse;
        serverResponse = null;
        return returnString;

    }

    public synchronized String join(String gameID) {

        serverResponse = null;

        send("join " + gameID);

        try {
            wait(5000);
        } catch(InterruptedException ignored) {}

        if(serverResponse == null)
            new TimeoutException("Couldn't join game " + gameID + ": Timeout").printStackTrace();

        String returnString = serverResponse;
        serverResponse = null;
        return returnString;

    }

    public synchronized String create(String game) {

        serverResponse = null;

        send ("create " + game);

        try {
            wait(5000);
        } catch(InterruptedException ignored) {}

        if(serverResponse == null)
            new TimeoutException("Couldn't create game " + game + ": Timeout").printStackTrace();

        String returnString = serverResponse;
        serverResponse = null;
        return returnString;

    }

    public synchronized String pick(int field) {

        serverResponse = null;

        this.send("pick " + field);

        try {
            wait(5000);
        } catch(InterruptedException ignored) {}

        if(serverResponse == null) {
            new TimeoutException("Couldn't pick field " + field + ": Timeout").printStackTrace();
            return "";
        }

        String returnString = serverResponse;
        serverResponse = null;
        return returnString;

    }

    public synchronized void leave () { send("leave"); }

}
