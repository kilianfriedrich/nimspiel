package gui.fxcontroller;

import javafx.application.Platform;
import javafx.scene.Scene;

public class NimspielSingleMatch extends NimspielMatch {

    public NimspielSingleMatch(Scene lobbyScene, byte first) {
        super(lobbyScene, null, "local", "Computer", first);
    }

    @Override
    protected synchronized void processClick(int weggenommen) {
        if(yourTurn == 1) {
            intLeftSticks -= weggenommen;
            leftSticks.setText(String.valueOf(intLeftSticks));
            if(intLeftSticks <= 0) {
                youScoreByte++;
                first = (byte) -first;
                reset();
            } else {
                yourTurn = -1;
                turnField.setText("Opponent's turn");
                Platform.runLater(this::computerLogic);
            }
        }
    }

    @Override
    protected synchronized void reset() {
        super.reset();
        if (yourTurn == -1) {

            Platform.runLater(this::computerLogic);
        }
    }

    private void computerLogic() {
        try {
            Thread.sleep((long) (Math.random() * 500 + 500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int pick = intLeftSticks % 4;
        if (pick == 0 || Math.random() > 0.9) { //
            pick = (int) (Math.random() * 3 + 1);
        }
        opponentPicked(pick);
    }
}
