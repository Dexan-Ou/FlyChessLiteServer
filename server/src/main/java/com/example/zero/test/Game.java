package com.example.zero.test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Enumeration;
import java.util.Vector;

public class Game {
    private int g_id;
    private Player[] players=new Player[4];
    private int order;
    private int roll;
    private Vector action=new Vector();

    public Game(int g,int[] ai,int o){
        g_id=g;
        for(int i=0;i<4;i++){
            players[i]=new Player(i,ai[i]);
        }
        order=o;
        roll=0;
    }
    public int getG_id(){   return g_id;}
    public void setAi(int p_id){
        players[p_id].setAi(1);
    }
    public void resetAi(int p_id){
        players[p_id].setAi(0);
    }
    public int getOrder(){  return order;}
    public void nextOrder(){
        order=(order+1)%4;
        roll=0;
    }
    private boolean isEnd(int color,int place){
        return place==(57+color*6);
    }
    public int isWin(){
        for(int i=0;i<4;i++){
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
    public void flyChess(int[] c_id,int step){
        if(c_id!=null) {
            int color = c_id[0] / 4;
            int place = players[color].getChess(c_id[0]).getPlace();
            Chess chess;
            if (place < -4) {
                chess = players[color].getChess(c_id[0]);
                if (step == 6 && chess.getState() == -1) {
                    int next_place = nextPlace(color, place, 1);
                    chess.setPlace(next_place);
                    chess.setState(0);
                    String act = String.valueOf(c_id[0]) + "," + String.valueOf(place) + "," + String.valueOf(next_place);
                    action.add(act);
                }
            } else {
                String act;
                int next_place = place;
                int direction = 1;
                while (step != 0) {
                    int past = next_place;
                    next_place = nextPlace(color, next_place, direction);
                    for (int i = 0; i < c_id.length; i++) {
                        chess = players[color].getChess(c_id[i]);
                        chess.setPlace(next_place);
                        players[color].setChesses(chess, c_id[i]);
                        act = String.valueOf(c_id[i]) + "," + String.valueOf(past) + "," + String.valueOf(next_place);
                        action.add(act);
                    }
                    if (isEnd(color, next_place)) {
                        direction = -direction;
                    }
                    step--;
                }
            }
            for (int i = 0; i < c_id.length; i++) {
                flyPlace(c_id[i]);
            }
        }
    }
    private int nextPlace(int color,int place,int direction){
        int next_place;
        if(place<-4){
            next_place=-1-color;
        }
        else if(place<0){
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
        eatChess(color,place);
        if(isEnd(color,place)){
            chess.setState(1);
            chess.setPlace(-5-c_id);
            players[color].setChesses(chess,c_id);
            String act=String.valueOf(c_id)+","+String.valueOf(place)+","+String.valueOf(chess.getPlace());
            action.add(act);
        }
        else if(place==(color*13+17)%52){
            chess.setPlace((place+16)%52);
            players[color].setChesses(chess,c_id);
            String act=String.valueOf(c_id)+","+String.valueOf(place)+","+String.valueOf((place+4)%52);
            action.add(act);
            eatChess(color,(place+4)%52);
            eatChess(color,54+((color+2)%4)*6);
            act=String.valueOf(c_id)+","+String.valueOf((place+4)%52)+","+String.valueOf(chess.getPlace());
            action.add(act);
            eatChess(color,chess.getPlace());
        }
        else if(place==(color*13+21)%52){
            chess.setPlace((place+16)%52);
            players[color].setChesses(chess,c_id);
            String act=String.valueOf(c_id)+","+String.valueOf(place)+","+String.valueOf((place+12)%52);
            action.add(act);
            eatChess(color,(place+12)%52);
            eatChess(color,54+((color+2)%4)*6);
            act=String.valueOf(c_id)+","+String.valueOf((place+12)%52)+","+String.valueOf(chess.getPlace());
            action.add(act);
            eatChess(color,chess.getPlace());
        }
        else if((place%4)==(color+1)%4&&place!=(49+color*13)%52){
            chess.setPlace((place+4)%52);
            players[color].setChesses(chess,c_id);
            String act=String.valueOf(c_id)+","+String.valueOf(place)+","+String.valueOf(chess.getPlace());
            action.add(act);
            eatChess(color,chess.getPlace());
        }
    }
    private void eatChess(int color,int place){
        for(int i=0;i<4;i++){
            if(i!=color){
                for(int j=0;j<4;j++){
                    Chess eatChess=players[i].getChess(j);
                    if(eatChess.getPlace()==place){
                        eatChess.setPlace(-5-j-i*4);
                        eatChess.setState(-1);
                        players[i].setChesses(eatChess,j);
                        String act=String.valueOf(eatChess.getC_id())+","+String.valueOf(place)+","+String.valueOf(eatChess.getPlace());
                        action.add(act);
                    }
                }
            }
        }
    }
    private int[] aiChoice(int color,int step){
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
        int[] same=new int[]{0,0,0,0};
        for(int j=0;j<4;j++){
            if(values[j]>values[max]){
                max=j;
            }
            for(int k=0;k<4;k++){
                if(values[j]==values[k]){
                    same[j]++;
                }
            }
        }
        int [] c_id=null;
        if(values[max]>0){
            int max_c=0;
            for(int j=0;j<4;j++){
                if(same[j]>max_c){
                    max_c=same[j];
                }
            }
            c_id=new int[max_c];
            if(max_c==1){
                c_id[0]=max+4*color;
            }
            else{
                int k=0;
                for(int j=0;j<4;j++){
                    if(same[j]==max_c){
                        c_id[k]=j+4*color;
                        k++;
                    }
                }
            }
        }
        return c_id;
    }
    public Vector oneRound(int[] c_id,int step){
        action.clear();
        if(roll==2&&step==6){
            for(int i=0;i<4;i++){
                Chess chess=players[order].getChess(i);
                if(chess.getState()!=-1){
                    String act=String.valueOf(chess.getC_id())+","+String.valueOf(chess.getPlace())+","+String.valueOf(-5-chess.getC_id());
                    action.add(act);
                    chess.setState(-1);
                    chess.setPlace(-5-chess.getC_id());
                    players[order].setChesses(chess,i);
                }
            }
        }
        else{
            if(players[order].getAi()==1){
                c_id=aiChoice(order,step);
                flyChess(c_id,step);
            }
            else{
                flyChess(c_id,step);
            }
        }
        roll++;
		if(step == 6 && roll <= 2){
			action.add("nextPlayer," + String.valueOf(order));
		}
		else{
            nextOrder();
			action.add("nextPlayer," + String.valueOf(order));
		}
        return action;
    }
	public String postActionSequence(Vector action)throws Exception{
		JSONArray ActionSequence = new JSONArray();
		for(int i = 0;i < action.size() - 1;i ++){
			JSONObject json = new JSONObject();
			String act = (String)action.get(i);
			String[] actDetail = act.split(",");
			json.put("c_id",actDetail[0]);
			json.put("getPlace",actDetail[1]);
			json.put("setPlace",actDetail[2]);
			ActionSequence.put(json);
		}
		JSONObject json = new JSONObject();
		String act = (String)action.get(action.size() - 1);
		String[] actDetail = act.split(",");
		json.put("nextPlayer",actDetail[1]);
		ActionSequence.put(json);
		String jsonStr = ActionSequence.toString();
		return jsonStr;
	}
    public void test() {
        Vector str;
        while (isWin() == -1) {
            System.out.println("player: "+String.valueOf(order));
            for (int i = 0; i < 3; i++) {
                int r = (int) (1 + Math.random() * 6);
                System.out.println("roll: "+String.valueOf(r));
                str = oneRound(null, r);
                for(int j=0;j<action.size();j++){
                    System.out.println((String)action.get(j));
                }
                if (r != 6) {
                    break;
                }
				System.out.println("Action Sequence:" + postActionSequence(action));
            }
        }
        System.out.println("winner: "+String.valueOf(isWin()));
    }
}
