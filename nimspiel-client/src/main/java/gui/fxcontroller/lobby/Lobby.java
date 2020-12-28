package gui.fxcontroller.lobby;

import gui.fxcontroller.NimspielMatch;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.NimspielClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * The controller class for /res/lobby/lobby.fxml.
 * Gets created and loaded by the {@link gui.fxcontroller.Signup} prompt.
 *
 * The lobby is used to find games or create new ones.
 * It consists of two parts: The game list (listing all available games, see /res/fxml/lobby/gameentry.fxml) and a prompt to create a new list.
 *
 * This object stays loaded when matches are running so that the game list stay updated.
 *
 * @author Kilian Friedrich
 */
public class Lobby {

    /**
     * Represents connection to the Nimspiel host server.
     * Requests will be send via this object, the object itself will send request using the NP (see {@link NimspielClient}).
     *
     * @see NimspielClient - describes the NP (Nimspiel Protocol)
     */
    protected NimspielClient serverConnection;

    /**
     * The window in which the Nimspiel game takes place.
     * The NimspielMatch scene will be loaded in that object.
     * You could get this over a FXML-assigned variable but it's more beautiful this way.
     *
     * @see #joinNewGame() - loading a new game into the window
     * @see Game#join() - loading a {@link Game} into the window
     */
    protected Stage primaryStage;

    /**
     * The server response from the login, starting with "gamelobbies ".
     * It represents a list of available games.
     *
     * The String is passed in the constructor but can't be initialized until all FXML fields are assigned.
     * Hence, it needs to be stored.
     *
     * The list may change over time, when games are {@link #addGameEntry(String, String, String) added} or {@link #removeGameEntry(String) removed}.
     *
     * @see #Lobby(Stage, NimspielClient, String) - declares this String
     * @see #initialize() - uses this String to build a list of available games
     */
    private String startGames;

    /**
     * Stores the list of available games.
     * This variable is changed on {@link #initialize() initialization} or when games are {@link #addGameEntry(String, String, String) added} or {@link #removeGameEntry(String) removed}.
     * This is needed to be able to remove a game when {@link #removeGameEntry(String)} is called.
     *
     * @see #startGames - the list of available games at the start
     * @see #addGameEntry(String, String, String) - adds new games to this list
     * @see #removeGameEntry(String) - removes games from this list
     */
    private Set<Game> games = new HashSet<>();

    /**
     * This pane (see /res/fxml/lobby/nogames.fxml) is displayed when no game is available.
     * It simply says "No games available. You can create one below.".
     *
     * @see #initialize() - loads the resource /res/fxml/lobby/nogames.fxml into this variable
     */
    private AnchorPane noGamePane;

    /**
     * The field in which the user can type the name of a game which is created.
     * Gets assigned by the {@link javafx.fxml.FXMLLoader} when the lobby scene gets loaded by a {@link gui.fxcontroller.Signup} prompt.
     *
     * @see #joinNewGame() - creates a new game
     */
    @FXML public TextField gameNameField;

    /**
     * The text field in which error messages are displayed.
     * Gets assigned by the {@link javafx.fxml.FXMLLoader} when the lobby scene gets loaded by a {@link gui.fxcontroller.Signup} prompt.
     *
     * @see #updateErrorMessage(String) - updates this field's content
     */
    @FXML public Text errField;

    /**
     * The UI list of available games.
     * Holds a lot of {@link Game game entries} as stored in {@link #games}.
     * Gets assigned by the {@link javafx.fxml.FXMLLoader} when the lobby scene gets loaded by a {@link gui.fxcontroller.Signup} prompt.
     *
     * @see #games - the list of available games
     */
    @FXML public VBox gameContainer;

    /**
     * Default constructor.
     * Stores assigned parameters in local variables.
     *
     * @param primaryStage - the window in which the Nimspiel game takes place
     * @param serverConnection - represents the connection to the Nimspiel host server
     * @param startGames - a list of available games at lobby initialization (in String format)
     *
     * @see #primaryStage - the window in which the Nimspiel game takes place
     * @see #serverConnection - represents the connection to the Nimspiel host server
     * @see #startGames - a list of available games at lobby initialization
     */
    public Lobby(Stage primaryStage, NimspielClient serverConnection, String startGames) {

        this.primaryStage = primaryStage;
        this.serverConnection = serverConnection;
        this.startGames = startGames;

        // register object so that it can receive server requests to add / remove games
        serverConnection.setLobbyObject(this);

    } // END constructor Lobby(String, NimspielClient, String)

    /**
     * Gets called when the variables {@link #gameNameField} and {@link #gameContainer} are set.
     * Loads the {@link #noGamePane} and adds it, then adds all other available games (if any).
     * Adds a text listener to the {@link #gameNameField} to
     *
     * @see #noGamePane - the message to display if no game is available ("No games available. You can create one below.")
     * @see #startGames - a list of available games at lobby initialization (in String format)
     */
    @FXML public void initialize() {

        try {
            noGamePane = FXMLLoader.load(this.getClass().getResource("/fxml/lobby/nogames.fxml"));
        } catch(IOException e) {
            e.printStackTrace();
        }

        // the addGameEntry will remove this pane when there are games (see following loop)
        gameContainer.getChildren().add(noGamePane);

        if(!startGames.equals("gamelobbies")) {  // if not empty

            // startGames looks like: gamelobbies (gamename 1) (gameid 1) (creator 1) ... (gamename n) (gameid n) (cretor n)
            String[] games = startGames.substring(12).split(" ");  // remove "gamelobbies " prefix
            for (int i = 0; i < games.length / 3; i++)
                addGameEntry(games[i * 3], games[i * 3 + 1], games[i * 3 + 2]);

        }

        // ignores the observed TextField (already stored in usernameField) and old value (not needed) on updateErrorMessage(String) call
        gameNameField.textProperty().addListener((ignored1, ignored2, newValue) -> updateErrorMessage(newValue));

    } // END function initialize()

    /**
     * Gets called on gameNameFields update (see res/fxml/lobby/lobby.fxml) or when "Create" is pressed.
     * Updates the error message in fx:errField (see res/fxml/lobby/lobby.fxml).
     *
     * @param value the new value of the name field
     * @return true when the name is invalid / false when the name is valid
     *
     * @see TextField#setText(String) - sets the text of a text field
     * @see #errField - the text field in which the error message is written
     */
    private boolean updateErrorMessage(String value) {

        if(value.isEmpty())
            errField.setText("Enter a game name.");
        else {
            errField.setText("");
            return false;  // false = no error
        }

        return true;  // true = error

    } // END function updateErrorMessage(String)

    /**
     * Creates a new {@link NimspielMatch game} and loads it into the {@link #primaryStage}.
     * It uses an FXMLLoader to load /res/fxml/match.fxml as the game UI.
     * Gets called by {@link #gameNameField}'s onAction event and by a click on the 'Create' button (see /res/fxml/lobby/lobby.fxml).
     *
     * @see #serverConnection - used to send a request to create the new game.
     */
    @FXML public void joinNewGame() {

        if(updateErrorMessage(gameNameField.getText()))
            return;

        String serverResponse = serverConnection.create(gameNameField.getText());

        if(serverResponse != null && serverResponse.startsWith("ok")) {

            try {

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/match.fxml"));
                fxmlLoader.setController(new NimspielMatch(this.getScene(), serverConnection, serverResponse.split(" ", 2)[1], null, (byte) 0));

                primaryStage.setScene(new Scene(fxmlLoader.load()));

            } catch(IOException e) {
                e.printStackTrace();
                primaryStage.close();
            }

        } // END if(serverResponse != null && ...)

    } // END function joinNewGame()

    /**
     * Gets called when a new game is created.
     * Adds the new game to the {@link #games list} and puts a UI element in {@link #gameContainer}.
     *
     * @param gameName - the name of the new game
     * @param id - the UUID of the new game (in String format)
     * @param creatorName - the username of the game's creator
     *
     * @see #games - all available games
     * @see #gameContainer - the container UI element which contains one UI element per game
     */
    public void addGameEntry(String gameName, String id, String creatorName) {

        try {

            Game game = new Game(this, serverConnection, gameName, creatorName, id);
            games.add(game);

            FXMLLoader fxmlLoader = new FXMLLoader(Lobby.class.getResource("/fxml/lobby/gameentry.fxml"));
            fxmlLoader.setController(game);

            gameContainer.getChildren().remove(noGamePane);
            gameContainer.getChildren().add(fxmlLoader.load());

        } catch(IOException e) {
            e.printStackTrace();
            primaryStage.close();
        }

    } // END function addGameEntry(String, String, String)

    /**
     * Removes a game from the {@link #games list} and its UI element.
     * May add the {@link #noGamePane} to the {@link #gameContainer}.
     *
     * @param id - the UUID of the game (in String format)
     *
     * @see #gameContainer - the container UI element which contains one UI element per game
     * @see #noGamePane - the message to display if no game is available ("No games available. You can create one below.")
     */
    public void removeGameEntry(String id) {

        for(Game game : games)  // loop through games to find game with UUID id
            if(game.getGameID().equals(id)) {

                games.remove(game);
                gameContainer.getChildren().remove(game.getParent());
                break;

            }

        // add noGamePane if necessary
        if(games.size() == 0)
            gameContainer.getChildren().add(noGamePane);

    } // END function removeGameEntry()

    /**
     * Return the {@link Scene} of the lobby.
     * This is used by a {@link NimspielMatch} to leave and load the lobby scene back into the window.
     *
     * @return the lobby scene
     */
    public Scene getScene() {

        return gameContainer.getScene();

    } // END function getScene()

} // END class Lobby
