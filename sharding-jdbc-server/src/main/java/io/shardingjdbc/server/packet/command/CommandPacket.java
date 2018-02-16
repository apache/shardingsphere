package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.server.packet.MySQLReceivedPacket;
import io.shardingjdbc.server.packet.MySQLSentPacket;

import java.util.List;

/**
 * Command packet.
 *
 * @author zhangliang
 */
public abstract class CommandPacket extends MySQLReceivedPacket {
    
    /**
     * Execute command.
     * 
     * @return result packets to be sent
     */
    public abstract List<MySQLSentPacket> execute();
}
