package info.avalon566.shardingscaling.sync.mysql.binlog;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth.ClientAuthenticationPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth.HandshakeInitializationPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.ErrorPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.OkPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.var;

/**
 * @author avalon566
 */
public class MySQLNegotiateHandler extends ChannelInboundHandlerAdapter {

    private final String username;
    private final String password;
    private final Promise<Object> authResultCallback;

    public MySQLNegotiateHandler(String username, String password, Promise<Object> authResultCallback) {
        this.username = username;
        this.password = password;
        this.authResultCallback = authResultCallback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HandshakeInitializationPacket) {
            var handshake = (HandshakeInitializationPacket)msg;
            var clientAuth = new ClientAuthenticationPacket();
            clientAuth.setSequenceNumber((byte) (handshake.getSequenceNumber() + 1));
            clientAuth.setCharsetNumber((byte) 33);
            clientAuth.setUsername(username);
            clientAuth.setPassword(password);
            clientAuth.setServerCapabilities(handshake.getServerCapabilities());
            // use default database
            clientAuth.setDatabaseName("mysql");
            clientAuth.setScrumbleBuff(joinAndCreateScrumbleBuff(handshake));
            clientAuth.setAuthPluginName(handshake.getAuthPluginName());
            ctx.channel().writeAndFlush(clientAuth);
            return;
        }
        if (msg instanceof OkPacket) {
            ctx.channel().pipeline().remove(this);
            authResultCallback.setSuccess(null);
            return;
        }
        var error = (ErrorPacket)msg;
        ctx.channel().close();
        throw new RuntimeException(error.getMessage());
    }

    private byte[] joinAndCreateScrumbleBuff(HandshakeInitializationPacket handshakePacket) {
        byte[] dest = new byte[handshakePacket.getScramble().length + handshakePacket.getRestOfScramble().length];
        System.arraycopy(handshakePacket.getScramble(), 0, dest, 0, handshakePacket.getScramble().length);
        System.arraycopy(handshakePacket.getRestOfScramble(),
                0, dest, handshakePacket.getScramble().length,
                handshakePacket.getRestOfScramble().length);
        return dest;
    }
}
