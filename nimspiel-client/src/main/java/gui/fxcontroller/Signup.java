package gui.fxcontroller;

import gui.fxcontroller.lobby.Lobby;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import gui.FXApplication;
import net.NimspielClient;

import java.io.IOException;

/**
 * The controller class for res/fxml/signup.fxml, the sign-up prompt for Nimspiel.
 * Adds functionality like sign-up or error messages to the fxml.
 * Gets called by {@link FXApplication#start(Stage)}.
 * 
 * @author Kilian Friedrich
 */
public class Signup {

    /**
     * The text field in which the user types his username
     * Gets assigned by the {@link javafx.fxml.FXMLLoader} when the signup prompt gets loaded by {@link FXApplication#start(Stage)}
     * 
     * @see #updateErrorMessage(String, String, String) - prints error messages on wrong input (shown in {@link #errField})
     */
    @FXML public TextField usernameField;

    /**
     * The text field in which error messages are displayed.
     * Gets assigned by the {@link javafx.fxml.FXMLLoader} when the signup prompt gets loaded by {@link FXApplication#start(Stage)}
     * 
     * @see #updateErrorMessage(String, String, String) - updates this field's content
     */
    @FXML public Text errField;
    
    @FXML public TextField serverIPField;
    
    @FXML public TextField serverPortField;

    /**
     * The window in which the Nimspiel game takes place.
     * The lobby scene will be loaded in that object.
     *
     * @see #signup() - loading the {@link Lobby} object into the window
     */
    protected Stage primaryStage;

    /**
     * Represents connection to the Nimspiel host server.
     * Requests will be send via this object, the object itself will send request using the NP (see {@link NimspielClient}).
     *
     * @see NimspielClient - describes the NP (Nimspiel Protocol)
     */
    protected NimspielClient serverConnection;

    /**
     * Default constructor.
     * Stores assigned parameters in local variables
     *
     * @param primaryStage - the window in which the Nimspiel game takes place
     *
     * @see #primaryStage - the window in which the Nimspiel game takes place
     */
    public Signup(Stage primaryStage) {

        this.primaryStage = primaryStage;

    } // END constructor Signup(String, NimspielClient)

    /**
     * Gets called when the variables {@link #usernameField} and {@link #errField} are set.
     * Adds a listener to the username text field which checks the validity of user names.
     * 
     * @see #usernameField - the field in which the user types its username
     */
    @FXML public void initialize() {
        
        // ignores the observed TextField (already stored in usernameField) and old value (not needed) on updateErrorMessage(String) call
        usernameField.textProperty().addListener((ignored1, ignored2, newValue) -> updateErrorMessage(newValue, "127.0.0.1", "80"));
        serverIPField.textProperty().addListener((ignored1, ignored2, newValue) -> updateErrorMessage("foo", newValue, "80"));
        serverPortField.textProperty().addListener((ignored1, ignored2, newValue) -> updateErrorMessage("foo", "127.0.0.1", newValue));

    } // END function initialize()

    /**
     * Gets called on usernameFields update (see res/fxml/signup.fxml) or when "Sign-Up" is pressed.
     * Updates the error message in fx:errField (see res/fxml/signup.fxml).
     *
     * @param username the new value of the username field
     * @param port the new value of the port field
     * @return true when the username is invalid / false when the username is valid
     *
     * @see TextField#setText(String) - sets the text of a text field
     * @see #errField - the text field in which the error message is written
     */
    private boolean updateErrorMessage(String username, String server, String port) {
        
        if(username.isEmpty())
            errField.setText("Enter a username.");
        else if(server.isEmpty())
            errField.setText("Enter a server.");
        else {
            try {

                errField.setText("");
                if(Integer.parseInt(port) > 0)
                    return false;  // fale = no error
                else
                    throw new RuntimeException();
                
            } catch(RuntimeException ignored) {
                errField.setText("Enter a valid port.");
                return true;  // true = error
            }
        }

        return true;  // true = error

    } // END function updateErrorMessage(String)

    /**
     * Gets called when "Sign-Up" (see res/fxml/signup.fxml) is pressed.
     * Signs the user up after checking the validity of his username (see {@link #updateErrorMessage(String, String, String) updateErrorMessage(value)})
     * and redirects him to the lobby (see res/fxml/lobby.fxml).
     *
     * @see #updateErrorMessage(String, String, String) - checks the validity of a username
     */
    @FXML public void signup() {

        if(!updateErrorMessage(usernameField.getText(), serverIPField.getText(), serverPortField.getText())) {

            try {
                this.serverConnection = new NimspielClient(serverIPField.getText(), Integer.parseInt(serverPortField.getText()));
            } catch(RuntimeException ignored) {}  // won't throw since checked in updateErrorMessage
            
            String serverResponse = serverConnection.signup(usernameField.getText());

            if(serverResponse != null && serverResponse.startsWith("gamelobbies")) {

                try {

                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/lobby/lobby.fxml"));
                    fxmlLoader.setController(new Lobby(primaryStage, serverConnection, serverResponse));

                    primaryStage.setScene(new Scene(fxmlLoader.load()));

                } catch (IOException e) {
                    e.printStackTrace();
                    primaryStage.close();
                }

            } // END if(serverResponse != null && ...)

        } // END if(!updateErrorMessage(...))

    } // END function signup()

    @FXML public void singlePlayer() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/match.fxml"));
            fxmlLoader.setController(new NimspielSingleMatch(primaryStage.getScene(),(byte) 1));

            primaryStage.setScene(new Scene(fxmlLoader.load()));

        } catch (IOException e) {
            e.printStackTrace();
            primaryStage.close();
        }
    }

} // END class Signup
