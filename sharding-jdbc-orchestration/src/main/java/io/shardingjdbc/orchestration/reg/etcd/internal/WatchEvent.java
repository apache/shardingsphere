package io.shardingjdbc.orchestration.reg.etcd.internal;

import lombok.Builder;
import lombok.Value;
import mvccpb.Kv.Event;

/**
 * @author junxiong
 */
@Builder
@Value
public class WatchEvent {
    
    private WatchEventType watchEventType;
    
    private long id;
    
    private String key;
    
    private String value;

    public static WatchEvent of(final long id, final Event event) {
        if (Event.EventType.DELETE == event.getType()) {
            return WatchEvent.builder()
                    .id(id)
                    .watchEventType(WatchEventType.DELETE)
                    .key(event.getKv().getKey().toStringUtf8())
                    .value(event.getKv().getValue().toStringUtf8())
                    .build();
        } else if (Event.EventType.PUT == event.getType()) {
            return WatchEvent.builder()
                    .id(id)
                    .watchEventType(WatchEventType.UPDATE)
                    .key(event.getKv().getKey().toStringUtf8())
                    .value(event.getKv().getValue().toStringUtf8())
                    .build();
        } else {
            return WatchEvent.builder()
                    .id(id)
                    .watchEventType(WatchEventType.UNKNOWN)
                    .key(event.getKv().getKey().toStringUtf8())
                    .value(event.getKv().getValue().toStringUtf8())
                    .build();
        }
    }
    
    /**
     * Watch event type.
     */
    public enum WatchEventType {
        
        UPDATE, DELETE, UNKNOWN
    }
}
