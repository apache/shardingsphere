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

import io.shardingsphere.core.executor.ShardingThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sampling control service.
 *
 * @author chenqingyang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SamplingService {
    
    private static final SamplingService INSTANCE = new SamplingService();
    
    private int samplingRatePerMinute;
    
    private volatile AtomicInteger samplingCount = new AtomicInteger(0);
    
    private volatile ScheduledFuture<?> scheduledFuture;
    
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
     * @param samplingRatePerMinute sampling rate in one minute
     */
    public void init(final int samplingRatePerMinute) {
        this.samplingRatePerMinute = samplingRatePerMinute;
        if (null != scheduledFuture) {
            scheduledFuture.cancel(true);
        }
        if (samplingRatePerMinute > 0) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(ShardingThreadFactoryBuilder.build("Opentracing-Sampling-Cleaner"));
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                
                @Override
                public void run() {
                    samplingCount.set(0);
                }
            }, 0, 1, TimeUnit.MINUTES);
        }
    }
    
    /**
     * Judge sampling is allowed or not.
     *
     * @return sampling is allowed or not
     */
    public boolean trySampling() {
        return samplingCount.get() < samplingRatePerMinute;
    }
    
    /**
     * Increase sampling count.
     */
    public void increaseSampling() {
        if (samplingRatePerMinute > 0) {
            samplingCount.getAndIncrement();
        }
    }
}
