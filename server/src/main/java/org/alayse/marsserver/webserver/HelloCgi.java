package org.alayse.marsserver.webserver;

import org.alayse.marsserver.game.GameStatus;
import org.alayse.marsserver.logicserver.GameRoom;
import org.alayse.marsserver.proto.Main;
import org.alayse.marsserver.proto.game.Game;
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

@Path("/game/hello")
public class HelloCgi {
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
    Logger logger = Logger.getLogger(HelloCgi.class.getName());

    @POST()
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    public Response hello(InputStream is) {
        try {
            final Main.HelloRequest request = Main.HelloRequest.parseFrom(is);

            logger.info(LogUtils.format("request hello, check username=%s", request.getAccessToken()));

            String roomName = GameRoom.getInstance().getPlayerRoom(request.getAccessToken());
            String content = "NULL";

            final Game.MessagePushProxy response = Game.MessagePushProxy.newBuilder()
                    .setRoom(roomName)
                    .setContent(content)
                    .setNextplayer("")
                    .build();

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
