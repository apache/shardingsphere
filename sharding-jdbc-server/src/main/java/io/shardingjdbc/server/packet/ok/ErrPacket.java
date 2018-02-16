package io.shardingjdbc.server.packet.ok;

import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLSentPacket;
import lombok.Getter;

/**
 * ERR packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html">ERR Packet</a>
 * 
 * @author zhangliang 
 */
@Getter
public class ErrPacket extends MySQLSentPacket {
    
    private static final int HEADER = 0xff;
    
    private final int errorCode;
    
    private final String sqlStateMarker;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    public ErrPacket(final int sequenceId, final int errorCode, final String sqlStateMarker, final String sqlState, final String errorMessage) {
        setSequenceId(sequenceId);
        this.errorCode = errorCode;
        this.sqlStateMarker = sqlStateMarker;
        this.sqlState = sqlState;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(HEADER);
        mysqlPacketPayload.writeInt2(errorCode);
        mysqlPacketPayload.writeStringFix(sqlStateMarker);
        mysqlPacketPayload.writeStringFix(sqlState);
        mysqlPacketPayload.writeStringEOF(errorMessage);
    }
}
