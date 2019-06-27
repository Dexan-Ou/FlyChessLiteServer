package org.alayse.marsserver.proxy;

import org.alayse.marsserver.logicserver.GameRoom;
import org.alayse.marsserver.logicserver.ProxySession;
import org.alayse.marsserver.proto.Main;
import org.alayse.marsserver.proto.game.Game;
import org.alayse.marsserver.proto.game.Messagepush;
import org.alayse.marsserver.proto.game.Room;
import org.alayse.marsserver.utils.BaseConstants;
import org.alayse.marsserver.utils.LogUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class NetMsgHeaderHandler extends ChannelInboundHandlerAdapter {

    private static String TARGET_HOST = "http://localhost:8080/";

    public static Logger logger = Logger.getLogger(NetMsgHeaderHandler.class.getName());

    private static Map<Integer, String> CMD_PATH_MAP = new HashMap<>();

    static {
        CMD_PATH_MAP.put(Main.CmdID.CMD_ID_HELLO_VALUE, "/game/hello");
        CMD_PATH_MAP.put(Main.CmdID.CMD_ID_SEND_ACTION_VALUE, "/game/sendaction");
        CMD_PATH_MAP.put(Main.CmdID.CMD_ID_JOINROOM_VALUE,"/game/joinroom");
        CMD_PATH_MAP.put(Main.CmdID.CMD_ID_LEFTROOM_VALUE,"/game/leftroom");
        CMD_PATH_MAP.put(Main.CmdID.CMD_ID_CREATEROOM_VALUE,"/game/createroom");
    }

    private ConcurrentHashMap<ChannelHandlerContext, Long> linkTimeout = new ConcurrentHashMap<>();
    private ConcurrentHashMap<ChannelHandlerContext, String> maskName = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ChannelHandlerContext> maskName_reverse = new ConcurrentHashMap<>();
    private ContextTimeoutChecker checker;
    private MessageDigest m;

    public NetMsgHeaderHandler() {
        super();

        checker = new ContextTimeoutChecker();
        Timer timer = new Timer();
        timer.schedule(checker, 15 * 60 * 1000, 15 * 60 * 1000);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        long time = System.currentTimeMillis();
        m = MessageDigest.getInstance("MD5");
        String s = time + "";
        m.update(s.getBytes(), 0, s.length());
        String mask = new BigInteger(1, m.digest()).toString(16);
        maskName.put(ctx, mask);
        maskName_reverse.put(mask, ctx);
        logger.info("client[" + mask + "] connected! " + ctx.toString());
        linkTimeout.put(ctx, time);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // decode request
            final NetMsgHeader msgXp = new NetMsgHeader();
            final InputStream socketInput = new ByteBufInputStream((ByteBuf) msg);
            boolean ret = msgXp.decode(socketInput);
            IOUtils.closeQuietly(socketInput);

            if(!ret) return;

            linkTimeout.remove(ctx);
            linkTimeout.put(ctx, System.currentTimeMillis());
            logger.info(LogUtils.format("client req, cmdId=%d, seq=%d", msgXp.cmdId, msgXp.seq));

            final ProxySession proxySession = ProxySession.Manager.get(ctx);
            if (proxySession == null) {
            }

            String webCgi = CMD_PATH_MAP.get(msgXp.cmdId);
            switch (msgXp.cmdId) {
                case Main.CmdID.CMD_ID_HELLO_VALUE:
                    InputStream requestDataStream = new ByteArrayInputStream(msgXp.body);
                    InputStream inputStream = doHttpRequest(webCgi, requestDataStream);
                    if (inputStream != null) {
                        msgXp.body = IOUtils.toByteArray(inputStream);
                        IOUtils.closeQuietly(requestDataStream);
                        byte[] respBuf = msgXp.encode();
                        logger.info(LogUtils.format( "client resp, cmdId=HELLO, seq=%d, resp.len=%d", msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(respBuf));
                    }
                    break;
                case Main.CmdID.CMD_ID_CREATEROOM_VALUE:
                    final Main.CreateRoomRequest request_c = Main.CreateRoomRequest.parseFrom(msgXp.body);
                    Main.CreateRoomRequest request_c_new = Main.CreateRoomRequest.newBuilder()
                            .setUser(maskName.get(ctx))
                            .setRoomname(request_c.getRoomname())
                            .setPlayerlimit(request_c.getPlayerlimit())
                            .build();
                    requestDataStream = new ByteArrayInputStream(request_c_new.toByteArray());
                    inputStream = doHttpRequest(webCgi, requestDataStream);
                    if (inputStream != null) {
                        msgXp.body = IOUtils.toByteArray(inputStream);
                        IOUtils.closeQuietly(requestDataStream);
                        byte[] respBuf = msgXp.encode();
                        logger.info(LogUtils.format( "client resp, cmdId=CREATEROOM, seq=%d, resp.len=%d", msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(respBuf));
                    }
                    break;
                case Main.CmdID.CMD_ID_JOINROOM_VALUE:
                    final Main.JoinRoomRequest request_j = Main.JoinRoomRequest.parseFrom(msgXp.body);
                    Main.JoinRoomRequest request_j_new = Main.JoinRoomRequest.newBuilder()
                            .setUser(maskName.get(ctx))
                            .setRoomname(request_j.getRoomname())
                            .build();
                    requestDataStream = new ByteArrayInputStream(request_j_new.toByteArray());
                    inputStream = doHttpRequest(webCgi, requestDataStream);
                    if (inputStream != null) {
                        msgXp.body = IOUtils.toByteArray(inputStream);
                        Main.MsgResponse msgResponse = Main.MsgResponse.parseFrom(inputStream);
                        if (msgResponse.getRetcode() == Main.MsgResponse.Error.ERR_START_VALUE)
                            informGameStart(request_j.getRoomname());
                        IOUtils.closeQuietly(requestDataStream);
                        byte[] respBuf = msgXp.encode();
                        logger.info(LogUtils.format( "client resp, cmdId=JOINROOM, seq=%d, resp.len=%d", msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(respBuf));
                    }
                    break;
                case Main.CmdID.CMD_ID_LEFTROOM_VALUE:
                    final Main.JoinRoomRequest request_l = Main.JoinRoomRequest.parseFrom(msgXp.body);
                    Main.JoinRoomRequest request_l_new = Main.JoinRoomRequest.newBuilder()
                            .setUser(maskName.get(ctx))
                            .setRoomname(request_l.getRoomname())
                            .build();
                    requestDataStream = new ByteArrayInputStream(request_l_new.toByteArray());
                    inputStream = doHttpRequest(webCgi, requestDataStream);
                    if (inputStream != null) {
                        msgXp.body = IOUtils.toByteArray(inputStream);
                        IOUtils.closeQuietly(requestDataStream);
                        byte[] respBuf = msgXp.encode();
                        logger.info(LogUtils.format( "client resp, cmdId=LEFTROOM, seq=%d, resp.len=%d", msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(respBuf));
                    }
                    break;
                case Main.CmdID.CMD_ID_SEND_ACTION_VALUE:
                    final Game.SendActionRequest request_s = Game.SendActionRequest.parseFrom(msgXp.body);
                    Game.SendActionRequest request_s_new = Game.SendActionRequest.newBuilder()
                            .setFrom(maskName.get(ctx))
                            .setContent(request_s.getContent())
                            .setRoom(request_s.getRoom())
                            .build();
                    requestDataStream = new ByteArrayInputStream(request_s_new.toByteArray());
                    inputStream = doHttpRequest(webCgi, requestDataStream);
                    if (inputStream != null) {
                        Game.SendActionProxyResponse response = Game.SendActionProxyResponse.parseFrom(inputStream);
                        if (response != null && response.getResponse().getErrCode() == Game.SendActionResponse.Error.ERR_OK_VALUE) {
                            pushMessage(response.getReceiverList(), response.getMsg());
                        }
                        IOUtils.closeQuietly(requestDataStream);
                        Game.SendActionResponse trueResponse = response.getResponse();
                        msgXp.body = trueResponse.toByteArray();
                        byte[] respBuf = msgXp.encode();
                        logger.info(LogUtils.format("client resp, cmdId=SEND_ACTION, seq=%d, resp.len=%d", msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                        ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(respBuf));
                    }
                    break;
                case NetMsgHeader.CMDID_NOOPING:
                    byte[] respBuf = msgXp.encode();
                    logger.info(LogUtils.format("client resp, cmdId=NOOPING, seq=%d, resp.len=%d", msgXp.seq, msgXp.body == null ? 0 : msgXp.body.length));
                    ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(respBuf));
                    break;
                default:
                    break;
            }
        }catch (Exception e) {
            e.printStackTrace();

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * redirect request to webserver
     * @param path
     * @param data
     * @return
     */
    private InputStream doHttpRequest(String path, InputStream data) {
        final Client client = ClientBuilder.newClient(new ClientConfig());
        final InputStream response = client.target(TARGET_HOST)
                .path(path)
                .request(MediaType.APPLICATION_OCTET_STREAM)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM), InputStream.class);

        return response;
    }

    private void informGameStart(String roomName) {
        try {
            Room.RoomRequest request = Room.RoomRequest.newBuilder()
                    .setRoom(roomName)
                    .build();
            InputStream requestDataStream = new ByteArrayInputStream(request.toByteArray());
            InputStream inputStream = doHttpRequest("/game/informGameStart", requestDataStream);
            if (inputStream != null) {
                Room.RoomResponseProxy response = Room.RoomResponseProxy.parseFrom(inputStream);
                IOUtils.closeQuietly(requestDataStream);
                Game.MessagePushProxy messagePushProxy = Game.MessagePushProxy.newBuilder()
                        .setRoom(roomName)
                        .setContent(response.getContent())
                        .setNextplayer(response.getNextplayer())
                        .build();
                pushMessage(response.getReceiverList(), messagePushProxy);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public class ContextTimeoutChecker extends TimerTask {

        @Override
        public void run() {
            logger.info(LogUtils.format("check longlink alive per 15 minutes, " + this));
            for (ChannelHandlerContext context : linkTimeout.keySet()) {
                if (System.currentTimeMillis() - linkTimeout.get(context) > 15 * 60 * 1000) {
                    String mask = maskName.get(context);
                    Main.JoinRoomRequest request_l_new = Main.JoinRoomRequest.newBuilder()
                            .setUser(mask)
                            .setRoomname("NULL")
                            .build();
                    InputStream requestDataStream = new ByteArrayInputStream(request_l_new.toByteArray());
                    doHttpRequest(CMD_PATH_MAP.get(Main.CmdID.CMD_ID_LEFTROOM_VALUE), requestDataStream);
                    maskName.remove(context);
                    maskName_reverse.remove(mask);
                    linkTimeout.remove(context);
                    logger.info(LogUtils.format("%s expired, deleted.", context.channel().toString()));
                }
            }
        }
    }

    public void pushMessage(List<String> playerList, Game.MessagePushProxy msg) {
        new Pusher(playerList, msg).start();
    }

    private class Pusher extends Thread {
        private List<String> playerList;
        private Messagepush.MessagePush msg_nonextplayer;
        private Messagepush.MessagePush msg_nextplayer;
        private String nextPlayer;

        public Pusher(List<String> playerList, Game.MessagePushProxy msg) {
            this.playerList = playerList;
            this.nextPlayer = msg.getNextplayer();
            this.msg_nonextplayer = Messagepush.MessagePush.newBuilder()
                    .setRoom(msg.getRoom())
                    .setContent(msg.getContent())
                    .setNextplayer(0)
                    .build();
            this.msg_nextplayer = Messagepush.MessagePush.newBuilder()
                    .setRoom(msg.getRoom())
                    .setContent(msg.getContent())
                    .setNextplayer(1)
                    .build();
        }
        @Override
        public void run() {
            try {
                NetMsgHeader msgXp = new NetMsgHeader();
                msgXp.cmdId = BaseConstants.MESSAGE_PUSH;

                for (String mask : playerList) {
                    if (mask.equals(this.nextPlayer))
                        msgXp.body = this.msg_nextplayer.toByteArray();
                    else
                        msgXp.body = this.msg_nonextplayer.toByteArray();
                    final ChannelHandlerContext ctx = maskName_reverse.get(mask);
                    ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(msgXp.encode()));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

