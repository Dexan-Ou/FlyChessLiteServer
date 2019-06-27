package org.alayse.marsserver.logicserver;

import org.alayse.marsserver.game.GameHandler;
import org.alayse.marsserver.game.roomStatus;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

public class GameRoom {
    public static Logger logger = Logger.getLogger(GameRoom.class.getName());

    public ConcurrentHashMap<String, GameHandler> roomList;

    private static GameRoom gameRooms = new GameRoom();
    public static GameRoom getInstance() {
        return gameRooms;
    }

    private GameRoom(){
        roomList = new ConcurrentHashMap<>();
    }

    public boolean createRoom(String userName, String roomName, int playerLimit){
        if (roomList.containsKey(roomName))
            return false;
        roomList.put(roomName, new GameHandler(roomName,playerLimit));
        roomList.get(roomName).joinRoom(userName);
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
        return gameHandler.joinRoom(userName);
    }

    public void leftRoom(String userName){
        for (String roomName : roomList.keySet()) {
            roomList.get(roomName).leftRoom(userName);
        }
    }
}
