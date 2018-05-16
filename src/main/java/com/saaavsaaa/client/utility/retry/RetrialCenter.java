package com.saaavsaaa.client.utility.retry;

import com.saaavsaaa.client.zookeeper.base.BaseOperation;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by aaa
 */
public enum RetrialCenter {
    INSTANCE;
    
    private static final DelayQueue<BaseOperation> queue = new DelayQueue<>();
    private static final ThreadPoolExecutor retryExecution = new ThreadPoolExecutor(1, 10, 200, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10), new ThreadFactory() {
        private final AtomicInteger threadIndex = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("zk-retry-" + threadIndex.incrementAndGet());
            return thread;
        }
    });
    private DelayRetrial retrial;
    
    public void init(DelayRetrial retrial) {
        if (retrial == null) {
            this.retrial = retrial;
        }
    }
    
    public void start(){
        for (;;) {
            BaseOperation op;
            try {
                op = queue.take();
            } catch (InterruptedException e) {
                continue;
            }
            retryExecution.submit(new Runnable() {
                @Override
                public void run() {
                    boolean result;
                    try {
                        result = op.executeOperation();
                    } catch (Exception e) {
                        result = false;
                    }
                    if (result){
                        queue.offer(op);
                    }
                }
            });
        }
    }
    
    public void stop(){
        retryExecution.shutdown();
    }
    
    public void add(BaseOperation operation){
        if (retrial == null){
            retrial = DelayRetrial.newNoInitDelayRetrial();
        }
        operation.setRetrial(new DelayRetryExecution(retrial));
        queue.offer(operation);
    }
}
