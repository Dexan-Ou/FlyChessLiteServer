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

@Path("/game/createroom")
public class CreateRoomCgi {
    Logger logger = Logger.getLogger(CreateRoomCgi.class.getName());

    @POST()
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response createroom(InputStream is) {
        try {
            final Main.CreateRoomRequest request = Main.CreateRoomRequest.parseFrom(is);

            logger.info(LogUtils.format("request from user=%s, token=%s, create room=%s", request.getUser(), request.getAccessToken(), request.getRoomname()));

            int retCode = Main.MsgResponse.Error.ERR_OK_VALUE;
            String errMsg = "congratulations, " + request.getUser();

            if (!GameRoom.getInstance().createRoom(request.getAccessToken(),request.getRoomname(), request.getPlayerlimit())){
                retCode = Main.MsgResponse.Error.ERR_FAIL_VALUE;
                errMsg = "congratulations, " + request.getUser();
            }
            if (GameRoom.getInstance().roomList.get(request.getRoomname()).getPlayerSize()>=request.getPlayerlimit())
                retCode = Main.MsgResponse.Error.ERR_START_VALUE;

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
