package org.alayse.marsserver.webserver;

import org.alayse.marsserver.logicserver.GameRoom;
import org.alayse.marsserver.proto.Main;
import org.alayse.marsserver.utils.LogUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/game/getroomlist")
public class GetRoomListCgi {
    Logger logger = Logger.getLogger(GetRoomListCgi.class.getName());

    private boolean checkType(int roomStatus, int type){
        if (type == Main.RoomListRequest.FilterType.DEFAULT_VALUE)
            return true;
        if (type == Main.RoomListRequest.FilterType.ALL_VALUE)
            return true;
        if (type == Main.RoomListRequest.FilterType.EMPTY_VALUE && roomStatus < 1)
            return true;
        if (type == Main.RoomListRequest.FilterType.FULL_VALUE && roomStatus > 0)
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
                if (checkType(GameRoom.getInstance().roomList.get(key).getRoomStatus().status, request.getType())){
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
            logger.info(LogUtils.format("request invalid %s", e));
        }

        return null;
    }
}
