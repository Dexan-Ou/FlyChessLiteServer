package org.alayse.marsserver.webserver;

import org.alayse.marsserver.game.roomStatus;
import org.alayse.marsserver.logicserver.GameRoom;
import org.alayse.marsserver.proto.Main;
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

@Path("/game/joinroom")
public class JoinRoomCgi {
    Logger logger = Logger.getLogger(JoinRoomCgi.class.getName());

    @POST()
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response joinroom(InputStream is) {
        try {
            final Main.JoinRoomRequest request = Main.JoinRoomRequest.parseFrom(is);

            logger.info(LogUtils.format("request from user=%s, token=%s, join room=%s", request.getUser(), request.getAccessToken(), request.getRoomname()));

            roomStatus rs = GameRoom.getInstance().joinRoom(request.getAccessToken(),request.getRoomname());
            int retCode = rs.status;
            String errMsg = "something wrong, " + request.getUser();
            if (retCode >= 0){
                errMsg = rs.colorMap.get(request.getAccessToken()) + "";
            }

            final Main.MsgResponse response = Main.MsgResponse.newBuilder()
                    .setRetcode(retCode)
                    .setErrmsg(errMsg)
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
