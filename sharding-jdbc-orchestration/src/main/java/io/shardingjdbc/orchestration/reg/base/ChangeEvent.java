package io.shardingjdbc.orchestration.reg.base;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data changed event.
 *
 * @author junxiong
 */
@Getter
public class ChangeEvent {
    
    private final ChangeType changeType;
    
    private final Optional<ChangeData> changeData;
    
    public ChangeEvent(final ChangeType changeType, final ChangeData changeData) {
        this.changeType = changeType;
        this.changeData = Optional.fromNullable(changeData);
    }
    
    /**
     * Data changed type.
     */
    public enum ChangeType {
        
        UPDATED, DELETED, UNKNOWN
    }
    
    /**
     * Changed data.
     */
    @RequiredArgsConstructor
    @Getter
    public static class ChangeData {
        
        private final String key;
        
        private final String value;
    }
}
