package io.shardingjdbc.orchestration.reg.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data changed event.
 *
 * @author junxiong
 */
@RequiredArgsConstructor
@Getter
public class ChangeEvent {
    
    private final Type eventType;
    
    private final String key;
    
    private final String value;
    
    /**
     * Data changed event type.
     */
    public enum Type {
        
        UPDATED, DELETED, IGNORED
    }
}
