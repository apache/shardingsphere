package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth.HandshakeInitializationPacket;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.response.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.util.List;

/**
 * MySQL Command Packet decoder
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public class MysqlCommandPacketDecoder extends ByteToMessageDecoder {
    
    private enum States {Initiate, ResponsePacket, FieldPacket, RowDataPacket}
    
    private States currentState = States.Initiate;
    
    private InternalResultSet internalResultSet = null;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // first packet from server is handshake initialization packet
        if (States.Initiate.equals(currentState)) {
            out.add(decodeHandshakeInitializationPacket(in));
            currentState = States.ResponsePacket;
            return;
        }
        if (States.FieldPacket.equals(currentState)) {
            decodeFieldPacket(in);
            return;
        }
        if (States.RowDataPacket.equals(currentState)) {
            decodeRowDataPacket(in, out);
            return;
        }
        decodeResponsePacket(in, out);
    }
    
    private HandshakeInitializationPacket decodeHandshakeInitializationPacket(final ByteBuf in) {
        var result = new HandshakeInitializationPacket();
        result.fromByteBuf(in);
        if (PacketConstants.PROTOCOL_VERSION != result.getProtocolVersion()) {
            throw new UnsupportedOperationException();
        }
        if (!AuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.equals(result.getAuthPluginName())) {
            throw new UnsupportedOperationException();
        }
        return result;
    }
    
    private void decodeFieldPacket(final ByteBuf in) {
        if (PacketConstants.EOF_PACKET_MARK != in.getByte(0)) {
            var fieldPacket = new FieldPacket();
            fieldPacket.fromByteBuf(in);
            internalResultSet.getFieldDescriptors().add(fieldPacket);
        } else {
            var eofPacket = new EofPacket();
            eofPacket.fromByteBuf(in);
            currentState = States.RowDataPacket;
        }
    }
    
    private void decodeRowDataPacket(final ByteBuf in, final List<Object> out) {
        if (PacketConstants.EOF_PACKET_MARK != in.getByte(0)) {
            var rowDataPacket = new RowDataPacket();
            rowDataPacket.fromByteBuf(in);
            internalResultSet.getFieldValues().add(rowDataPacket);
        } else {
            var eofPacket = new EofPacket();
            eofPacket.fromByteBuf(in);
            out.add(internalResultSet);
            currentState = States.ResponsePacket;
            internalResultSet = null;
        }
    }
    
    private void decodeResponsePacket(final ByteBuf in, final List<Object> out) {
        if (PacketConstants.ERR_PACKET_MARK == in.getByte(0)) {
            var error = new ErrorPacket();
            error.fromByteBuf(in);
            out.add(error);
        } else if (PacketConstants.OK_PACKET_MARK == in.getByte(0)) {
            var ok = new OkPacket();
            ok.fromByteBuf(in);
            out.add(ok);
        } else {
            var resultSetHeaderPacket = new ResultSetHeaderPacket();
            resultSetHeaderPacket.fromByteBuf(in);
            currentState = States.FieldPacket;
            internalResultSet = new InternalResultSet(resultSetHeaderPacket);
        }
    }
}
