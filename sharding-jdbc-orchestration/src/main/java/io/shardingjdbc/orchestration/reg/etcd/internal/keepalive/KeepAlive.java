/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.reg.etcd.internal.keepalive;

import etcdserverpb.LeaseGrpc;
import etcdserverpb.LeaseGrpc.LeaseStub;
import etcdserverpb.Rpc.LeaseKeepAliveRequest;
import etcdserverpb.Rpc.LeaseKeepAliveResponse;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Keep the lease alive.
 *
 * @author junxiong
 */
@Slf4j
public final class KeepAlive {
    
    private final LeaseStub leaseStub;
    
    private final long heartbeatIntervalMilliseconds;
    
    private final ConcurrentMap<Long, KeepAliveTask> keepAliveTasks;
    
    private final ScheduledFuture scheduledFuture;
    
    public KeepAlive(final Channel channel, final long timeToLiveSeconds) {
        leaseStub = LeaseGrpc.newStub(channel);
        heartbeatIntervalMilliseconds = timeToLiveSeconds * 1000 / 3;
        keepAliveTasks = new ConcurrentHashMap<>();
        scheduledFuture = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2).scheduleAtFixedRate(new Runnable() {
            
            @Override
            public void run() {
                for (KeepAliveTask keepAliveTask : keepAliveTasks.values()) {
                    keepAliveTask.run();
                }
            }
        }, 100L, heartbeatIntervalMilliseconds, TimeUnit.MILLISECONDS);
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
                long ttlMilliseconds = response.getTTL() * 1000L;
                long nextHeartbeatTimestamp = System.currentTimeMillis() + ttlMilliseconds / 3L;
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
    
    /**
     * Close keep alive.
     */
    public synchronized void close() {
        for (KeepAliveTask keepAliveTask: keepAliveTasks.values()) {
            keepAliveTask.cancel();
        }
        keepAliveTasks.clear();
        scheduledFuture.cancel(false);
    }
    
    @AllArgsConstructor
    private class KeepAliveTask implements Runnable {
        
        private final long leaseId;
        
        private final StreamObserver<LeaseKeepAliveRequest> observer;
        
        private long nextHeartbeatTimestamp;
        
        /**
         * Set next heartbeat timestamp.
         * 
         * @param nextHeartbeatTimestamp Next heartbeat timestamp.
         */
        public void setNextHeartbeatTimestamp(final long nextHeartbeatTimestamp) {
            this.nextHeartbeatTimestamp = nextHeartbeatTimestamp;
        }
        
        /**
         * Cancel task.
         */
        public void cancel() {
            observer.onCompleted();
        }
        
        @Override
        public void run() {
            if (nextHeartbeatTimestamp <= System.currentTimeMillis()) {
                log.debug("Heartbeat lease {} at time {}", leaseId, nextHeartbeatTimestamp);
                observer.onNext(LeaseKeepAliveRequest.newBuilder().setID(leaseId).build());
            }
        }
    }
}
