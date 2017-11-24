package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.shardingjdbc.orchestration.reg.base.ChangeEvent;
import lombok.Builder;
import lombok.Value;
import mvccpb.Kv.Event;

/**
 * @author junxiong
 */
@Builder
@Value
public class WatchEvent {
    
    private ChangeEvent.ChangeType watchEventType;
    
    private String key;
    
    private String value;
    
    public static WatchEvent of(final Event event) {
        if (Event.EventType.DELETE == event.getType()) {
            return WatchEvent.builder()
                    .watchEventType(ChangeEvent.ChangeType.DELETED)
                    .key(event.getKv().getKey().toStringUtf8())
                    .value(event.getKv().getValue().toStringUtf8())
                    .build();
        } else if (Event.EventType.PUT == event.getType()) {
            return WatchEvent.builder()
                    .watchEventType(ChangeEvent.ChangeType.UPDATED)
                    .key(event.getKv().getKey().toStringUtf8())
                    .value(event.getKv().getValue().toStringUtf8())
                    .build();
        } else {
            return WatchEvent.builder()
                    .watchEventType(ChangeEvent.ChangeType.UNKNOWN)
                    .key(event.getKv().getKey().toStringUtf8())
                    .value(event.getKv().getValue().toStringUtf8())
                    .build();
        }
    }
}
