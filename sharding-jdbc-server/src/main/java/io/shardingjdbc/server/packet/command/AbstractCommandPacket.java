package io.shardingjdbc.server.packet.command;

import io.shardingjdbc.server.packet.AbstractMySQLReceivedPacket;
import io.shardingjdbc.server.packet.AbstractMySQLSentPacket;

import java.util.List;

/**
 * Command packet.
 *
 * @author zhangliang
 */
public abstract class AbstractCommandPacket extends AbstractMySQLReceivedPacket {
    
    /**
     * Execute command.
     * 
     * @return result packets to be sent
     */
    public abstract List<AbstractMySQLSentPacket> execute();
}
