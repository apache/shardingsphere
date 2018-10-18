/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.reg.etcd;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
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
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.orchestration.reg.etcd.internal.channel.EtcdChannelFactory;
import io.shardingsphere.orchestration.reg.etcd.internal.keepalive.KeepAlive;
import io.shardingsphere.orchestration.reg.etcd.internal.retry.EtcdRetryEngine;
import io.shardingsphere.orchestration.reg.etcd.internal.watcher.EtcdWatchStreamObserver;
import io.shardingsphere.orchestration.reg.exception.RegistryCenterException;
import io.shardingsphere.orchestration.reg.listener.EventListener;
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
public final class EtcdRegistryCenter implements RegistryCenter {
    
    private RegistryCenterConfiguration config;
    
    private EtcdRetryEngine etcdRetryEngine;
    
    private KVFutureStub kvStub;
    
    private LeaseFutureStub leaseStub;
    
    private WatchStub watchStub;
    
    private KeepAlive keepAlive;
    
    @Override
    public void init(final RegistryCenterConfiguration config) {
        this.config = config;
        etcdRetryEngine = new EtcdRetryEngine(config);
        Channel channel = EtcdChannelFactory.getInstance(Splitter.on(',').trimResults().splitToList(config.getServerLists()));
        kvStub = KVGrpc.newFutureStub(channel);
        leaseStub = LeaseGrpc.newFutureStub(channel);
        watchStub = WatchGrpc.newStub(channel);
        keepAlive = new KeepAlive(channel, config.getTimeToLiveSeconds());
    }
    
    @Override
    public String get(final String key) {
        final RangeRequest request = RangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(key)).build();
        return etcdRetryEngine.execute(new Callable<String>() {
            
            @Override
            public String call() throws InterruptedException, ExecutionException, TimeoutException {
                RangeResponse response = kvStub.range(request).get(config.getOperationTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
                return response.getKvsCount() > 0 ? response.getKvs(0).getValue().toStringUtf8() : null;
            }
        }).orNull();
    }
    
    @Override
    public String getDirectly(final String key) {
        return get(key);
    }
    
    @Override
    public boolean isExisted(final String key) {
        return null != get(key);
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        String path = key + "/";
        final RangeRequest request = RangeRequest.newBuilder().setKey(ByteString.copyFromUtf8(path)).setRangeEnd(getRangeEnd(path)).build();
        Optional<List<String>> result = etcdRetryEngine.execute(new Callable<List<String>>() {
            
            @Override
            public List<String> call() throws InterruptedException, ExecutionException, TimeoutException {
                RangeResponse response = kvStub.range(request).get(config.getOperationTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
                List<String> result = new ArrayList<>();
                for (KeyValue each : response.getKvsList()) {
                    String childFullPath = each.getKey().toStringUtf8();
                    result.add(childFullPath.substring(childFullPath.lastIndexOf("/") + 1));
                }
                return result;
            }
        });
        return result.isPresent() ? result.get() : Collections.<String>emptyList();
    }
    
    @Override
    public void persist(final String key, final String value) {
        final PutRequest request = PutRequest.newBuilder().setPrevKv(true).setKey(ByteString.copyFromUtf8(key)).setValue(ByteString.copyFromUtf8(value)).build();
        etcdRetryEngine.execute(new Callable<Void>() {
            
            @Override
            public Void call() throws InterruptedException, ExecutionException, TimeoutException {
                kvStub.put(request).get(config.getOperationTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
                return null;
            }
        });
    }
    
    @Override
    public void update(final String key, final String value) {
        persist(key, value);
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        final Optional<Long> leaseId = lease();
        if (!leaseId.isPresent()) {
            throw new RegistryCenterException("Unable to set up heat beat for key %s", key);
        }
        final PutRequest request = PutRequest.newBuilder().setPrevKv(true).setLease(leaseId.get()).setKey(ByteString.copyFromUtf8(key)).setValue(ByteString.copyFromUtf8(value)).build();
        etcdRetryEngine.execute(new Callable<Void>() {
            
            @Override
            public Void call() throws InterruptedException, ExecutionException, TimeoutException {
                kvStub.put(request).get(config.getOperationTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
                return null;
            }
        });
    }
    
    private Optional<Long> lease() {
        final LeaseGrantRequest request = LeaseGrantRequest.newBuilder().setTTL(config.getTimeToLiveSeconds()).build();
        return etcdRetryEngine.execute(new Callable<Long>() {
            
            @Override
            public Long call() throws InterruptedException, ExecutionException, TimeoutException {
                long leaseId = leaseStub.leaseGrant(request).get(config.getOperationTimeoutMilliseconds(), TimeUnit.MILLISECONDS).getID();
                keepAlive.heartbeat(leaseId);
                return leaseId;
            }
        });
    }

    @Override
    public void watch(final String key, final EventListener eventListener) {
        WatchCreateRequest createWatchRequest = WatchCreateRequest.newBuilder().setKey(ByteString.copyFromUtf8(key)).setRangeEnd(getRangeEnd(key)).build();
        final WatchRequest request = WatchRequest.newBuilder().setCreateRequest(createWatchRequest).build();
        etcdRetryEngine.execute(new Callable<Void>() {
            
            @Override
            public Void call() {
                watchStub.watch(new EtcdWatchStreamObserver(eventListener)).onNext(request);
                return null;
            }
        });
    }
    
    @Override
    public void close() {
        keepAlive.close();
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
