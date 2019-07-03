package org.alayse.marsserver.game;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class GameHandler {
    ConcurrentLinkedDeque<String> joinPlayer;
    String roomName;
    int playerLimit, botNum;
    roomStatus rs = new roomStatus();
    Game game;
    int order;
    public GameHandler(String roomName, int playerLimit, int botNum){
        this.roomName = roomName;
        this.playerLimit = playerLimit  - botNum;
        this.botNum = botNum;
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
        if (this.checkUser(userName) || this.rs.status >= 1 || joinPlayer.size() >= playerLimit) {
            roomStatus temp_rs = new roomStatus();
            temp_rs.status = -1;
            return temp_rs;
        }
        int emptyColor = rs.getFirstEmptyColor(this.playerLimit);
        joinPlayer.offer(userName);
        rs.colorMap.put(userName, emptyColor);
        rs.colorMapReverse.put(emptyColor, userName);
        if (joinPlayer.size() < playerLimit)
            rs.status = 0;
        else
            rs.status = 1;
        return rs;
    }

    public boolean leftRoom(String userName){
        if (!this.checkUser(userName)) {
            return false;
        }
        joinPlayer.remove(userName);
        int color = rs.colorMap.get(userName);
        if(rs.status > 1) {
            game.setPlayerAI(color,1);
        }
        rs.colorMapReverse.remove(rs.colorMap.get(userName));
        rs.colorMap.remove(userName);
        return true;
    }

    public int getPlayerSize(){
        return joinPlayer.size();
    }

    public GameStatus startGame(){
        game = new Game(getPlayerSize(), this.botNum);
        rs.status = 2;
        return new GameStatus("game start", this.joinPlayer.getFirst());
    }
    public GameStatus runOneRound(String content){
        String actions = game.run(content);
        String actionArray[] = actions.split(";");
        order = Integer.parseInt(actionArray[actionArray.length - 1].split(",")[0].substring(1));
        if (order == -1) {
            rs.status = 3;
            return new GameStatus(actions, "NULL");
        }
        String nextPlayer = this.getNextPlayer();
        String actionSequence = "";
        for(int i = 0;i < actionArray.length - 1;i ++){
            actionSequence = actionSequence + actionArray[i] + ";";
        }
        return new GameStatus(actionSequence, nextPlayer);
    }
    public String getChessPlace(){
        return game.getPlace();
    }
    public String getNextPlayer(){
        return rs.colorMapReverse.get(order);
    }
    public String[] getJoinPlayerList(){
        return this.joinPlayer.toArray(new String[0]);
    }
}
