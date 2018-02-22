package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.server.packet.MySQLPacketPayload;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;
import io.shardingjdbc.server.packet.ok.ErrPacket;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Unsupported command packet.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class UnsupportedCommandPacket extends AbstractCommandPacket {
    
    private static final int ERROR_CODE = 0xcc;
    
    private static final String SQL_STATE_MARKER = "x";
    
    private static final String SQL_STATE = "xxxxx";
    
    private static final String ERROR_MESSAGE = "Unsupported command packet '%s'.";
    
    private final CommandPacketType type;
    
    @Override
    public UnsupportedCommandPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        return this;
    }
    
    @Override
    public List<AbstractMySQLSentPacket> execute() {
        return Collections.<AbstractMySQLSentPacket>singletonList(new ErrPacket(getSequenceId() + 1, ERROR_CODE, SQL_STATE_MARKER, SQL_STATE, String.format(ERROR_MESSAGE, type)));
    }
}
