package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.server.constant.StatusFlag;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.ok.OKPacket;

import java.util.Collections;
import java.util.List;

/**
 * COM_QUIT command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-quit.html">COM_QUIT</a>
 *
 * @author zhangliang
 */
public final class ComQuitPacket extends AbstractCommandPacket {
    
    @Override
    public ComQuitPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        return this;
    }
    
    @Override
    public List<AbstractMySQLSentPacket> execute() {
        return Collections.<AbstractMySQLSentPacket>singletonList(new OKPacket(getSequenceId() + 1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
}
