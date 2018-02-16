package io.shardingjdbc.server.packet;

/**
 * MySQL send packet.
 * 
 * @author zhangliang 
 */
public abstract class MySQLSentPacket extends MySQLPacket {
    
    /**
     * Write packet to byte buffer.
     * 
     * @param mysqlPacketPayload packet payload to be write
     */
    public abstract void write(final MySQLPacketPayload mysqlPacketPayload);
}
