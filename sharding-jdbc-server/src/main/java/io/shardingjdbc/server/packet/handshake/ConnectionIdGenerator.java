package io.shardingjdbc.server.packet.handshake;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Connection ID generator.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ConnectionIdGenerator {
    
    @Getter
    private static ConnectionIdGenerator instance = new ConnectionIdGenerator();
    
    private int currentId;
    
    /**
     * Get next connection ID.
     * 
     * @return next connection ID
     */
    public synchronized int nextId() {
        if (currentId >= Integer.MAX_VALUE) {
            currentId = 0;
        }
        return ++currentId;
    }
}
