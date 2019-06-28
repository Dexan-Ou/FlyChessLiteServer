package org.alayse.marsserver.game;

public class Player {
    private int p_id;
    private Chess[] chesses=new Chess[4];
    public Player(int id){
        p_id=id;
        for(int i=0;i<4;i++){
            chesses[i]=new Chess(i+id*4,76+p_id*5+i);
        }
    }
    public int getP_id(){   return p_id;}
    public Chess getChess(int c_id){
        return chesses[c_id%4];
    }
    public void setChesses(Chess c,int c_id){
        chesses[c_id%4]=c;
    }
}
