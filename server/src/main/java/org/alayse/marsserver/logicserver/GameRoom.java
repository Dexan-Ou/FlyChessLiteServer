package org.alayse.marsserver.logicserver;

import org.alayse.marsserver.game.GameHandler;
import org.alayse.marsserver.game.GameStatus;
import org.alayse.marsserver.game.roomStatus;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class GameRoom {
    public static Logger logger = Logger.getLogger(GameRoom.class.getName());

    public ConcurrentHashMap<String, GameHandler> roomList;
    public ConcurrentHashMap<String, String> playerList;

    private static GameRoom gameRooms = new GameRoom();
    public static GameRoom getInstance() {
        return gameRooms;
    }

    private GameRoom(){
        roomList = new ConcurrentHashMap<>();
        playerList = new ConcurrentHashMap<>();
    }

    public boolean createRoom(String userName, String roomName, int playerLimit, int botNum){
        if (roomList.containsKey(roomName) || botNum > playerLimit - 1 || botNum < 0 || playerLimit <= 0)
            return false;
        roomList.put(roomName, new GameHandler(roomName, playerLimit, botNum));
        this.joinRoom(userName, roomName);
        return true;
    }

    public roomStatus joinRoom(String userName, String roomName){
        if (!roomList.containsKey(roomName) || playerList.containsKey(userName)){
            roomStatus rs = new roomStatus();
            rs.status = -1;
            return rs;
        }
        final GameHandler gameHandler = roomList.get(roomName);
        roomStatus rs = gameHandler.joinRoom(userName);
        if (rs.status >= 0)
            playerList.put(userName, roomName);
        return rs;
    }

    public void leftRoom(String userName){
        if (playerList.containsKey(userName)) {
            String roomName = playerList.get(userName);
            roomList.get(roomName).leftRoom(userName);
            playerList.remove(userName);
            if (roomList.get(roomName).getPlayerSize() <= 0)
                roomList.remove(roomName);
        }
    }

    public GameStatus getPlayerRoom(String userName){
        if (playerList.containsKey(userName))
        {
            GameHandler gameHandler = roomList.get(playerList.get(userName));
            GameStatus gs = new GameStatus(gameHandler.getChessPlace(), gameHandler.getNextPlayer());
            gs.room = playerList.get(userName);
            return gs;
        }
        return new GameStatus("NULL","NULL");
    }

    public void endGame(String roomName){
        if (!roomList.containsKey(roomName))
            return;
        String[] list = roomList.get(roomName).getJoinPlayerList();
        for (String player: list){
            playerList.remove(player);
        }
        roomList.remove(roomName);
    }
}
