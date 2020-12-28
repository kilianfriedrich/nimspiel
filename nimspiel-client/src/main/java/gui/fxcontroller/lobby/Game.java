package gui.fxcontroller.lobby;

import gui.fxcontroller.NimspielMatch;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.NimspielClient;

import java.io.IOException;

public class Game {

    protected NimspielClient serverConnection;
    protected String gameID, gameName, creatorName;

    Game(Lobby lobbyObject, NimspielClient serverConnection, String gameName, String creatorName, String gameID) {

        this.serverConnection = serverConnection;

        this.lobbyObject = lobbyObject;
        this.gameID = gameID;
        this.gameName = gameName;
        this.creatorName = creatorName;

    }

    private Lobby lobbyObject;

    @FXML public Text gameNameField;
    @FXML public Text creatorNameField;
    @FXML public AnchorPane parent;

    @FXML public void initialize() {

        gameNameField.setText(gameName);
        creatorNameField.setText("by " + creatorName);

        Tooltip.install(parent, new Tooltip("Game-ID: " + gameID));

    }

    @FXML public void join() {

        String serverResponse = serverConnection.join(gameID);

        if(serverResponse.startsWith("ok")) {

            try {

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/match.fxml"));
                fxmlLoader.setController(new NimspielMatch(lobbyObject.getScene(), serverConnection, gameID, creatorName, Byte.parseByte(serverResponse.split(" ", 2)[1])));

                ((Stage) parent.getScene().getWindow()).setScene(new Scene(fxmlLoader.load()));

            } catch(IOException e) {
                e.printStackTrace();
                ((Stage) parent.getScene().getWindow()).close();
            }

        }

    }
    String getGameID() { return gameID; }
    AnchorPane getParent() { return parent; }

}
