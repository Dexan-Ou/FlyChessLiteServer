package org.alayse.marsserver.game;

public class GameStatus {
    public String content;
    public String nextPlayer;
    public GameStatus(String content, String nextPlayer){
        this.content = content;
        this.nextPlayer = nextPlayer;
    }
}
