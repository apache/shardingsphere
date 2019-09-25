package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

/**
 * MySQL protocol constants.
 *
 * @author yangyi
 */
public final class PacketConstants {
    
    public static final byte PROTOCOL_VERSION = 0x0a;
    
    public static final byte OK_PACKET_MARK = 0x00;
    
    public static final byte EOF_PACKET_MARK = (byte) 0xfe;
    
    public static final byte ERR_PACKET_MARK = (byte) 0xff;
}
