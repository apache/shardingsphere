package io.shardingjdbc.server.packet.handshake;

import io.shardingjdbc.server.constant.CapabilityFlag;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLReceivedPacket;
import lombok.Getter;

/**
 *
 * Handshake response above MySQL 4.1 packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse41">HandshakeResponse41</a>
 * 
 * @author zhangliang
 */
@Getter
public final class HandshakeResponse41Packet extends MySQLReceivedPacket {
    
    private int capabilityFlags;
    
    private int maxPacketSize;
    
    private int characterSet;
    
    private String username;
    
    private byte[] authResponse;
    
    private String database;
    
    @Override
    public HandshakeResponse41Packet read(final MySQLPacketPayload mysqlPacketPayload) {
        setSequenceId(mysqlPacketPayload.readInt1());
        capabilityFlags = mysqlPacketPayload.readInt4();
        maxPacketSize = mysqlPacketPayload.readInt4();
        characterSet = mysqlPacketPayload.readInt1();
        mysqlPacketPayload.skipReserved(23);
        username = mysqlPacketPayload.readStringNul();
        readAuthResponse(mysqlPacketPayload);
        readDatabase(mysqlPacketPayload);
        return this;
    }
    
    private void readAuthResponse(final MySQLPacketPayload mysqlPacketPayload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA.getValue())) {
            authResponse = mysqlPacketPayload.readStringLenenc().getBytes();
        } else if (0 != (capabilityFlags & CapabilityFlag.CLIENT_SECURE_CONNECTION.getValue())) {
            int length = mysqlPacketPayload.readInt1();
            authResponse = mysqlPacketPayload.readStringFix(length).getBytes();
        } else {
            authResponse = mysqlPacketPayload.readStringNul().getBytes();
        }
    }
    
    private void readDatabase(final MySQLPacketPayload mysqlPacketPayload) {
        if (0 != (capabilityFlags & CapabilityFlag.CLIENT_CONNECT_WITH_DB.getValue())) {
            database = mysqlPacketPayload.readStringNul();
        }
    }
}
