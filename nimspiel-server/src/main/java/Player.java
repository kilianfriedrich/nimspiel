
/**
 * Write a description of class Player here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Player
{
    // instance variables - replace the example below with your own
    String IP;
    int port;

    Game active;
    String displayName;

    /**
     * Constructor for objects of class Player
     */
    public Player(String pIP, int pPlayerPort, Game pActiveGame, String pName)
    {
        IP = pIP;
        this.port = pPlayerPort;
        active = pActiveGame;
        displayName = pName;
    }
    public void setActive(Game pActiveGame)
    {
        active = pActiveGame;
    }
    public String getIP()
    {
        return IP;
    }
    public int getPort() { return port; }
    public Game getActive()
    {
        return active;
    }
    public String getDisplayName()
    {
        return displayName;
    }
    
}
