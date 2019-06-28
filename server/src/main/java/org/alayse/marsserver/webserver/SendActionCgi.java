package org.alayse.marsserver.webserver;

import org.alayse.marsserver.game.GameStatus;
import org.alayse.marsserver.logicserver.GameRoom;
import org.alayse.marsserver.proto.game.Game;
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

@Path("/game/sendaction")
public class SendActionCgi {

    Logger logger = Logger.getLogger(SendActionCgi.class.getName());

    @POST()
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response sendaction(InputStream is) {
        try {
            final Game.SendActionRequest request = Game.SendActionRequest.parseFrom(is);

            logger.info(LogUtils.format("request from user=%s token=%s, content=%s to room=%s", request.getFrom(), request.getAccessToken(), request.getContent(), request.getRoom()));

            int retCode = Game.SendActionResponse.Error.ERR_OK_VALUE;
            String errMsg = "congratulations, " + request.getFrom();

            GameStatus changelog = GameRoom.getInstance().roomList.get(request.getRoom()).runOneRound(request.getContent());
            final Game.SendActionProxyResponse response = Game.SendActionProxyResponse.newBuilder()
                    .setResponse(Game.SendActionResponse.newBuilder()
                            .setErrCode(retCode)
                            .setErrMsg(errMsg)
                            .build())
                    .setMsg(Game.MessagePushProxy.newBuilder()
                            .setContent(changelog.content)
                            .setRoom(request.getRoom())
                            .setNextplayer(changelog.nextPlayer)
                            .build())
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
