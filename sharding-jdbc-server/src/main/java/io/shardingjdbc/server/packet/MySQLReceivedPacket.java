package io.shardingjdbc.server.packet;

/**
 * MySQL received packet.
 * 
 * @author zhangliang 
 */
public abstract class MySQLReceivedPacket extends MySQLPacket {
    
    /**
     * Read packet from byte buffer.
     * 
     * @param mysqlPacketPayload packet payload to be read
     * 
     * @return instance of received packet
     */
    public abstract MySQLReceivedPacket read(final MySQLPacketPayload mysqlPacketPayload);
}
