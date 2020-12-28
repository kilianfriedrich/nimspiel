import java.util.UUID;
/**
 * Write a description of class NimspielServer here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class NimspielServer extends Server
{

    public static void main(String[] args) {
        new NimspielServer(args.length > 0 ? Integer.parseInt(args[0]) : 80);
    }

    // instance variables - replace the example below with your own
    List<Game> hostedGames = new List<Game>();
    List<Game> openGames = new List<Game>();
    List<Player> clients = new List<Player>();

    /**
     * Constructor for objects of class NimspielServer
     */
    public NimspielServer(int pPortnummer)
    {
        super(pPortnummer);
    }

    public synchronized void processNewConnection(String pPlayerID, int pPlayerPort)
    {
    }

    public synchronized void processMessage(String pPlayerIP, int pPlayerPort, String pMessage)
    {
        System.out.println("[SERVER] Received \"" + pMessage + "\" from " + pPlayerIP + ":" + pPlayerPort);
        String[] incom = pMessage.split(" ", 2);
        clients.toFirst();
        boolean login = false;
        while (clients.hasAccess()){
            if (clients.getContent().getIP().equals(pPlayerIP) && clients.getContent().getPort() == pPlayerPort){
                login = true;
            }
            clients.next();
        }
        if (login == false){
            if (incom[0].equals("login")){
                clients.append(new Player(pPlayerIP, pPlayerPort, null, incom[1]));
                this.send(pPlayerIP, pPlayerPort, "gamelobbies" + scrapLobbies(openGames));
            } else {
                this.send(pPlayerIP, pPlayerPort, "error");
            }
        }
        if (login == true){
            if (incom[0].equals("join")){
                String ID = incom[1];
                openGames.toFirst();
                while (openGames.hasAccess() && !(UUID.fromString(ID).equals(openGames.getContent().getGameID()))){ // sucht das Spiel in der Liste
                    openGames.next();
                }
                if(openGames.hasAccess()){ //Wenn er das Spiel gefunden hat, legt er den Anfrager als Gast fest und bestätigt
                    getPlayer(pPlayerIP, pPlayerPort).setActive(openGames.getContent());
                    openGames.getContent().setGuest(getPlayer(pPlayerIP, pPlayerPort));
                    openGames.getContent().pickStarter();
                    if (openGames.getContent().getActive() == 1){ // gibt wieder wer die Runde startet
                        this.send(pPlayerIP, pPlayerPort, "ok 1");
                        this.send(openGames.getContent().getHost().getIP(), openGames.getContent().getHost().getPort(), "opponentjoined " + openGames.getContent().getGuest().getDisplayName() + " -1");
                    } else {
                        this.send(pPlayerIP, pPlayerPort, "ok -1");
                        this.send(openGames.getContent().getHost().getIP(), openGames.getContent().getHost().getPort(), "opponentjoined " + openGames.getContent().getGuest().getDisplayName() + " 1");
                    }
                    hostedGames.append(openGames.getContent()); // Hier wird das Spiel von einer offenen Lobby zu einer geschlossenen Lobby
                    sendToAll("delgame " + openGames.getContent().getGameID().toString());
                    openGames.remove();
                } else {
                    this.send(pPlayerIP, pPlayerPort, "error");
                }
            }
            if (incom[0].equals("create")){
                Game newGame = new Game(getPlayer(pPlayerIP, pPlayerPort), UUID.randomUUID(), incom[1]);
                openGames.append(newGame);
                getPlayer(pPlayerIP, pPlayerPort).setActive(newGame);
                this.send(pPlayerIP, pPlayerPort, "ok " + getGame(pPlayerIP, pPlayerPort).getGameID().toString());
                sendToAll("addgame " + incom[1] + " " + getGame(pPlayerIP, pPlayerPort).getGameID().toString() + " " + getPlayer(pPlayerIP, pPlayerPort).getDisplayName());

            }

            if(incom[0].equals("pick")) {

                Player player = getPlayer(pPlayerIP, pPlayerPort);
                Game game = player.getActive();

                if(game == null || !game.isActive(player)) {
                    send(pPlayerIP, pPlayerPort, "error");
                    return;
                }

                try {
                    game.update(Integer.parseInt(incom[1]));
                } catch(RuntimeException e) {
                    send(pPlayerIP, pPlayerPort, "error");
                    return;
                }

                if(game.getHost() == player)
                    send(game.getGuest().getIP(), game.getGuest().getPort(), "updatefield " + incom[1]);
                else
                    send(game.getHost().getIP(), game.getHost().getPort(), "updatefield " + incom[1]);

                send(pPlayerIP, pPlayerPort, "ok");
                System.out.println(game.leftSticks);

            }

            if (incom[0].equals("leave")){
                Player self = getPlayer(pPlayerIP, pPlayerPort);
                if ((self.getActive() != null)){ // wirkt nur wenn der Wirker auch in einem Spiel ist

                    Game game = self.getActive();

                    if(game.getHost() == self && game.getGuest() == null) {

                        sendToAll("delgame " + game.getGameID().toString());
                        openGames.toFirst();
                        while(openGames.hasAccess()) {
                            if (openGames.getContent() == game)
                                openGames.remove();
                            openGames.next();
                        }

                    } else if(game.getHost() == self) {

                        send(game.getGuest().getIP(), game.getGuest().getPort(), "opponentleft");

                        // Gast wird zum Host, wenn Host das Spiel verlässt
                        game.host = game.getGuest();
                        game.setGuest(null);

                        sendToAll("addgame " + game.getName() + " " + game.getGameID() + " " + game.getHost().getDisplayName());

                        // zu offenen Spielen hinzufügen
                        openGames.append(game);
                        hostedGames.toFirst();
                        while(hostedGames.hasAccess()) {
                            if (hostedGames.getContent() == game)
                                hostedGames.remove();
                            hostedGames.next();
                        }

                    } else {

                        send(game.getHost().getIP(), game.getHost().getPort(), "opponentleft");

                        sendToAll("addgame " + game.getName() + " " + game.getGameID() + " " + game.getHost().getDisplayName());

                        game.setGuest(null);

                        // zu offenen Spielen hinzufügen
                        openGames.append(game);
                        hostedGames.toFirst();
                        while(hostedGames.hasAccess()) {
                            if (hostedGames.getContent() == game)
                                hostedGames.remove();
                            hostedGames.next();
                        }
                    }

                    game.reset();

                    send(pPlayerIP, pPlayerPort, "ok");

                } else {
                    this.send(pPlayerIP, pPlayerPort, "error");
                }
            }
        }
    }

    public synchronized void processClosingConnection(String pPlayerID, int pPlayerPort)
    {
        processMessage(pPlayerID, pPlayerPort, "leave");
    }

    public Game getGame(String pPlayerID, int pPlayerPort)
    {
        clients.toFirst();
        while (clients.hasAccess()){
            if (clients.getContent().getIP().equals(pPlayerID) && clients.getContent().getPort() == pPlayerPort){
                return clients.getContent().getActive();
            }
            clients.next();
        }
        return null;
    }

    public Player getPlayer(String pPlayerID, int pPlayerPort)
    {
        clients.toFirst();
        while (clients.hasAccess()){
            if (clients.getContent().getIP().equals(pPlayerID) && clients.getContent().getPort() == pPlayerPort){
                return clients.getContent();
            }
            clients.next();
        }
        return null;
    }

    public Player getHost(UUID pGameID)
    {
        openGames.toFirst();
        while (openGames.hasAccess()){
            if (openGames.getContent().getGameID().equals(pGameID)){
                return openGames.getContent().getHost();
            }
            openGames.next();
        }
        hostedGames.toFirst();
        while (hostedGames.hasAccess()){
            if (hostedGames.getContent().getGameID().equals(pGameID)){
                return hostedGames.getContent().getHost();
            }
            hostedGames.next();
        }
        return null;
    }

    public String scrapLobbies(List<Game> List)
    {
        String output = "";
        List.toFirst();
        while (List.hasAccess()){
            Game current = List.getContent();
            output = output + " " +current.getName();
            output = output + " " +current.getGameID().toString();
            output = output + " " +current.getHost().getDisplayName();
            List.next();
        }
        return output;
    }
}
