package org.alayse.marsserver.game;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class GameHandler {
    ConcurrentLinkedDeque<String> joinPlayer;
    String roomName;
    int playerLimit;
    roomStatus rs = new roomStatus();
    Game game;
    public GameHandler(String roomName, int playerLimit){
        this.roomName=roomName;
        this.playerLimit=playerLimit;
        this.joinPlayer=new ConcurrentLinkedDeque<>();
    }

    public int getPlayerLimit() {
        return playerLimit;
    }
    public roomStatus getRoomStatus(){return rs;}

    public boolean checkUser(String userName){
        return joinPlayer.contains(userName);
    }

    public roomStatus joinRoom(String userName) {
        if (this.checkUser(userName)) {
            rs.status = -1;
            return rs;
        }
        int emptyColor = rs.getFirstEmptyColor(this.playerLimit);
        if (joinPlayer.size() != playerLimit) {
            joinPlayer.offer(userName);
            rs.status = 0;
            rs.colorMap.put(userName, emptyColor);
            rs.colorMapReverse.put(emptyColor, userName);
            return rs;
        }
        joinPlayer.offer(userName);
        rs.status = 1;
        rs.colorMap.put(userName, emptyColor);
        rs.colorMapReverse.put(emptyColor, userName);
        return rs;
    }

    public boolean leftRoom(String userName){
        if (!this.checkUser(userName))
            return false;
        joinPlayer.remove(userName);
        rs.colorMapReverse.remove(rs.colorMap.get(userName));
        rs.colorMap.remove(userName);
        return true;
    }

    public int getPlayerSize(){
        return joinPlayer.size();
    }

    public String startGame(){
        game = new Game(getPlayerSize(), 0);
        return "game start";
    }
    public GameStatus runOneRound(String content){
        String actions = game.run(content);
        String actionArray[] = actions.split(";");
        int order = Integer.parseInt(actionArray[actionArray.length - 1].split(",")[0].substring(1));
        Set<Map.Entry<String,Integer>> entrySet = rs.colorMap.entrySet();
        Iterator<Map.Entry<String,Integer>> it = entrySet.iterator();
        String nextplayer = "";
        while(it.hasNext()){
            Map.Entry<String,Integer> entry = it.next();
            String key = entry.getKey();
            int value = entry.getValue();
            if(value == order){
                nextplayer = key;
                break;
            }
        }
        String actionSequence = "";
        for(int i = 0;i < actionArray.length - 1;i ++){
            actionSequence = actionSequence + actionArray[i] + ";";
        }
        return new GameStatus(actionSequence, nextplayer);
    }

}
