package io.shardingjdbc.server.packet;

/**
 * MySQL received packet.
 * 
 * @author zhangliang 
 */
public abstract class AbstractMySQLReceivedPacket extends AbstractMySQLPacket {
    
    /**
     * Read packet from byte buffer.
     * 
     * @param mysqlPacketPayload packet payload to be read
     * 
     * @return instance of received packet
     */
    public abstract AbstractMySQLReceivedPacket read(final MySQLPacketPayload mysqlPacketPayload);
}
