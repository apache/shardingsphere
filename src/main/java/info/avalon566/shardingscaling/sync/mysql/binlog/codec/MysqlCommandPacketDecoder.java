package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth.HandshakeInitializationPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author avalon566
 */
public class MysqlCommandPacketDecoder extends ByteToMessageDecoder {

    private Logger LOGGER = LoggerFactory.getLogger(MysqlCommandPacketDecoder.class);

    private enum States {OkOrError, FieldPacket, RowDataPacket}

    private boolean initiated = false;
    private States expectedState = States.OkOrError;
    private InternalResultSet internalResultSet = null;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) {
        // first packet from server is handshake initialization packet
        if (!initiated) {
            var handshake = new HandshakeInitializationPacket();
            handshake.fromByteBuf(in);
            if (handshake.getProtocolVersion() != 0x0a) {
                throw new UnsupportedOperationException();
            }
            if (!"mysql_native_password".equals(handshake.getAuthPluginName())) {
                throw new UnsupportedOperationException();
            }
            out.add(handshake);
            initiated = true;
            return;
        }
        if (States.FieldPacket.equals(expectedState)) {
            if (-2 != in.getByte(0)) {
                var fieldPacket = new FieldPacket();
                fieldPacket.fromByteBuf(in);
                internalResultSet.getFieldDescriptors().add(fieldPacket);
            } else {
                var eofPacket = new EofPacket();
                eofPacket.fromByteBuf(in);
                expectedState = States.RowDataPacket;
            }
            return;
        }
        if (States.RowDataPacket.equals(expectedState)) {
            if (-2 != in.getByte(0)) {
                var rowDataPacket = new RowDataPacket();
                rowDataPacket.fromByteBuf(in);
                internalResultSet.getFieldValues().add(rowDataPacket);
            } else {
                var eofPacket = new EofPacket();
                eofPacket.fromByteBuf(in);
                out.add(internalResultSet);
                expectedState = States.OkOrError;
                internalResultSet = null;
            }
            return;
        }
        if (-1 == in.getByte(0)) {
            var error = new ErrorPacket();
            error.fromByteBuf((ByteBuf) in);
            out.add(error);
        } else if (0 == in.getByte(0)) {
            var ok = new OkPacket();
            ok.fromByteBuf(in);
            out.add(ok);
        } else {
            var resultSetHeaderPacket = new ResultSetHeaderPacket();
            resultSetHeaderPacket.fromByteBuf(in);
            expectedState = States.FieldPacket;
            internalResultSet = new InternalResultSet(resultSetHeaderPacket);
        }
    }
}
