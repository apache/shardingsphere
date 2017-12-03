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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import etcdserverpb.LeaseGrpc;
import etcdserverpb.LeaseGrpc.LeaseStub;
import etcdserverpb.Rpc.LeaseKeepAliveRequest;
import etcdserverpb.Rpc.LeaseKeepAliveResponse;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keep the lease alive.
 *
 * @author junxiong
 */
@Slf4j
public final class KeepAlive {
    
    private static final long INITIAL_DELAY = 100;
    
    private final LeaseStub leaseStub;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private final ConcurrentMap<Long, KeepAliveTask> keepAliveTasks = Maps.newConcurrentMap();
    
    private final long span;
    
    private ScheduledFuture scheduledFuture;
    
    private AtomicBoolean closed = new AtomicBoolean(true);
    
    public KeepAlive(final Channel channel, final long timeToLiveSeconds) {
        this.span = timeToLiveSeconds * 1000 / 3;
        this.leaseStub = LeaseGrpc.newStub(channel);
    }
    
    /**
     * Start keep alive.
     */
    public synchronized void start() {
        if (closed.get()) {
            scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
                
                @Override
                public void run() {
                    for (KeepAliveTask keepAliveTask : keepAliveTasks.values()) {
                        keepAliveTask.run();
                    }
                }
            }, INITIAL_DELAY, span / 3000, TimeUnit.MILLISECONDS);
            closed.compareAndSet(true, false);
        }
    }
    
    /**
     * keep lease alive.
     *
     * @param leaseId lease ID
     */
    public void heartbeat(final long leaseId) {
        log.debug("Heartbeat lease {}", leaseId);
        Preconditions.checkState(!closed.get(), "Keep alive is closed");
        final StreamObserver<LeaseKeepAliveRequest> requestObserver = leaseStub.leaseKeepAlive(createResponseObserver(leaseId));
        final long tickNow = System.currentTimeMillis();
        keepAliveTasks.putIfAbsent(leaseId, new KeepAliveTask(leaseId, tickNow, requestObserver));
    }

    private StreamObserver<LeaseKeepAliveResponse> createResponseObserver(final long leaseId) {
        return new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(final LeaseKeepAliveResponse response) {
                long id = response.getID();
                long ttl = response.getTTL() * 1000;

                long tickTime = System.currentTimeMillis() + ttl / 3;
                log.debug("Reschedule heartbeat time for lease {} to {}", id, tickTime);
                final KeepAliveTask keepAliveTask = keepAliveTasks.get(id);
                if (keepAliveTask != null) {
                    keepAliveTasks.put(id, keepAliveTask.newTick(tickTime));
                }
            }
            
            @Override
            public void onCompleted() {
                log.info("Keep alive finished");
            }
            
            @Override
            public void onError(final Throwable t) {
                log.warn("Keep alive failed, due to {}, renew it", Status.fromThrowable(t));
                heartbeat(leaseId);
            }
        };
    }
    
    /**
     * Close keep alive.
     */
    public synchronized void close() {
        if (!closed.get()) {
            for (KeepAliveTask keepAliveTask: keepAliveTasks.values()) {
                keepAliveTask.cancel();
            }
            keepAliveTasks.clear();
            scheduledFuture.cancel(false);
            closed.compareAndSet(false, true);
        }
    }
    
    @RequiredArgsConstructor
    private class KeepAliveTask implements Runnable {
        
        private final long id;
        
        private final long tick;
        
        private final StreamObserver<LeaseKeepAliveRequest> observer;
        
        private boolean fired;
        
        public void cancel() {
            observer.onCompleted();
        }
        
        public KeepAliveTask newTick(final long tick) {
            return new KeepAliveTask(id, tick, observer);
        }
        
        @Override
        public void run() {
            if (!fired && tick <= System.currentTimeMillis()) {
                log.debug("heart beat lease {} at time {}", id, tick);
                LeaseKeepAliveRequest request = LeaseKeepAliveRequest.newBuilder().setID(id).build();
                observer.onNext(request);
                fired = true;
            }
        }
    }
}
