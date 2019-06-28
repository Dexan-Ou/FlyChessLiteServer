package org.alayse.marsserver.webserver;

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

            logger.info(LogUtils.format("request from user=%s, content=%s to room=%s", request.getFrom(), request.getContent(), request.getRoom()));

            int retCode = Game.SendActionResponse.Error.ERR_OK_VALUE;
            String errMsg = "congratulations, " + request.getFrom();

            // String changelog = GameRoom.getInstance().roomList.get(request.getRoom()).runOneRound(request.getContent());
            String changelog = "(0,1,2);(0,2,3);(0,3,4);(0,4,5);(0,5,6);(0,6,7);(0,7,8);(1,-1)";
            String actionArray[] = changelog.split(";");
            // String nextPlayer = actionArray[actionArray.length - 1];//下一个玩家userName
            String nextPlayer = "1";
            String actionSequence = "";//动作序列，用;分割
            for(int i = 0;i < actionArray.length - 1;i ++){
                actionSequence = actionSequence + actionArray[i] + ";";
            }
            logger.info(LogUtils.format("Test 1"));


            final Game.MessagePushProxy boardcastResponse = Game.MessagePushProxy.newBuilder()
                    .setContent(actionSequence)
                    .setRoom(request.getRoom())
                    .setNextplayer(nextPlayer)
                    .build();

            Game.SendActionProxyResponse.Builder responseBuilder = Game.SendActionProxyResponse.newBuilder()
                    .setResponse(Game.SendActionResponse.newBuilder()
                        .setErrCode(retCode)
                        .setErrMsg(errMsg)
                        .build())
                    .setMsg(boardcastResponse);
            for (String user:GameRoom.getInstance().roomList.get(request.getRoom()).getRoomStatus().colorMap.keySet()) {
                responseBuilder.addReceiver(user);
            }
            final Game.SendActionProxyResponse response = responseBuilder.build();
            logger.info(LogUtils.format("Test 2"));

            final StreamingOutput stream = new StreamingOutput() {
                public void write(OutputStream os) throws IOException {
                    response.writeTo(os);
                }
            };
            return Response.ok(stream).build();

        } catch (Exception e) {
            logger.info(LogUtils.format("%s", e));
        }

        return null;
    }
}