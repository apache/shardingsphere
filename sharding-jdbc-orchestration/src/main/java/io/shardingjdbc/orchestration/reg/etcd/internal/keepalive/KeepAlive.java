package io.shardingjdbc.orchestration.reg.etcd.internal.keepalive;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import etcdserverpb.LeaseGrpc;
import etcdserverpb.LeaseGrpc.LeaseStub;
import etcdserverpb.Rpc;
import etcdserverpb.Rpc.LeaseKeepAliveRequest;
import etcdserverpb.Rpc.LeaseKeepAliveResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.Synchronized;
import lombok.Value;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keep the lease alive
 *
 * @author junxiong
 */
@Slf4j
public class KeepAlive {
    private static final long INITIAL_DELAY = 100;

    private final LeaseStub leaseStub;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ConcurrentMap<Long, KeepAliveTask> keepAliveTasks = Maps.newConcurrentMap();
    private final long span;

    private ScheduledFuture scheduledFuture;

    private AtomicBoolean closed = new AtomicBoolean(true);

    public KeepAlive(io.grpc.Channel channel, long span) {
        this.span = span;
        this.leaseStub = LeaseGrpc.newStub(channel);
    }

    @Synchronized
    public void start() {
        if (closed.get()) {
            scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    for (KeepAliveTask keepAliveTask : keepAliveTasks.values()) {
                        keepAliveTask.run();
                    }
                }
            }, INITIAL_DELAY, span, TimeUnit.MILLISECONDS);
            closed.compareAndSet(true, false);
        }
    }

    /**
     * keep lease alive
     *
     * @param leaseID lease id
     */
    public void heartbeat(final long leaseID) {
        log.debug("Heartbeat lease {}", leaseID);
        Preconditions.checkState(!closed.get(), "Keep alive is closed");
        final StreamObserver<LeaseKeepAliveRequest> requestObserver = leaseStub.leaseKeepAlive(createResponseObserver(leaseID));
        final long tickNow = System.currentTimeMillis();
        keepAliveTasks.putIfAbsent(leaseID, new KeepAliveTask(leaseID, tickNow, requestObserver));
    }

    private StreamObserver<LeaseKeepAliveResponse> createResponseObserver(final long leaseID) {
        return new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse response) {
                long id = response.getID();
                long ttl = response.getTTL() * 1000;

                long tickTime = System.currentTimeMillis() + ttl / 2;
                log.debug("Reschedule heartbeat time for lease {} to {}", id, tickTime);
                final KeepAliveTask keepAliveTask = keepAliveTasks.get(id);
                if (keepAliveTask != null) {
                    keepAliveTasks.put(id, keepAliveTask.withTick(tickTime));
                }
            }

            @Override
            public void onError(Throwable t) {
                log.warn("Keep alive failed, due to {}, renew it", Status.fromThrowable(t));
                heartbeat(leaseID);
            }

            @Override
            public void onCompleted() {
                log.info("Keep alive finished");
            }
        };
    }

    @Synchronized
    public void close() {
        if (!closed.get()) {
            for (KeepAliveTask keepAliveTask: keepAliveTasks.values()) {
                keepAliveTask.cancel();
            }
            keepAliveTasks.clear();
            scheduledFuture.cancel(false);
            closed.compareAndSet(false, true);
        }
    }

    @Value
    @Wither
    private class KeepAliveTask implements Runnable {
        long id;
        long tick;
        StreamObserver<Rpc.LeaseKeepAliveRequest> observer;

        public void cancel() {
            observer.onCompleted();
        }

        @Override
        public void run() {
            if (tick < System.currentTimeMillis()) {
                log.debug("heart beat lease {} at time {}", id, tick);
                LeaseKeepAliveRequest request = LeaseKeepAliveRequest.newBuilder().setID(id).build();
                observer.onNext(request);
            }
        }

    }


}
