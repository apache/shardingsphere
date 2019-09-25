package info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * MySQL handshake initializetion packet.
 *
 * https://github.com/mysql/mysql-server/blob/5.7/sql/auth/sql_authentication.cc
 * Bytes       Content
 * -----       ----
 * 1           protocol version (always 10)
 * n           server version string, \0-terminated
 * 4           thread id
 * 8           first 8 bytes of the plugin provided data (scramble)
 * 1           \0 byte, terminating the first part of a scramble
 * 2           server capabilities (two lower bytes)
 * 1           server character set
 * 2           server status
 * 2           server capabilities (two upper bytes)
 * 1           length of the scramble
 * 10          reserved, always 0
 * n           rest of the plugin provided data (at least 12 bytes)
 * 1           \0 byte, terminating the second part of a scramble
 *
 * @author avalon566
 * @author yangyi
 */
@Data
public final class HandshakeInitializationPacket extends AbstractPacket {
    
    private byte protocolVersion = 0x0a;
    private String serverVersion;
    private long threadId;
    private byte[] scramble;
    private int serverCapabilities;
    private byte serverCharsetSet;
    private int serverStatus;
    private int serverCapabilities2;
    private byte[] restOfScramble;
    private String authPluginName;

    @Override
    public void fromByteBuf(final ByteBuf data) {
        protocolVersion = DataTypesCodec.readByte(data);
        serverVersion = DataTypesCodec.readNullTerminatedString(data);
        threadId = DataTypesCodec.readInt(data);
        scramble = DataTypesCodec.readBytes(8, data);
        readTerminated(data);
        serverCapabilities = DataTypesCodec.readShort(data);
        if (data.isReadable()) {
            serverCharsetSet = DataTypesCodec.readByte(data);
            serverStatus = DataTypesCodec.readShort(data);
            serverCapabilities2 = DataTypesCodec.readShort(data);
            int capabilities = (serverCapabilities2 << 16) | serverCapabilities;
            DataTypesCodec.readByte(data);
            DataTypesCodec.readBytes(10, data);
            if ((capabilities & 0x00008000) != 0) {
                restOfScramble = DataTypesCodec.readBytes(12, data);
            }
            readTerminated(data);
            if ((capabilities & 0x00080000) != 0) {
                authPluginName = DataTypesCodec.readNullTerminatedString(data);
            }
        }
    }
    
    private void readTerminated(final ByteBuf data) {
        DataTypesCodec.readByte(data);
    }
}
