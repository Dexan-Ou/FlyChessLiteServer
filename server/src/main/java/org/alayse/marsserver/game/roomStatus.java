package org.alayse.marsserver.game;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bound on 2019/6/26.
 */

public class roomStatus {
    public int status = 0;
    public ConcurrentHashMap<String,Integer> colorMap;
    public ConcurrentHashMap<Integer,String> colorMapReverse;
    public roomStatus(){
        colorMap = new ConcurrentHashMap<>();
        colorMapReverse = new ConcurrentHashMap<>();
    }
    int getFirstEmptyColor(int limit){
        for (int i = 0; i < limit; i++)
            if (!colorMapReverse.containsKey(i))
                return i;
        return -1;
    }
}
