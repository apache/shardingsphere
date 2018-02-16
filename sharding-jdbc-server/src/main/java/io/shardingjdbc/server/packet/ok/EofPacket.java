package io.shardingjdbc.server.packet.ok;

import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLSentPacket;
import lombok.Getter;

/**
 * EOF packet protocol.
 * @see <a href="https://dev.mysql.com/doc/internals/en/packet-EOF_Packet.html">EOF Packet</a>
 * 
 * @author zhangliang 
 */
@Getter
public class EofPacket extends MySQLSentPacket {
    
    private static final int HEADER = 0xfe;
    
    private final int warnings;
    
    private final int statusFlags;
    
    public EofPacket(final int sequenceId, final int warnings, final int statusFlags) {
        setSequenceId(sequenceId);
        this.warnings = warnings;
        this.statusFlags = statusFlags;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeInt1(HEADER);
        mysqlPacketPayload.writeInt2(warnings);
        mysqlPacketPayload.writeInt2(statusFlags);
    }
}
