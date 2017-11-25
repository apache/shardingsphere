package io.shardingjdbc.orchestration.reg.etcd.internal;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import etcdserverpb.KVGrpc;
import etcdserverpb.LeaseGrpc;
import etcdserverpb.LeaseGrpc.LeaseFutureStub;
import etcdserverpb.Rpc.LeaseGrantRequest;
import etcdserverpb.Rpc.PutRequest;
import etcdserverpb.Rpc.PutResponse;
import etcdserverpb.Rpc.RangeRequest;
import etcdserverpb.Rpc.RangeResponse;
import etcdserverpb.Rpc.WatchCreateRequest;
import etcdserverpb.Rpc.WatchRequest;
import etcdserverpb.WatchGrpc;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import io.shardingjdbc.orchestration.reg.exception.RegExceptionHandler;
import mvccpb.Kv.KeyValue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Etcd client.
 *
 * @author junxiong
 */
public final class EtcdClient {
    
    private long timeoutMills = 500L;
    
    private long retryMills = 200L;
    
    private int retryTimes = 2;
    
    private LeaseFutureStub leaseStub;
    
    private KVGrpc.KVFutureStub kvStub;
    
    private WatchGrpc.WatchStub watchStub;
    
    EtcdClient(final Channel channel, final long timeoutMills, final long retryMills, final int retryTimes) {
        this.timeoutMills = timeoutMills;
        this.retryMills = retryMills;
        this.retryTimes = retryTimes;
        leaseStub = LeaseGrpc.newFutureStub(channel);
        kvStub = KVGrpc.newFutureStub(channel);
        watchStub = WatchGrpc.newStub(channel);
    }
    
    /**
     * Get value of a specific key.
     *
     * @param key key
     * @return value
     */
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
    
    /**
     * List all child key/value for a directory.
     * directory should be end with "/"
     *
     * @param directory directory
     * @return value
     */
    public Optional<List<String>> list(final String directory) {
        final RangeRequest request = RangeRequest.newBuilder()
                .setKey(ByteString.copyFromUtf8(directory))
                .setRangeEnd(getRangeEnd(directory))
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
    
    /**
     * Put value to a specific key, if result is not absent, it is an update.
     *
     * @param key key
     * @param value value
     * @return old value
     */
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
    
    /**
     * Put value to a specific key, if result is not absent, it is an update.
     *
     * @param key key
     * @param value value
     * @param ttl time to live in milliseconds
     * @return old value
     */
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
    
    /**
     * Watch a keys.
     *
     * @param key String
     * @return list of watcher
     */
    public Optional<Watcher> watch(final String key) {
        WatchCreateRequest createWatchRequest = WatchCreateRequest.newBuilder().setKey(ByteString.copyFromUtf8(key)).setRangeEnd(getRangeEnd(key)).build();
        final WatchRequest watchRequest = WatchRequest.newBuilder().setCreateRequest(createWatchRequest).build();
        return retry(new Callable<Watcher>() {
            
            @Override
            public Watcher call() throws Exception {
                Watcher watcher = new Watcher();
                StreamObserver<WatchRequest> requestStream = watchStub.watch(new WatchStreamObserver(watcher));
                requestStream.onNext(watchRequest);
                return watcher;
            }
        });
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
    
    private ByteString getRangeEnd(final String key) {
        byte[] noPrefix = {0};
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
