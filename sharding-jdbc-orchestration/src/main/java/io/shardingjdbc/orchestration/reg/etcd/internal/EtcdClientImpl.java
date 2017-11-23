package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import etcdserverpb.KVGrpc;
import etcdserverpb.LeaseGrpc;
import etcdserverpb.LeaseGrpc.LeaseFutureStub;
import etcdserverpb.Rpc.DeleteRangeRequest;
import etcdserverpb.Rpc.DeleteRangeResponse;
import etcdserverpb.Rpc.LeaseGrantRequest;
import etcdserverpb.Rpc.PutRequest;
import etcdserverpb.Rpc.PutResponse;
import etcdserverpb.Rpc.RangeRequest;
import etcdserverpb.Rpc.RangeResponse;
import etcdserverpb.Rpc.WatchCreateRequest;
import etcdserverpb.Rpc.WatchRequest;
import etcdserverpb.Rpc.WatchResponse;
import etcdserverpb.WatchGrpc;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import io.shardingjdbc.orchestration.reg.exception.RegExceptionHandler;
import mvccpb.Kv.Event;
import mvccpb.Kv.KeyValue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Etcd client implementation client.
 *
 * @author junxiong
 */
public class EtcdClientImpl implements EtcdClient, AutoCloseable {
    
    private long timeoutMills = 500L;
    
    private long retryMills = 200L;
    
    private int retryTimes = 2;
    
    private LeaseFutureStub leaseStub;
    
    private KVGrpc.KVFutureStub kvStub;
    
    private WatchGrpc.WatchStub watchStub;
    
    private AtomicBoolean closed = new AtomicBoolean(false);
    
    private ConcurrentMap<Long, Watcher> watchers = Maps.newConcurrentMap();
    
    /**
     * construct a etcd client from grpc channel.
     *
     * @param channel Channel
     */
    EtcdClientImpl(final Channel channel, final long timeoutMills, final long retryMills, final int retryTimes) {
        this.timeoutMills = timeoutMills;
        this.retryMills = retryMills;
        this.retryTimes = retryTimes;
        leaseStub = LeaseGrpc.newFutureStub(channel);
        kvStub = KVGrpc.newFutureStub(channel);
        watchStub = WatchGrpc.newStub(channel);
    }
    
    @Override
    public Optional<String> get(final String key) {
        final RangeRequest request = RangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(key)).build();
        return retry(new Callable<String>() {
            
            @Override
            public String call() throws Exception {
                final RangeResponse rangeResponse = kvStub.range(request).get(timeoutMills, TimeUnit.MILLISECONDS);
                return rangeResponse.getKvsCount() > 0
                        ? rangeResponse.getKvs(0).getValue().toStringUtf8() : null;
            }
        });
    }
    
    @Override
    public Optional<List<String>> list(final String dir) {
        final RangeRequest request = RangeRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(dir))
                .setRangeEnd(prefix(dir))
                .build();
        return retry(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                RangeResponse rangeResponse = kvStub.range(request).get(timeoutMills, TimeUnit.MILLISECONDS);
                final List<String> keys = Lists.newArrayList();
                for (KeyValue keyValue : rangeResponse.getKvsList()) {
                    keys.add(keyValue.getKey().toStringUtf8());
                }
                return keys;
            }
        });
    }
    
    @Override
    public Optional<String> put(final String key, final String value) {
        final PutRequest request = PutRequest.newBuilder()
                .setPrevKv(true)
                .setKey(ByteString.copyFromUtf8(key))
                .setValue(ByteString.copyFromUtf8(value))
                .build();
        return retry(new Callable<String>() {
            @Override
            public String call() throws Exception {
                PutResponse putResponse = kvStub.put(request).get(timeoutMills, TimeUnit.MILLISECONDS);
                return putResponse.getPrevKv().getValue().toStringUtf8();
            }
        });
    }
    
    @Override
    public Optional<String> put(final String key, final String value, final long ttl) {
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
                PutResponse putResponse = kvStub.put(request).get(timeoutMills, TimeUnit.MILLISECONDS);
                return putResponse.getPrevKv().getValue().toStringUtf8();
            }
        });
    }
    
    private Optional<Long> lease(final long ttl) {
        final LeaseGrantRequest request = LeaseGrantRequest.newBuilder().setTTL(ttl).build();
        leaseStub.leaseGrant(request);
        return retry(new Callable<Long>() {
            
            @Override
            public Long call() throws Exception {
                return leaseStub.leaseGrant(request).get(timeoutMills, TimeUnit.MILLISECONDS).getID();
            }
        });
    }
    
    @Override
    public Optional<List<String>> delete(final String key) {
        final DeleteRangeRequest request = DeleteRangeRequest.newBuilder().build();
        return retry(new Callable<List<String>>() {
            
            @Override
            public List<String> call() throws Exception {
                DeleteRangeResponse deleteRangeResponse = kvStub.deleteRange(request).get(timeoutMills, TimeUnit.MILLISECONDS);
                List<String> deletedKeys = Lists.newArrayList();
                for (KeyValue keyValue : deleteRangeResponse.getPrevKvsList()) {
                    deletedKeys.add(keyValue.getKey().toStringUtf8());
                }
                return deletedKeys;
            }
        });
    }
    
    @Override
    public Optional<Watcher> watch(final String key) {
        final WatchCreateRequest createWatchRequest = WatchCreateRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(key))
                .setRangeEnd(prefix(key))
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
                    public void onNext(final WatchResponse response) {
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
                    public void onError(final Throwable t) {
                        // TODO retry watch later
                        throw new RegException(new Exception(t));
                    }
                    
                    @Override
                    public void onCompleted() {
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
                    .withWaitStrategy(WaitStrategies.fixedWait(retryMills, TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(retryTimes))
                    .build().call(command));
        } catch (final ExecutionException | RetryException ex) {
            RegExceptionHandler.handleException(ex);
            return Optional.absent();
        }
    }
    
    private ByteString prefix(final String key) {
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
