package gui.fxcontroller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.NimspielClient;

public class NimspielMatch {

    public Text turnField;
    protected int intLeftSticks = 20;
    protected NimspielClient serverConnection;
    protected String opponent, gameID;
    protected byte first;
    protected byte yourTurn;
    protected Scene lobbyScene;

@FXML protected Label leftSticks;

    public void opponentJoined(String opponentName, byte first) {

        this.first = first;
        
        this.opponent = opponentName;

        reset();

    }

    public void opponentPicked(int weggenommen) {

        intLeftSticks -= weggenommen;
        leftSticks.setText(String.valueOf(intLeftSticks));

        if(intLeftSticks <= 0) {

            opponentScoreByte++;
            first = (byte) -first;
            reset();

        } else {
            yourTurn = 1;
            turnField.setText("It's your turn!");
        }
    }

    public void opponentLeft() {
        this.yourTurn = 0;
        this.opponent = null;
        initialize();
    }

    public NimspielMatch(Scene lobbyScene, NimspielClient serverConnection, String gameID, String opponent, byte first) {

        this.serverConnection = serverConnection;
        this.lobbyScene = lobbyScene;
        this.gameID = gameID;
        this.opponent = opponent;
        this.yourTurn = first;
        this.first = first;

        if (serverConnection != null) {
            serverConnection.setCurrentGame(this);
        }
    }

    @FXML public Text opponentField;
    @FXML public Text gameidField;

    @FXML public Label youScore;
    protected byte youScoreByte = 0, opponentScoreByte = 0;
    @FXML public Label opponentScore;

    @FXML public void initialize() {

        if(opponent != null)
            opponentField.setText("Playing against " + opponent);
        else
            opponentField.setText("Waiting for opponent...");

        gameidField.setText("Game-ID: " + gameID);

        youScore.setText(Byte.toString(youScoreByte));
        opponentScore.setText(Byte.toString(opponentScoreByte));

        if(yourTurn == 1)
            turnField.setText("Your Turn!");
        else if(yourTurn == -1)
            turnField.setText("Opponent's turn!");
        else
            turnField.setText("");

    }

    @FXML
    public void wegnehm1() { processClick(1);}

    @FXML
    public void wegnehm2() { processClick(2);}

    @FXML
    public void wegnehm3() { processClick(3);}

    protected synchronized void processClick(int weggenommen) {

        if(yourTurn == 1) {

            String response = serverConnection.pick(weggenommen);

            if(response.startsWith("ok")) {

                intLeftSticks -= weggenommen;
                leftSticks.setText(String.valueOf(intLeftSticks));

            }

            if(intLeftSticks <= 0) {
                youScoreByte++;
                first = (byte) -first;
                reset();
            } else {
                yourTurn = -1;
                turnField.setText("Opponent's turn");
            }

        }

    }
    
    protected synchronized void reset() {

        this.yourTurn = this.first;
        
        initialize();

        intLeftSticks = 20;
        leftSticks.setText(String.valueOf(intLeftSticks));

        
    }

    @FXML public void leave() {
        if (serverConnection != null) {
            serverConnection.leave();
        }

        ((Stage) gameidField.getScene().getWindow()).setScene(lobbyScene);

    }

}
