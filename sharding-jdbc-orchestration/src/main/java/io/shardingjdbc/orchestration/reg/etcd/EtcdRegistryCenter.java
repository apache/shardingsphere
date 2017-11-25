package io.shardingjdbc.orchestration.reg.etcd;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Optional;
import com.google.protobuf.ByteString;
import etcdserverpb.KVGrpc;
import etcdserverpb.KVGrpc.KVFutureStub;
import etcdserverpb.LeaseGrpc;
import etcdserverpb.LeaseGrpc.LeaseFutureStub;
import etcdserverpb.Rpc.LeaseGrantRequest;
import etcdserverpb.Rpc.PutRequest;
import etcdserverpb.Rpc.RangeRequest;
import etcdserverpb.Rpc.RangeResponse;
import etcdserverpb.Rpc.WatchCreateRequest;
import etcdserverpb.Rpc.WatchRequest;
import etcdserverpb.WatchGrpc;
import etcdserverpb.WatchGrpc.WatchStub;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;
import io.shardingjdbc.orchestration.reg.base.EventListener;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdChannelFactory;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdWatchStreamObserver;
import io.shardingjdbc.orchestration.reg.etcd.internal.EtcdWatcher;
import io.shardingjdbc.orchestration.reg.exception.RegException;
import io.shardingjdbc.orchestration.reg.exception.RegExceptionHandler;
import mvccpb.Kv.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Etcd based registry center.
 *
 * @author junxiong
 */
public final class EtcdRegistryCenter implements CoordinatorRegistryCenter {
    
    private final EtcdConfiguration etcdConfiguration;
    
    private final KVFutureStub kvStub;
    
    private final LeaseFutureStub leaseStub;
    
    private final WatchStub watchStub;
    
    public EtcdRegistryCenter(final EtcdConfiguration etcdConfiguration) {
        this.etcdConfiguration = etcdConfiguration;
        Channel channel = EtcdChannelFactory.getInstance(Arrays.asList(etcdConfiguration.getServerLists().split(",")));
        kvStub = KVGrpc.newFutureStub(channel);
        leaseStub = LeaseGrpc.newFutureStub(channel);
        watchStub = WatchGrpc.newStub(channel);
    }
    
    @Override
    public void init() {
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String get(final String key) {
        final RangeRequest request = RangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(getFullPathWithNamespace(key))).build();
        return retry(new Callable<String>() {
            
            @Override
            public String call() throws Exception {
                RangeResponse response = kvStub.range(request).get(etcdConfiguration.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
                return response.getKvsCount() > 0 ? response.getKvs(0).getValue().toStringUtf8() : null;
            }
        }).orNull();
    }
    
    @Override
    public boolean isExisted(final String key) {
        return null != get(key);
    }
    
    @Override
    public void persist(final String key, final String value) {
        final PutRequest request = PutRequest.newBuilder().setPrevKv(true).setKey(ByteString.copyFromUtf8(getFullPathWithNamespace(key))).setValue(ByteString.copyFromUtf8(value)).build();
        retry(new Callable<String>() {
            
            @Override
            public String call() throws Exception {
                return kvStub.put(request).get(etcdConfiguration.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS).getPrevKv().getValue().toStringUtf8();
            }
        });
    }
    
    @Override
    public void update(final String key, final String value) {
        persist(key, value);
    }
    
    @Override
    public String getDirectly(final String key) {
        return get(key);
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        String fullPath = getFullPathWithNamespace(key);
        final Optional<Long> leaseId = lease();
        if (!leaseId.isPresent()) {
            throw new RegException("Unable to set up heat beat for key %s", fullPath);
        }
        final PutRequest request = PutRequest.newBuilder().setPrevKv(true).setLease(leaseId.get()).setKey(ByteString.copyFromUtf8(fullPath)).setValue(ByteString.copyFromUtf8(value)).build();
        retry(new Callable<String>() {
            
            @Override
            public String call() throws Exception {
                return kvStub.put(request).get(etcdConfiguration.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS).getPrevKv().getValue().toStringUtf8();
            }
        });
    }
    
    private Optional<Long> lease() {
        final LeaseGrantRequest request = LeaseGrantRequest.newBuilder().setTTL(etcdConfiguration.getTimeToLiveMilliseconds()).build();
        return retry(new Callable<Long>() {
            
            @Override
            public Long call() throws Exception {
                return leaseStub.leaseGrant(request).get(etcdConfiguration.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS).getID();
            }
        });
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        String fullPath = getFullPathWithNamespace(key);
        final RangeRequest request = RangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(fullPath)).setRangeEnd(getRangeEnd(fullPath)).build();
        Optional<List<String>> result = retry(new Callable<List<String>>() {
            
            @Override
            public List<String> call() throws Exception {
                RangeResponse response = kvStub.range(request).get(etcdConfiguration.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
                List<String> result = new ArrayList<>();
                for (KeyValue each : response.getKvsList()) {
                    result.add(each.getKey().toStringUtf8());
                }
                return result;
            }
        });
        return result.isPresent() ? result.get() : Collections.<String>emptyList();
    }
    
    @Override
    public void watch(final String key, final EventListener eventListener) {
        String fullPath = getFullPathWithNamespace(key);
        WatchCreateRequest createWatchRequest = WatchCreateRequest.newBuilder().setKey(ByteString.copyFromUtf8(fullPath)).setRangeEnd(getRangeEnd(fullPath)).build();
        final WatchRequest watchRequest = WatchRequest.newBuilder().setCreateRequest(createWatchRequest).build();
        Optional<EtcdWatcher> watcher = retry(new Callable<EtcdWatcher>() {
            
            @Override
            public EtcdWatcher call() throws Exception {
                EtcdWatcher watcher = new EtcdWatcher();
                StreamObserver<WatchRequest> requestStream = watchStub.watch(new EtcdWatchStreamObserver(watcher));
                requestStream.onNext(watchRequest);
                return watcher;
            }
        });
        if (watcher.isPresent()) {
            watcher.get().addEventListener(eventListener);
        }
    }
    
    private String getFullPathWithNamespace(final String path) {
        return String.format("/%s/%s", etcdConfiguration.getNamespace(), path);
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
    
    private <T> Optional<T> retry(final Callable<T> command) {
        try {
            return Optional.fromNullable(RetryerBuilder.<T>newBuilder()
                    .retryIfExceptionOfType(TimeoutException.class)
                    .retryIfExceptionOfType(ExecutionException.class)
                    .retryIfExceptionOfType(InterruptedException.class)
                    .withWaitStrategy(WaitStrategies.fixedWait(etcdConfiguration.getRetryIntervalMilliseconds(), TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.stopAfterAttempt(etcdConfiguration.getMaxRetries()))
                    .build().call(command));
        } catch (final ExecutionException | RetryException ex) {
            RegExceptionHandler.handleException(ex);
            return Optional.absent();
        }
    }
}
