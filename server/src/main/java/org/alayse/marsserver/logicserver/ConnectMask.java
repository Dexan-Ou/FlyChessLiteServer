package org.alayse.marsserver.logicserver;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;

public class ConnectMask {
    public ConcurrentHashMap<ChannelHandlerContext, String> maskName;
    public ConcurrentHashMap<String, ChannelHandlerContext> maskName_reverse;

    public static ConnectMask inst = new ConnectMask();

    public static ConnectMask getInstance(){
        return inst;
    }

    ConnectMask(){
        maskName = new ConcurrentHashMap<>();
        maskName_reverse = new ConcurrentHashMap<>();
    }

    public void addMask(ChannelHandlerContext ctx, String mask){
        maskName.put(ctx, mask);
        maskName_reverse.put(mask, ctx);
    }

    public ChannelHandlerContext checkMask(String mask){
        if (!maskName_reverse.containsKey(mask))
            return null;
        return maskName_reverse.get(mask);
    }

    public void setMaskMap(String mask, ChannelHandlerContext ctx){
        maskName_reverse.remove(mask);
        maskName.remove(ctx);
        addMask(ctx, mask);
    }

    public void removeChannel(ChannelHandlerContext ctx){
        if (maskName.containsKey(ctx)){
            if (maskName_reverse.get(maskName.get(ctx)) == ctx)
                maskName.remove(ctx);
            else{
                maskName_reverse.remove(maskName.get(ctx));
                maskName.remove(ctx);
            }
        }
    }
}
