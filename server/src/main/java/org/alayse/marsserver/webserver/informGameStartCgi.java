package org.alayse.marsserver.webserver;

import org.alayse.marsserver.logicserver.GameRoom;
import org.alayse.marsserver.proto.game.Room;
import org.alayse.marsserver.utils.LogUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/game/informGameStart")
public class informGameStartCgi {
    Logger logger = Logger.getLogger(informGameStartCgi.class.getName());

    @POST()
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response informGameStart(InputStream is) {
        try {
            final Room.RoomRequest request = Room.RoomRequest.parseFrom(is);

            logger.info(LogUtils.format("inform game start room=%s", request.getRoom()));

            GameRoom.getInstance().roomList.get(request.getRoom()).startGame();

            final Room.RoomResponseProxy response = Room.RoomResponseProxy.newBuilder()
                    .setContent("")
                    .setNextplayer("")
                    .addAllReceiver(GameRoom.getInstance().roomList.get(request.getRoom()).getRoomStatus().colorMap.keySet())
                    .build();

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
