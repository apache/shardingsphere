package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;

/**
 * COM_QUERY response field count packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query-response.html">COM_QUERY field count</a>
 *
 * @author zhangliang
 */
public final class FieldCountPacket extends AbstractMySQLSentPacket {
    
    private final long columnCount;
    
    public FieldCountPacket(final int sequenceId, final long columnCount) {
        setSequenceId(sequenceId);
        this.columnCount = columnCount;
    }
    
    @Override
    public void write(final MySQLPacketPayload mysqlPacketPayload) {
        mysqlPacketPayload.writeIntLenenc(columnCount);
    }
}
