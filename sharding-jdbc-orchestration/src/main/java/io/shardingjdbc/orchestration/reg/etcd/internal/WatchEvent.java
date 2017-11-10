package io.shardingjdbc.orchestration.reg.etcd.internal;

import io.shardingjdbc.orchestration.reg.etcd.internal.stub.Event;
import lombok.Builder;
import lombok.Value;


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

    public static WatchEvent of(Event event) {
        return null;
    }

    public enum WatchEventType {
        /**
         * UPDATE
         */
        UPDATE,

        /**
         * DELETE
         */
        DELETE
    }

}
