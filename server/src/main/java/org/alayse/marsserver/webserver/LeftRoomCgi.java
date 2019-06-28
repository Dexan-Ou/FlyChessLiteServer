package org.alayse.marsserver.webserver;

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

@Path("/game/leftroom")
public class LeftRoomCgi {
    Logger logger = Logger.getLogger(LeftRoomCgi.class.getName());

    @POST()
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response leftroom(InputStream is) {
        try {
            final Main.JoinRoomRequest request = Main.JoinRoomRequest.parseFrom(is);

            logger.info(LogUtils.format("request from user=%s, token=%s, left room=%s", request.getUser(), request.getAccessToken(), request.getRoomname()));

            int retCode = Main.MsgResponse.Error.ERR_OK_VALUE;
            String errMsg = "congratulations, " + request.getUser();

            GameRoom.getInstance().leftRoom(request.getAccessToken());

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
