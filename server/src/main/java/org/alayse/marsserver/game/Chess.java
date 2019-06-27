package org.alayse.marsserver.game;

public class Chess {
    private int c_id;
    private int place;
    private int state;
    public Chess(int id,int p){
        c_id=id;
        place=p;
        state=-1;
    }
    public int getC_id(){  return c_id;}
    public int getPlace(){ return place;}
    public void setPlace(int p){   place=p;}
    public int getState(){  return state;}
    public void setState(int s){    state=s;}
}
