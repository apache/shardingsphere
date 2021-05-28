package org.apache.shardingsphere.governance.core.lock.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lock ack.
 */
@RequiredArgsConstructor
@Getter
public enum LockAck {
    
    LOCKED("LOCKED"), UNLOCKED("UNLOCKED");
    
    private final String value;
}
