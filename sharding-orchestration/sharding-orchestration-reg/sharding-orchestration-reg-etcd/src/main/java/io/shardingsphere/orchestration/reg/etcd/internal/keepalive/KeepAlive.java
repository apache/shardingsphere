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

package io.shardingsphere.orchestration.reg.etcd.internal.keepalive;

import etcdserverpb.LeaseGrpc;
import etcdserverpb.LeaseGrpc.LeaseStub;
import etcdserverpb.Rpc.LeaseKeepAliveRequest;
import etcdserverpb.Rpc.LeaseKeepAliveResponse;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Keep the lease alive.
 *
 * @author junxiong
 */
@Slf4j
public final class KeepAlive implements AutoCloseable {
    
    private static final long DELAY_MILLISECONDS = 100L;
    
    private final LeaseStub leaseStub;
    
    private final long heartbeatIntervalMilliseconds;
    
    private final ConcurrentMap<Long, KeepAliveTask> keepAliveTasks;
    
    private final ScheduledFuture scheduledFuture;
    
    private final ScheduledExecutorService scheduledService;
    
    public KeepAlive(final Channel channel, final long timeToLiveSeconds) {
        leaseStub = LeaseGrpc.newStub(channel);
        heartbeatIntervalMilliseconds = timeToLiveSeconds * 1000L / 3L;
        keepAliveTasks = new ConcurrentHashMap<>();
        scheduledService = Executors.newScheduledThreadPool(1);
        scheduledFuture = scheduledService.scheduleAtFixedRate(new Runnable() {
            
            @Override
            public void run() {
                for (KeepAliveTask keepAliveTask : keepAliveTasks.values()) {
                    keepAliveTask.heartbeat();
                }
            }
        }, DELAY_MILLISECONDS, heartbeatIntervalMilliseconds, TimeUnit.MILLISECONDS);
    }
    
    /**
     * keep lease alive.
     *
     * @param leaseId lease ID
     */
    public void heartbeat(final long leaseId) {
        keepAliveTasks.putIfAbsent(leaseId, new KeepAliveTask(leaseId, leaseStub.leaseKeepAlive(createResponseObserver(leaseId)), System.currentTimeMillis()));
    }
    
    private StreamObserver<LeaseKeepAliveResponse> createResponseObserver(final long leaseId) {
        return new StreamObserver<LeaseKeepAliveResponse>() {
            
            @Override
            public void onNext(final LeaseKeepAliveResponse response) {
                long leaseId = response.getID();
                long nextHeartbeatTimestamp = System.currentTimeMillis() + response.getTTL() * 1000L / 3L;
                log.debug("Reschedule heartbeat time for lease {} to {}", leaseId, nextHeartbeatTimestamp);
                KeepAliveTask keepAliveTask = keepAliveTasks.get(leaseId);
                if (null != keepAliveTask) {
                    keepAliveTask.setNextHeartbeatTimestamp(nextHeartbeatTimestamp);
                }
            }
            
            @Override
            public void onCompleted() {
                log.debug("Keep alive finished");
            }
            
            @Override
            public void onError(final Throwable cause) {
                log.warn("Keep alive failed, due to {}, renew it", Status.fromThrowable(cause));
                heartbeat(leaseId);
            }
        };
    }
    
    @Override
    public void close() {
        for (KeepAliveTask keepAliveTask: keepAliveTasks.values()) {
            keepAliveTask.close();
        }
        keepAliveTasks.clear();
        scheduledService.shutdown();
        scheduledFuture.cancel(false);
    }
    
    @AllArgsConstructor
    private class KeepAliveTask implements AutoCloseable {
        
        private final long leaseId;
        
        private final StreamObserver<LeaseKeepAliveRequest> observer;
        
        @Setter
        private long nextHeartbeatTimestamp;
        
        /**
         * keep heartbeat.
         */
        public void heartbeat() {
            if (nextHeartbeatTimestamp <= System.currentTimeMillis()) {
                log.debug("Heartbeat lease {} at time {}", leaseId, nextHeartbeatTimestamp);
                observer.onNext(LeaseKeepAliveRequest.newBuilder().setID(leaseId).build());
            }
        }
        
        @Override
        public void close() {
            observer.onCompleted();
        }
    }
}
