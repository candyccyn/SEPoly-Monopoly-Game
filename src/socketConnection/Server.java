package socketConnection;

import model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Server {
    private List<ClientHandler> clients;
    private ServerSocket serverSocket;
    private ArrayList<Player> players;
    private ArrayList<Space> map;
    private ArrayList<Card> communityDeck;
    private ArrayList<Card> chanceDeck;
    private ArrayList<String> names;
    private Integer highestPlayerIDBidder;
    private Integer highestBiddingMoney;
    private PropertySpace auctionProperty;
    final private int STARTINGMONEY = 1500000;
    final private int CARDCOUNT = 9;
    final private int MAX_PLAYER = 4;
    final private String basePath = "C:\\Users\\Lenovo\\Documents\\SE\\Year2S1\\Java\\Monopoly\\";
    final private String databasePath = "jdbc:sqlite:" + basePath + "src\\Database\\SEpoly.db";

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clients = Collections.synchronizedList(new ArrayList<>());
        players = new ArrayList<>();
        map = new ArrayList<>();
        communityDeck = new ArrayList<>();
        chanceDeck = new ArrayList<>();
        names = new ArrayList<>();
    }

    public void connect() throws IOException {
        try {
            System.out.println("waiting for client");
            Socket socket = serverSocket.accept();
            System.out.println("connected");
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            int ID = players.size();
            ClientHandler clientHandler = new ClientHandler(socket, inputStream, outputStream, this, ID);
            Player player = new Player(STARTINGMONEY, ID);

            clients.add(clientHandler);
            players.add(player);
            clientHandler.start();
        } catch (Exception e) {
            close();
        }
    }

    public void start() throws IOException, SQLException {
        highestPlayerIDBidder = null;
        highestBiddingMoney = null;
        initMapData();
        initCardData();
        sendMapData();
        sendInitPlayerData();
        sendInitOpponentData();
    }

    public void sendToAllClients(ServerMessage serverMessage) throws IOException {
        for (ClientHandler client : clients) {
            client.getOutputStream().writeUnshared(serverMessage);
            client.getOutputStream().reset();
        }
    }

    public void startNextPlayerTurn(int playerID) throws IOException {
        System.out.println("player ending turn: " + playerID);
        int nextPlayerIDTurn = 0;

        while (true) {
            nextPlayerIDTurn = playerID == players.size() - 1 ? 0 : playerID + 1;

            if (players.get(nextPlayerIDTurn) != null) {
                break;
            }
        }

        ClientHandler firstPlayerClient = clients.get(nextPlayerIDTurn);
        ServerMessage serverMessage = new ServerMessage("startTurn", "");

        firstPlayerClient.getOutputStream().writeUnshared(serverMessage);
//        firstPlayerClient.getOutputStream().reset();
        firstPlayerClient.getOutputStream().flush();
    }

    public void updatePlayer(Player player) throws IOException {
        players.set(player.getID(), player);
        PlayerObj playerObj = new PlayerObj(player.getX(), player.getY(), player.getMoney(), player.getID());
        ServerMessage serverMessage = new ServerMessage("updateOpponent", playerObj);
//        sendToAllExcept(player.getID(), serverMessage);
        sendToAllClients(serverMessage);
    }

    public void addNames(String name) throws IOException {
        names.add(name);

        if (names.size() == MAX_PLAYER) {
            sendAllNames();
            sendStartGame();
            startNextPlayerTurn(-1);
        }
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    private void initMapData() throws SQLException {
        //init map queried from database here
        try {
            String sDriverName = "org.sqlite.JDBC";
            Class.forName(sDriverName);
            Connection connection = DriverManager.getConnection(databasePath);
            Statement statement = connection.createStatement();
            ResultSet estate = statement.executeQuery("select * from Map");
            Space temp;

            while (estate.next()) {
                double[] pos = new double[8];
                pos[0] = estate.getDouble(13);
                pos[1] = estate.getDouble(14);
                pos[2] = estate.getDouble(15);
                pos[3] = estate.getDouble(16);
                pos[4] = estate.getDouble(17);
                pos[5] = estate.getDouble(18);
                pos[6] = estate.getDouble(19);
                pos[7] = estate.getDouble(20);
                //System.out.println(pos[0]);
                switch (estate.getString(3)) {
                    case "estate":
                        temp = new EstateSpace(estate.getInt(1), estate.getString(2), estate.getInt(4),
                                estate.getInt(7), estate.getInt(8), estate.getInt(9),
                                estate.getInt(10), estate.getInt(11), estate.getInt(5),
                                estate.getInt(6), estate.getBytes(12), pos);
                        map.add(temp);
                        break;
                    case "utility":
                        temp = new UtilitySpace(estate.getInt(1), estate.getString(2),
                                estate.getInt(4), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "railroad":
                        temp = new RailroadSpace(estate.getInt(1), estate.getString(2),
                                estate.getInt(4), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "start":
                        temp = new StartSpace(estate.getInt(1), estate.getString(2),
                                estate.getInt(4), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "community":
                        temp = new CardSpace(estate.getInt(1), estate.getString(2),
                                estate.getString(3), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "chance":
                        temp = new CardSpace(estate.getInt(1), estate.getString(2),
                                estate.getString(3), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "free parking":
                        temp = new FreeParkingSpace(estate.getInt(1), estate.getString(2), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "tax":
                        temp = new TaxSpace(estate.getInt(1), estate.getString(2), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "jail":
                        temp = new JailSpace(estate.getInt(1), estate.getString(2), pos, estate.getBytes(12));
                        map.add(temp);
                        break;
                    case "go to jail":
                        temp = new MoveSpace(estate.getInt(1), estate.getString(2), pos, estate.getBytes(12));
                        map.add(temp);
                        break;

                }
            }
            connection.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void setNewHighestBid(BidObj bidObj) throws IOException {
        highestBiddingMoney = bidObj.getBidMoney();
        highestPlayerIDBidder = bidObj.getPlayerID();

        ServerMessage serverMessage = new ServerMessage("updateHighestBidPrice", bidObj);
        sendToAllClients(serverMessage);
    }

    public void startAuction(ServerMessage serverMessage) throws IOException {
        auctionProperty = (PropertySpace) serverMessage.getData();
        sendToAllClients(serverMessage);
    }

    public void endAuction() throws IOException {
        ServerMessage serverMessage = new ServerMessage("endAuction", "");
        sendToAllClients(serverMessage);

        if (highestPlayerIDBidder != null) {
            Player player = players.get(highestPlayerIDBidder);
            player.pay(highestBiddingMoney - auctionProperty.getPrice());
            player.buy(auctionProperty);
            auctionProperty.soldTo(player);

            //update player to player client
            serverMessage = new ServerMessage("updatePlayer", player);
            sendToPlayer(serverMessage, player.getID());

            //update player to opponents
            updatePlayer(player);

            //update map to every player
            serverMessage = new ServerMessage("updateMap", auctionProperty);
            sendToAllClients(serverMessage);
        }

        highestPlayerIDBidder = null;
        highestBiddingMoney = null;
    }

    public void drawCard(DrawCardObj drawCardObj) throws IOException {
        Random randomGenerator = new Random();
        int cardNumber = randomGenerator.nextInt(CARDCOUNT);
        Card card = drawCardObj.getdeckType().equalsIgnoreCase("community") ? communityDeck.get(cardNumber) : chanceDeck.get(cardNumber);
        String effect = card.getEffect();
        int effectAmount = card.getEffectAmount();
        Player player = players.get(drawCardObj.getPlayerID());

        ServerMessage serverMessage = new ServerMessage("showCard", card.getImage());
        sendToAllClients(serverMessage);

        switch (effect) {
            case ("getPaid"): {
                player.getPaid(effectAmount);
                break;
            }

            case ("pay"): {
                player.pay(effectAmount);
//                if (isBankrupt(effectAmount)) {
//                    player.pay(effectAmount);
//                }
//                checkBankrupt(player);
                break;
            }

            case ("breakJail"): {
                player.drawBreakJailCard();
                break;
            }
            case ("getJailed"): {
                player.jailed();
                serverMessage = new ServerMessage("goToJail", "");
                sendToPlayer(serverMessage, player.getID());
                break;
            }

            case ("moveForward"): {
                serverMessage = new ServerMessage("moveForward", effectAmount);
                sendToPlayer(serverMessage, player.getID());
                break;
            }

            case ("moveTo"): {
                serverMessage = new ServerMessage("moveTo", effectAmount);
                sendToPlayer(serverMessage, player.getID());
                break;
            }

            default:
                break;
        }
        //TODO: player act on card effect

        serverMessage = new ServerMessage("updatePlayer", player);
        sendToPlayer(serverMessage, player.getID());
        updatePlayer(player);
    }

    public void payRentPlayer(GetPaidObj getPaidObj) throws IOException {
        System.out.println("HIA");
        Player player = players.get(getPaidObj.getPlayerID());
        player.getPaid(getPaidObj.getRent());
        ServerMessage serverMessage = new ServerMessage("updatePlayer", player);
        sendToPlayer(serverMessage, player.getID());
        updatePlayer(player);
    }

    public void sendToAllExcept(int ID, ServerMessage serverMessage) throws IOException {
        for (ClientHandler client : clients) {
            if (client.getID() == ID) {
                continue;
            }
            client.getOutputStream().writeUnshared(serverMessage);
//            client.getOutputStream().reset();
            client.getOutputStream().flush();
        }
    }

    public void playerBankrupt(int playerID) throws IOException {
        Player player = players.get(playerID);
        ServerMessage mapMessage = new ServerMessage("updateMap", "");
        ArrayList<PropertySpace> properties = player.getAllProperty();

        for (PropertySpace property : properties) {
            property.soldBack();
            mapMessage.setData(property);
            sendToAllClients(mapMessage);
        }

        players.set(playerID, null);
        ServerMessage serverMessage = new ServerMessage("bankrupt", playerID);
        sendToAllClients(serverMessage);
    }

    private void sendInitPlayerData() throws IOException {
        ServerMessage serverMessage = new ServerMessage("initPlayer", "");

        for (ClientHandler client : clients) {
            Player player = players.get(client.getID());
            serverMessage.setData(player);
            client.getOutputStream().writeUnshared(serverMessage);
            client.getOutputStream().reset();
        }
    }

    private void sendInitOpponentData() throws IOException {
        ServerMessage serverMessage = new ServerMessage("initOpponents", "");
        ArrayList<PlayerObj> opponents = new ArrayList<>();

        for (ClientHandler client : clients) {
            for (Player player : players) {
                if (player.getID() == client.getID()) {
                    continue;
                }
                PlayerObj opponentObj = new PlayerObj(player.getX(), player.getY(), player.getMoney(), player.getID());
                opponents.add(opponentObj);
            }
            serverMessage.setData(opponents);
            client.getOutputStream().writeUnshared(serverMessage);
            client.getOutputStream().reset();
            opponents.clear();
        }
    }

    private void sendMapData() throws IOException {
        ServerMessage serverMessage = new ServerMessage("initMap", map);
        sendToAllClients(serverMessage);
    }

    private void sendStartGame() throws IOException {
        System.out.println("starting!");
        ServerMessage serverMessage = new ServerMessage("startGame", "");
        sendToAllClients(serverMessage);
    }


    private void sendToPlayer(ServerMessage serverMessage, int ID) throws IOException {
        ClientHandler client = clients.get(ID);
        client.getOutputStream().writeUnshared(serverMessage);
        client.getOutputStream().reset();
    }

    private void checkBankrupt(Player player) {
        //TODO: check bankrupt
    }

    private void initCardData() throws SQLException {
        initCommunityCardData();
        initChanceCardData();
    }

    private void initCommunityCardData() throws SQLException {
        //TODO: query community card data from database
        try {
            Connection connection = DriverManager.getConnection(databasePath);
            Statement statement = connection.createStatement();
            ResultSet card = statement.executeQuery("select * from Community_cards");
            Card temp;
            while (card.next()) {
                //System.out.println(card.getString(3) + card.getInt(4));
                temp = new Card(card.getString(3), card.getInt(4), card.getBytes(5));
                communityDeck.add(temp);
            }
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void initChanceCardData() throws SQLException {
        //TODO: query chance card data from database
        try {
            Connection connection = DriverManager.getConnection(databasePath);
            Statement statement = connection.createStatement();
            ResultSet card = statement.executeQuery("select * from Chance_cards");
            Card temp;
            while (card.next()) {
                //System.out.println(card.getString(3) + card.getInt(4));
                temp = new Card(card.getString(3), card.getInt(4), card.getBytes(5));
                chanceDeck.add(temp);
            }
            connection.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void sendAllNames() throws IOException {
        ServerMessage serverMessage = new ServerMessage("initNames", names);
        sendToAllClients(serverMessage);
    }

//    private boolean isBankrupt(int payingAmount) {
//
//        return true;
//    }
}
