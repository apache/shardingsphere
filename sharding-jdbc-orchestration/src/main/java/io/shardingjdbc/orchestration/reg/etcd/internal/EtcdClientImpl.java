package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.shardingjdbc.orchestration.reg.etcd.internal.stub.*;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import io.shardingjdbc.orchestration.reg.exception.RegExceptionHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * EtcdClientImpl client
 * <p>
 *
 * @author junxiong
 */
@Slf4j
public class EtcdClientImpl implements EtcdClient, AutoCloseable {
    private long timeout = 500;
    private long span = 200;
    private int retryTimes = 2;

    private LeaseGrpc.LeaseFutureStub leaseStub;
    private KVGrpc.KVFutureStub kvStub;
    private WatchGrpc.WatchStub watchStub;

    private AtomicBoolean closed = new AtomicBoolean(false);
    private ConcurrentMap<Long, Watcher> watchers = Maps.newConcurrentMap();

    /**
     * construct a etcd client from grpc channel
     *
     * @param channel Channel
     */
    EtcdClientImpl(Channel channel, long timeout, long span, int retryTimes) {
        this.timeout = timeout;
        this.span = span;
        this.retryTimes = retryTimes;

        leaseStub = LeaseGrpc.newFutureStub(channel);
        kvStub = KVGrpc.newFutureStub(channel);
        watchStub = WatchGrpc.newStub(channel);
    }

    @Override
    public Optional<String> get(String key) {
        final RangeRequest request = RangeRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(key))
                .build();
        return retry(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final RangeResponse rangeResponse = kvStub.range(request).get(timeout, TimeUnit.MILLISECONDS);
                return rangeResponse.getKvsCount() > 0
                        ? rangeResponse.getKvs(0).getValue().toStringUtf8() : null;
            }
        });
    }

    @Override
    public Optional<List<String>> list(String dir) {
        final RangeRequest request = RangeRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(dir))
                .setRangeEnd(prefix(dir))
                .build();
        return retry(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                RangeResponse rangeResponse = kvStub.range(request).get(timeout, TimeUnit.MILLISECONDS);
                final List<String> keys = Lists.newArrayList();
                for (KeyValue keyValue : rangeResponse.getKvsList()) {
                    keys.add(keyValue.getKey().toStringUtf8());
                }
                return keys;
            }
        });
    }

    @Override
    public Optional<String> put(String key, String value) {
        final PutRequest request = PutRequest.newBuilder()
                .setPrevKv(true)
                .setKey(ByteString.copyFromUtf8(key))
                .setValue(ByteString.copyFromUtf8(value))
                .build();
        return retry(new Callable<String>() {
            @Override
            public String call() throws Exception {
                PutResponse putResponse = kvStub.put(request).get(timeout, TimeUnit.MILLISECONDS);
                return putResponse.getPrevKv().getValue().toStringUtf8();
            }
        });
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
        return retry(new Callable<String>() {
            @Override
            public String call() throws Exception {
                PutResponse putResponse = kvStub.put(request).get(timeout, TimeUnit.MILLISECONDS);
                return putResponse.getPrevKv().getValue().toStringUtf8();
            }
        });
    }

    @Override
    public Optional<List<String>> delete(String key) {
        final DeleteRangeRequest request = DeleteRangeRequest.newBuilder()
                .build();
        return retry(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                DeleteRangeResponse deleteRangeResponse = kvStub.deleteRange(request).get(timeout, TimeUnit.MILLISECONDS);
                List<String> deletedKeys = Lists.newArrayList();
                for (KeyValue keyValue : deleteRangeResponse.getPrevKvsList()) {
                    deletedKeys.add(keyValue.getKey().toStringUtf8());
                }
                return deletedKeys;
            }
        });
    }

    @Override
    public Optional<Long> lease(long ttl) {
        final LeaseGrantRequest request = LeaseGrantRequest.newBuilder().setTTL(ttl).build();
        final ListenableFuture<LeaseGrantResponse> response = leaseStub.leaseGrant(request);
        return retry(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                LeaseGrantResponse leaseGrantResponse = leaseStub.leaseGrant(request).get(timeout, TimeUnit.MILLISECONDS);
                return leaseGrantResponse.getID();
            }
        });
    }

    @Override
    public Optional<Watcher> watch(final @NonNull String key) {
        final WatchCreateRequest createWatchRequest = WatchCreateRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(key))
                .build();
        final WatchRequest request = WatchRequest.newBuilder()
                .setCreateRequest(createWatchRequest)
                .build();
        return retry(new Callable<Watcher>() {
            @Override
            public Watcher call() throws Exception {
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
                                final WatchEvent watchEvent = WatchEvent.of(watcher.getId(), event);
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
                requestStream.onNext(request);
                return watcher;
            }
        });
    }

    @Override
    public void close() throws Exception {
        closed.compareAndSet(true, false);
    }

    private <T> Optional<T> retry(final Callable<T> command) {
        try {
            return Optional.fromNullable(RetryerBuilder.<T>newBuilder()
                    .retryIfExceptionOfType(TimeoutException.class)
                    .retryIfExceptionOfType(ExecutionException.class)
                    .retryIfExceptionOfType(InterruptedException.class)
                    .withWaitStrategy(WaitStrategies.fixedWait(span, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(retryTimes))
                    .build()
                    .call(command));
        } catch (Exception e) {
            RegExceptionHandler.handleException(e);
            return Optional.absent();
        }
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

}
