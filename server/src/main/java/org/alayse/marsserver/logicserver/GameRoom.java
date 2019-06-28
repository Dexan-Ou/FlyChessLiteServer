package org.alayse.marsserver.logicserver;

import org.alayse.marsserver.game.GameHandler;
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

    public boolean createRoom(String userName, String roomName, int playerLimit){
        if (roomList.containsKey(roomName))
            return false;
        roomList.put(roomName, new GameHandler(roomName,playerLimit));
        this.joinRoom(userName, roomName);
        return true;
    }

    public roomStatus joinRoom(String userName, String roomName){
        if (!roomList.containsKey(roomName)){
            roomStatus rs = new roomStatus();
            rs.status = -1;
            return rs;
        }
        final GameHandler gameHandler = roomList.get(roomName);
        if (gameHandler.getPlayerSize() >= gameHandler.getPlayerLimit()) {
            roomStatus rs = gameHandler.getRoomStatus();
            rs.status = -1;
            return rs;
        }
        playerList.put(userName, roomName);
        return gameHandler.joinRoom(userName);
    }

    public void leftRoom(String userName){
        if (playerList.containsKey(userName)) {
            roomList.get(playerList.get(userName)).leftRoom(userName);
            playerList.remove(userName);
        }
    }

    public String getPlayerRoom(String userName){
        if (playerList.containsKey(userName))
            return playerList.get(userName);
        return "";
    }
}
