package io.shardingjdbc.orchestration.reg.etcd.internal.watcher;

import etcdserverpb.Rpc;
import etcdserverpb.Rpc.WatchResponse;
import io.grpc.stub.StreamObserver;
import io.shardingjdbc.orchestration.reg.listener.DataChangedEvent;
import io.shardingjdbc.orchestration.reg.listener.EventListener;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import lombok.RequiredArgsConstructor;
import mvccpb.Kv.Event;

/**
 * Watch stream observer.
 *
 * @author junxiong
 */
@RequiredArgsConstructor
public final class EtcdWatchStreamObserver implements StreamObserver<WatchResponse> {
    
    private final EventListener eventListener;
    
    @Override
    public void onNext(final Rpc.WatchResponse response) {
        if (response.getCanceled() || response.getCreated()) {
            return;
        }
        for (Event event : response.getEventsList()) {
            eventListener.onChange(new DataChangedEvent(getEventType(event), event.getKv().getKey().toStringUtf8(), event.getKv().getValue().toStringUtf8()));
        }
    }
    
    private DataChangedEvent.Type getEventType(final Event event) {
        switch (event.getType()) {
            case PUT:
                return DataChangedEvent.Type.UPDATED;
            case DELETE:
                return DataChangedEvent.Type.DELETED;
            default:
                return DataChangedEvent.Type.IGNORED;
        }
    }
    
    @Override
    public void onError(final Throwable throwable) {
        // TODO retry watch later
        throw new RegException(new Exception(throwable));
    }
    
    @Override
    public void onCompleted() {
    }
}
