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

package io.shardingsphere.opentracing.sampling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

/**
 * samping control service.
 *
 * @author chenqingyang
 */
public final class SamplingService {
    
    private static final SamplingService INSTANCE = new SamplingService();
    
    private int sampleNumPM;
    
    private volatile boolean on;
    
    private volatile AtomicInteger samplingCount;
    
    private volatile ScheduledFuture<?> scheduledFuture;
    
    private SamplingService() {
        
    }
    
    /**
     * Get sampling service instance.
     *
     * @return sampling service instance
     */
    public static SamplingService getInstance() {
        return INSTANCE;
    }
    
    /**
     * sampling service init.
     *
     * @param sampleNumPM sampling num in one minutes
     */
    public void init(final int sampleNumPM) {
        this.sampleNumPM = sampleNumPM;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        if (this.sampleNumPM > 0) {
            on = true;
            this.resetSamplingFactor();
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                private final AtomicInteger threadIndex = new AtomicInteger(0);
                
                @Override
                public Thread newThread(final Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("Sharding-opentracing-sampling" + threadIndex.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }
            });
            scheduledFuture = service.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    resetSamplingFactor();
                }
            }, 0, 1, TimeUnit.MINUTES);
        }
    }
    
    /**
     * Is sampling allowed.
     *
     * @return true, if sampling mechanism is on.
     */
    public boolean trySampling() {
        if (on) {
            int factor = samplingCount.get();
            if (factor > sampleNumPM) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * sampling count.
     */
    public void samplingAdd() {
        if (on) {
            samplingCount.getAndIncrement();
        }
        
    }
    
    private void resetSamplingFactor() {
        samplingCount = new AtomicInteger(0);
    }
}
