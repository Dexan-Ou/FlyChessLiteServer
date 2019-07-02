package com.example.zero.test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

public class Game {
    private Player[] players;
    private int p_number;
    private int order;
    private int roll;
    private Vector action=new Vector();

    public Game(int n,int ai){
        p_number=n+ai;
        players=new Player[p_number];
        for(int i=0;i<n;i++){
            players[i]=new Player(i,0);
        }
        for(int i=0;i<ai;i++){
            players[n+i]=new Player(n+i,1);
        }
        order=0;
        roll=0;
    }
    private void nextOrder(){
        order=(order+1)%p_number;
        roll=0;
    }
    private boolean isEnd(int color,int place){
        return place==(57+color*6);
    }
    private int isWin(){
        for(int i=0;i<p_number;i++){
            boolean flag=true;
            for(int j=0;j<4;j++){
                Chess chess=players[i].getChess(j);
                if(chess.getState()!=1){
                    flag=false;
                    break;
                }
            }
            if(flag){
                return i;
            }
        }
        return -1;
    }
    private void flyChess(int c_id,int step){
            int color = c_id / 4;
            int place = players[color].getChess(c_id).getPlace();
            Chess chess;
            if (place>75&&place!=80+color*5) {
                chess = players[color].getChess(c_id);
                if (step == 6 && chess.getState() == -1) {
                    int next_place = nextPlace(color, place, 1);
                    chess.setPlace(next_place);
                    chess.setState(0);
                    String act = "(" + String.valueOf(color) + "," + String.valueOf(place) + "," + String.valueOf(next_place) + ")";
                    action.add(act);
                }
            } else {
                String act;
                int next_place = place;
                int direction = 1;
                while (step != 0) {
                    int past = next_place;
                    next_place = nextPlace(color, next_place, direction);
                    chess = players[color].getChess(c_id);
                    chess.setPlace(next_place);
                    players[color].setChesses(chess, c_id);
                    act = "(" + String.valueOf(color) + "," + String.valueOf(past) + "," + String.valueOf(next_place) + ")";
                    action.add(act);
                    if (isEnd(color, next_place)) {
                        direction = -direction;
                    }
                    step--;
                }
            }
            flyPlace(c_id);
    }
    private int nextPlace(int color,int place,int direction){
        int next_place;
        if(place>75&&place!=80+color*5){
            next_place=80+color*5;
        }
        else if(place==80+color*5){
            next_place=color*13;
        }
        else if(place==(color*13+49)%52){
            next_place=52+color*6;
        }
        else if(place>51){
            next_place=place+direction;
        }
        else{
            next_place=(place+direction)%52;
        }
        return next_place;
    }
    private void flyPlace(int c_id){
        int color=c_id/4;
        Chess chess=players[color].getChess(c_id);
        int place=chess.getPlace();
        if(eatChess(c_id,place)){
            if (isEnd(color, place)) {
                chess.setState(1);
                chess.setPlace(76 + color + c_id);
                players[color].setChesses(chess, c_id);
                String act = "(" + String.valueOf(color) + "," + String.valueOf(place) + "," + String.valueOf(chess.getPlace()) + ")";
                action.add(act);
            } else if (place == (color * 13 + 13) % 52) {
                chess.setPlace((place + 4) % 52);
                players[color].setChesses(chess, c_id);
                String act = "(" + String.valueOf(color) + "," + String.valueOf(place) + "," + String.valueOf(chess.getPlace()) + ")";
                action.add(act);
                if(eatChess(c_id, chess.getPlace())) {
                    chess.setPlace(54 + ((color + 2) % 4) * 6);
                    players[color].setChesses(chess, c_id);
                    act = "(" + String.valueOf(color) + "," + String.valueOf((place + 4) % 52) + "," + String.valueOf(chess.getPlace()) + ")";
                    action.add(act);
                    if(eatChess(c_id, chess.getPlace())) {
                        chess.setPlace((place + 16) % 52);
                        players[color].setChesses(chess, c_id);
                        act = "(" + String.valueOf(color) + "," + String.valueOf(54 + ((color + 2) % 4) * 6) + "," + String.valueOf(chess.getPlace()) + ")";
                        action.add(act);
                        eatChess(c_id, chess.getPlace());
                    }
                }
            } else if (place == (color * 13 + 17) % 52) {
                chess.setPlace(54 + ((color + 2) % 4) * 6);
                players[color].setChesses(chess, c_id);
                String act = "(" + String.valueOf(color) + "," + String.valueOf(place) + "," + String.valueOf(chess.getPlace()) + ")";
                action.add(act);
                if(eatChess(c_id, chess.getPlace())) {
                    chess.setPlace((place + 12) % 52);
                    players[color].setChesses(chess, c_id);
                    act = "(" + String.valueOf(color) + "," + String.valueOf(54 + ((color + 2) % 4) * 6) + "," + String.valueOf(chess.getPlace()) + ")";
                    action.add(act);
                    if(eatChess(c_id, chess.getPlace())) {
                        chess.setPlace((place + 16) % 52);
                        players[color].setChesses(chess, c_id);
                        act = "(" + String.valueOf(color) + "," + String.valueOf((place + 12) % 52) + "," + String.valueOf(chess.getPlace()) + ")";
                        action.add(act);
                        eatChess(c_id, chess.getPlace());
                    }
                }
            } else if ((place % 4) == (color + 1) % 4 && place != (49 + color * 13) % 52) {
                chess.setPlace((place + 4) % 52);
                players[color].setChesses(chess, c_id);
                String act = "(" + String.valueOf(color) + "," + String.valueOf(place) + "," + String.valueOf(chess.getPlace()) + ")";
                action.add(act);
                eatChess(c_id, chess.getPlace());
            }
        }
    }
    private boolean eatChess(int c_id,int place){
        int c=0;
        int color=c_id/4;
        for(int i=0;i<p_number;i++){
            if(i!=color){
                for(int j=0;j<4;j++){
                    Chess eatChess=players[i].getChess(j);
                    if(eatChess.getPlace()==place){
                        c++;
                        eatChess.setPlace(76+i*5+j);
                        eatChess.setState(-1);
                        players[i].setChesses(eatChess,j);
                        String act="("+String.valueOf(i)+","+String.valueOf(place)+","+String.valueOf(eatChess.getPlace())+")";
                        action.add(act);
                    }
                }
            }
        }
        if(c>1){
            Chess chess=players[color].getChess(c_id);
            chess.setPlace(76+c_id+color);
            chess.setState(-1);
            String act="("+String.valueOf(color)+","+String.valueOf(place)+","+String.valueOf(chess.getPlace())+")";
            action.add(act);
            return false;
        }
        return true;
    }
    private int aiChoice(int color,int step){
        int[] values=new int[4];
        for(int j=0;j<4;j++){
            Chess chess=players[color].getChess(j);
            int state=chess.getState();
            if(state==-1){
                if(step==6){
                    values[j] = 800+j;
                }
                else{
                    values[j] = -100-j;
                }
            }
            else if(state==0){
                int next_place=chess.getPlace();
                int direction=1;
                while(step!=0){
                    next_place=nextPlace(color,next_place,direction);
                    if(isEnd(color,next_place)){
                        direction=-direction;
                    }
                    step--;
                }
                if(next_place==57+color*6){
                    values[j] = next_place*10;
                }
                else if(next_place>51){
                    values[j] = next_place;
                }
                else{
                    values[j] = next_place-color*13;
                    if(values[j]<0){
                        values[j] = values[j]+52;
                    }
                    values[j] = values[j]+100;
                }
            }
            else{
                values[j] = -200-j;
            }
        }
        int max=0;
        for(int j=0;j<4;j++){
            if(values[j]>values[max]){
                max=j;
            }
        }
        if(values[max]<0){
            return -1;
        }
        else {
            return max+color*4;
        }
    }
    private void oneRound(int ai,int c_id,int step){
        if(roll==2&&step==6){
            for(int i=0;i<4;i++){
                Chess chess=players[order].getChess(i);
                if(chess.getState()!=-1){
                    String act="("+String.valueOf(order)+","+String.valueOf(chess.getPlace())+","+String.valueOf(-5-chess.getC_id())+")";
                    action.add(act);
                    chess.setState(-1);
                    chess.setPlace(76+order+chess.getC_id());
                    players[order].setChesses(chess,i);
                }
            }
        }
        else{
            if(ai==1){
                c_id=aiChoice(order,step);
                if(c_id!=-1) {
                    flyChess(c_id, step);
                }
            }
            else{
                if(c_id>=0&&c_id<p_number*4) {
                    flyChess(c_id, step);
                }
            }
        }
        roll++;
        int w=isWin();
        if(w==-1){
            if(step == 6 && roll <= 2){
                action.add("("+String.valueOf(order)+",-1)");
            }
            else{
                nextOrder();
                action.add("("+String.valueOf(order)+",-1)");
            }
        }
		else{
            action.add("(-1,"+String.valueOf(w)+")");
        }
    }
    private String getActionString(){
        String str="";
        for(int i = 0;i < action.size() - 1;i ++){
            String s=(String)action.get(i);
            if(s.split(",").length!=2) {
                str += s + ";";
            }
        }
        str+=(String)action.get(action.size()-1);
        return str;
    }
    private int findC_id(int place){
        for(int i=0;i<4;i++){
            Chess chess=players[order].getChess(i);
            if(chess.getPlace()==place){
                return chess.getC_id();
            }
        }
        return -1;
    }
    private void aiRun(){
        String act=(String) action.get(action.size()-1);
        int next_player=Integer.valueOf(act.split(",")[0].substring(1));
        while(next_player!=-1&&players[next_player].getAi()==1){
            int r=rollStep();
            oneRound(1,-1,r);
            act=(String) action.get(action.size()-1);
            next_player=Integer.valueOf(act.split(",")[0].substring(1));
        }
    }
    public String run(String str){
        String[] s_p=str.split(",");
        int ai=Integer.valueOf(s_p[0]);
        int step=Integer.valueOf(s_p[1]);
        int place=Integer.valueOf(s_p[2]);
        int c_id=findC_id(place);
        action.clear();
        oneRound(ai,c_id,step);
        aiRun();
        return getActionString();
    }
    public String getPlace(){
        String s_p="";
        for(int i=0;i<p_number;i++){
            for(int j=0;j<4;j++){
                Chess chess=players[i].getChess(j);
                s_p+="("+String.valueOf(i)+",-1,"+String.valueOf(chess.getPlace())+");";
            }
        }
        int w=isWin();
        if(w==-1) {
            s_p += "(" + String.valueOf(order) + ",-1)";
        }
        else{
            s_p += "(-1," + String.valueOf(w)+")";
        }
        return  s_p;
    }
    private int rollStep(){
        return (int) (1 + Math.random() * 6);
    }
    public void test() {
        int w=-1;
        while (w == -1) {
            System.out.println("player: "+String.valueOf(order));
            for (int i = 0; i < 3; i++) {
                int r = rollStep();
                System.out.println("roll: "+String.valueOf(r));
                String str=run("1,"+String.valueOf(r)+",-1");
                System.out.println(str);
                String[] str_list=str.split(";");
                str_list=str_list[str_list.length-1].split(",");
                str=str_list[1].substring(0,str_list[1].length()-1);
                w=Integer.valueOf(str);
                if (r != 6||w!=-1) {
                    break;
                }
            }
        }
        System.out.println("winner: "+String.valueOf(w));
    }
}
