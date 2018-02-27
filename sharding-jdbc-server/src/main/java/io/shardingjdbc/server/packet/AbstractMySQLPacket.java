package io.shardingjdbc.server.packet;

import lombok.Getter;
import lombok.Setter;

/**
 * MySQL packet.
 *
 * @author zhangliang
 */
@Getter
@Setter
public abstract class AbstractMySQLPacket {
    
    public static final int PAYLOAD_LENGTH = 3;
    
    public static final int SEQUENCE_LENGTH = 1;
    
    private int sequenceId;
}
