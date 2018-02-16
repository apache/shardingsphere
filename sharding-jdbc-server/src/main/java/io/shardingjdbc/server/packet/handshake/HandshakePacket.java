package io.shardingjdbc.server.packet.handshake;

import io.shardingjdbc.server.constant.CapabilityFlag;
import io.shardingjdbc.server.constant.ServerInfo;
import io.shardingjdbc.server.constant.StatusFlag;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLSentPacket;
import lombok.Getter;

/**
 * Handshake packet protocol.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::Handshake">Handshake</a>
 * 
 * @author zhangliang
 */
@Getter
public class HandshakePacket extends MySQLSentPacket {
    
    private final int protocolVersion = ServerInfo.PROTOCOL_VERSION;
    
    private final String serverVersion = ServerInfo.SERVER_VERSION;
    
    private final int capabilityFlagsLower = CapabilityFlag.calculateHandshakeCapabilityFlagsLower();
    
    private final int characterSet = ServerInfo.CHARSET;
    
    private final StatusFlag statusFlag = StatusFlag.SERVER_STATUS_AUTOCOMMIT;
    
    private final int capabilityFlagsUpper = CapabilityFlag.calculateHandshakeCapabilityFlagsUpper();
    
    private final int connectionId;
    
    private final AuthPluginData authPluginData;
    
    public HandshakePacket(final int connectionId, AuthPluginData authPluginData) {
        setSequenceId(0);
        this.connectionId = connectionId;
        this.authPluginData = authPluginData;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(protocolVersion);
        mysqlPacketPayload.writeStringNul(serverVersion);
        mysqlPacketPayload.writeInt4(connectionId);
        mysqlPacketPayload.writeStringNul(new String(authPluginData.getAuthPluginDataPart1()));
        mysqlPacketPayload.writeInt2(capabilityFlagsLower);
        mysqlPacketPayload.writeInt1(ServerInfo.CHARSET);
        mysqlPacketPayload.writeInt2(statusFlag.getValue());
        mysqlPacketPayload.writeInt2(capabilityFlagsUpper);
        mysqlPacketPayload.writeInt1(0);
        mysqlPacketPayload.writeReserved(10);
        mysqlPacketPayload.writeStringNul(new String(authPluginData.getAuthPluginDataPart2()));
    }
}
