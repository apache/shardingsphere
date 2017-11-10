package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.shardingjdbc.orchestration.reg.etcd.internal.stub.*;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EtcdClientImpl client
 * <p>
 * TODO add retry support
 *
 * @author junxiong
 */
@Slf4j
public class EtcdClientImpl implements EtcdClient, AutoCloseable {
    private LeaseGrpc.LeaseBlockingStub leaseBlockingStub;
    private KVGrpc.KVBlockingStub kvBlockingStub;
    private WatchGrpc.WatchStub watchStub;

    private AtomicBoolean closed = new AtomicBoolean(false);
    private ConcurrentMap<Long, Watcher> watchers = Maps.newConcurrentMap();

    /**
     * construct a etcd client from grpc channel
     *
     * @param channel Channel
     */
    EtcdClientImpl(Channel channel) {
        leaseBlockingStub = LeaseGrpc.newBlockingStub(channel);
        kvBlockingStub = KVGrpc.newBlockingStub(channel);
        watchStub = WatchGrpc.newStub(channel);
    }

    @Override
    public Optional<String> get(String key) {
        final RangeRequest request = RangeRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(key))
                .build();
        final RangeResponse response = kvBlockingStub.range(request);
        final List<io.shardingjdbc.orchestration.reg.etcd.internal.stub.KeyValue> keyValues = response.getKvsList();
        return keyValues.isEmpty() ? Optional.<String>absent() : Optional.of(keyValues.get(0).getValue().toStringUtf8());
    }

    @Override
    public Optional<List<EtcdClient.KeyValue>> list(String dir) {
        final RangeRequest request = RangeRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(dir))
                .setRangeEnd(prefix(dir))
                .build();
        final RangeResponse response = kvBlockingStub.range(request);
        final List<io.shardingjdbc.orchestration.reg.etcd.internal.stub.KeyValue> keyValues = response.getKvsList();
        List<EtcdClient.KeyValue> result = Lists.newArrayList();
        for (io.shardingjdbc.orchestration.reg.etcd.internal.stub.KeyValue keyValue : keyValues) {
            result.add(EtcdClient.KeyValue.builder()
                    .key(keyValue.getKey().toStringUtf8())
                    .value(keyValue.getValue().toStringUtf8())
                    .build());
        }
        return Optional.of(result);
    }

    @Override
    public Optional<String> put(String key, String value) {
        final PutRequest request = PutRequest.newBuilder()
                .setPrevKv(true)
                .setKey(ByteString.copyFromUtf8(key))
                .setValue(ByteString.copyFromUtf8(value))
                .build();
        final PutResponse response = kvBlockingStub.put(request);
        return Optional.of(response.getPrevKv().getValue().toStringUtf8());
    }

    @Override
    public Optional<String> put(@NonNull String key, @NonNull String value, long ttl) {
        final Optional<Long> leaseId = lease(ttl);
        if (!leaseId.isPresent()) {
            throw new RegException("Unable to set up heat beat for key %s", key);
        }
        final PutRequest request = PutRequest.newBuilder()
                .setPrevKv(true)
                .setLease(leaseId.get())
                .setKey(ByteString.copyFromUtf8(key))
                .setValue(ByteString.copyFromUtf8(value))
                .build();
        final PutResponse response = kvBlockingStub.put(request);
        return Optional.of(response.getPrevKv().getValue().toStringUtf8());
    }

    @Override
    public Optional<List<String>> delete(String key) {
        final DeleteRangeRequest request = DeleteRangeRequest.newBuilder()
                .build();
        DeleteRangeResponse response = kvBlockingStub.deleteRange(request);
        List<String> deletedKeys = Lists.newArrayList();
        for (io.shardingjdbc.orchestration.reg.etcd.internal.stub.KeyValue keyValue : response.getPrevKvsList()) {
            deletedKeys.add(keyValue.getKey().toStringUtf8());
        }
        return Optional.of(deletedKeys);
    }

    @Override
    public Optional<Long> lease(long ttl) {
        final LeaseGrantRequest request = LeaseGrantRequest.newBuilder().setTTL(ttl).build();
        final LeaseGrantResponse response = leaseBlockingStub.leaseGrant(request);
        return Optional.of(response.getID());
    }

    private ByteString prefix(String key) {
        final byte[] noPrefix = {0};
        byte[] endKey = key.getBytes().clone();
        for (int i = endKey.length - 1; i >= 0; i--) {
            if (endKey[i] < 0xff) {
                endKey[i] = (byte) (endKey[i] + 1);
                return ByteString.copyFrom(Arrays.copyOf(endKey, i + 1));
            }
        }

        return ByteString.copyFrom(noPrefix);
    }

    @Override
    public Optional<Watcher> watch(final @NonNull String key) {
        final WatcherImpl watcher = new WatcherImpl(key);
        final StreamObserver<WatchResponse> responseStream = new StreamObserver<WatchResponse>() {
            @Override
            public void onNext(WatchResponse response) {
                if (response.getCanceled()) {
                    watchers.remove(response.getWatchId());
                } else if (response.getCreated()) {
                    final long id = response.getWatchId();
                    watcher.setId(id);
                    watchers.put(id, watcher);
                } else {
                    for (final Event event : response.getEventsList()) {
                        final WatchEvent watchEvent = WatchEvent.of(event);
                        watcher.notify(watchEvent);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                // TODO retry watch later
                throw new RegException(new Exception(t));
            }

            @Override
            public void onCompleted() {
                log.info("etcd watch complemented");
            }
        };
        final StreamObserver<WatchRequest> requestStream = watchStub.watch(responseStream);
        final WatchCreateRequest createWatchRequest = WatchCreateRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(key))
                .build();
        final WatchRequest request = WatchRequest.newBuilder()
                .setCreateRequest(createWatchRequest)
                .build();
        requestStream.onNext(request);
        return Optional.of((Watcher) watcher);
    }

    @Override
    public void close() throws Exception {
        closed.compareAndSet(true, false);
    }


}
