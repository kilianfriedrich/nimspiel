import java.util.UUID;
/**
 * Write a description of class Game here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Game
{
    
    Player guest;
    Player host;
    UUID gameID;
    String name;
    int leftSticks = 20;
    int active;
    int first, nextFirst; // -1: host - 1: guest
    /**
     * Constructor for objects of class Game
     */
    public Game(Player pHost, UUID pGameID, String pName)
    {
        host = pHost;
        gameID = pGameID;
        name = pName;
    }

    public void setGuest(Player pGuest)
        //Da der Guest erst im Nachhinein in das Spiel geht muss er später erst initialisiert werden.
    {
        guest = pGuest;
    }

    public void pickStarter()
        //Wählt zufällig einen der beiden Spieler aus, der den ersten Zug machen darf. Nachdem das Spielfeld geräumt wurde, sollte diese Methode aufgerufen werden.
    {
        if(nextFirst != 0) {
            first = nextFirst;
            nextFirst = - nextFirst;

            active = first;

        } else if (Math.random()<0.5 )
        {
            active = -1;
            first = -1;
            nextFirst = 1;
        } 
        else 
        {
            active = 1;
            first = 1;
            nextFirst = -1;
        }
    }

    public void reset()
        //Nachdem ein Spiel beendet wurde, aber noch kein Spieler das Spiel verlassen hat, kann hiermit das Feld für eine anfolgende Partie geräumt werden.
    {
        active = first;
        leftSticks = 20;
    }

    public void update(int pField)
        //Wählt eine bestimmte Anzahl an Sticks aus. Dann ist der andere Spieler dran
    {

        //Spielerzugwechsel
        active = - active;
        win();
    }

    public void win()
    {
        // Das sind alle Fälle in denen man gewinnen kann.
        if(leftSticks <= 0) {
            reset();
            pickStarter();
        }
    }
    
    public Player getGuest(){return guest;}
    public Player getHost(){return host;}
    public UUID getGameID(){return gameID;}
    public String getName(){return name;}
    public int getActive(){return active;}
    public boolean isActive(Player p) {

        return (active == -1 && p == host) || (active == 1 && p == guest);

    }

}
