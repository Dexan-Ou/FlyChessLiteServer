package org.alayse.marsserver.webserver;

import org.alayse.marsserver.logicserver.GameRoom;
import org.alayse.marsserver.proto.Main;
import org.alayse.marsserver.utils.LogUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/game/getroomlist")
public class GetRoomListCgi {
    static {
        Properties pro = new Properties();
        pro.put("log4j.rootLogger", "DEBUG,stdout,R");

        pro.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
        pro.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
        pro.put("log4j.appender.stdout.layout.ConversionPattern", "%5p [%t] (%F:%L) - %m%n");

        pro.put("log4j.appender.R", "org.apache.log4j.DailyRollingFileAppender");
        pro.put("log4j.appender.R.Threshold", "INFO");
        pro.put("log4j.appender.R.File", "${user.home}/logs/mars/info_webserver.log");
        pro.put("log4j.appender.R.DatePattern", ".yyyy-MM-dd");
        pro.put("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
        pro.put("log4j.appender.R.layout.ConversionPattern", "[%d{HH:mm:ss:SSS}] [%p] - %l - %m%n");

        PropertyConfigurator.configure(pro);
    }
    Logger logger = Logger.getLogger(GetRoomListCgi.class.getName());

    private boolean checkType(int player, int playerLimit, int type){
        if (type == Main.RoomListRequest.FilterType.DEFAULT_VALUE)
            return true;
        if (type == Main.RoomListRequest.FilterType.ALL_VALUE)
            return true;
        if (type == Main.RoomListRequest.FilterType.EMPTY_VALUE && player < playerLimit)
            return true;
        if (type == Main.RoomListRequest.FilterType.FULL_VALUE && player >= playerLimit)
            return true;
        return false;
    }

    @POST()
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response getroomlist(InputStream is) {
        try {
            final Main.RoomListRequest request = Main.RoomListRequest.parseFrom(is);

            logger.info(LogUtils.format("request room list, roomList.size=%d, type=%d", GameRoom.getInstance().roomList.mappingCount(), request.getType()));

            List<Main.Room> roomList = new LinkedList<>();
            for (String key: GameRoom.getInstance().roomList.keySet()){
                int player = GameRoom.getInstance().roomList.get(key).getPlayerSize();
                int playerLimit = GameRoom.getInstance().roomList.get(key).getPlayerLimit();
                if (checkType(player, playerLimit, request.getType())){
                    roomList.add(Main.Room.newBuilder()
                                    .setName(key)
                                    .setPlayer(player)
                                    .setPlayerlimit(playerLimit)
                                    .build());
                }
            }

            final Main.RoomListResponse response = Main.RoomListResponse.newBuilder()
                    .addAllList(roomList).build();

            final StreamingOutput stream = new StreamingOutput() {
                public void write(OutputStream os) throws IOException {
                    response.writeTo(os);
                }
            };
            return Response.ok(stream).build();

        } catch (Exception e) {
            logger.info(LogUtils.format("request invalid", e));
        }

        return null;
    }
}
