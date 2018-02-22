package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.server.packet.ok.ErrPacket;

import java.util.Collections;
import java.util.List;

/**
 * COM_STMT_EXECUTE command packet.
 *
 * @author zhangliang
 */
public final class ComStatExecutePacket extends AbstractCommandPacket {
    
    @Override
    public ComStatExecutePacket read(final MySQLPacketPayload mysqlPacketPayload) {
        return this;
    }
    
    @Override
    public List<AbstractMySQLSentPacket> execute() {
        return Collections.<AbstractMySQLSentPacket>singletonList(new ErrPacket(getSequenceId() + 1, 1, "x", "xxxxx", "xxxxxxx"));
    }
}
