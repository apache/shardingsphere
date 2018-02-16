package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.MySQLSentPacket;

import java.util.List;

/**
 * Text result set row packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-ProtocolText::ResultsetRow">ResultsetRow</a>
 *
 * @author zhangliang
 */
public final class TextResultSetRowPacket extends MySQLSentPacket {
    
    private final List<Object> data;
    
    public TextResultSetRowPacket(final int sequenceId, final List<Object> data) {
        setSequenceId(sequenceId);
        this.data = data;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        for (Object each : data) {
            mysqlPacketPayload.writeStringLenenc(each.toString());
        }
    }
}
