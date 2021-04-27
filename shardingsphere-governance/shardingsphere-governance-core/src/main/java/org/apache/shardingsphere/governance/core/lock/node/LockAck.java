package org.apache.shardingsphere.governance.core.lock.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lock ack.
 */
@AllArgsConstructor
@Getter
public enum LockAck {
    
    LOCKED("LOCKED"), UNLOCKED("UNLOCKED");
    
    private String value;
}
