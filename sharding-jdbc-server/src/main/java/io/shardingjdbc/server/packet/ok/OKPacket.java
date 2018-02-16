package io.shardingjdbc.server.packet.ok;

import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLSentPacket;
import lombok.Getter;

/**
 * OK packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html">OK Packet</a>
 * 
 * @author zhangliang 
 */
@Getter
public class OKPacket extends MySQLSentPacket {
    
    private static final int HEADER = 0x00;
    
    private final long affectedRows;
    
    private final long lastInsertId;
    
    private final int statusFlags;
    
    private final int warnings;
    
    private final String info;
    
    public OKPacket(final int sequenceId, final long affectedRows, final long lastInsertId, final int statusFlags, final int warnings, final String info) {
        setSequenceId(sequenceId);
        this.affectedRows = affectedRows;
        this.lastInsertId = lastInsertId;
        this.statusFlags = statusFlags;
        this.warnings = warnings;
        this.info = info;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(HEADER);
        mysqlPacketPayload.writeIntLenenc(affectedRows);
        mysqlPacketPayload.writeIntLenenc(lastInsertId);
        mysqlPacketPayload.writeInt2(statusFlags);
        mysqlPacketPayload.writeInt2(warnings);
        mysqlPacketPayload.writeStringEOF(info);
    }
}
