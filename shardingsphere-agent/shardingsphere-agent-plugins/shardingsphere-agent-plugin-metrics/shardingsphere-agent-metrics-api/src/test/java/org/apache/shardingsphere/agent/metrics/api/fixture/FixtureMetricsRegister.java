/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.agent.metrics.api.fixture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import org.apache.shardingsphere.agent.metrics.api.MetricsRegister;

public final class FixtureMetricsRegister implements MetricsRegister {
    
    private static final Map<String, DoubleAdder> COUNTER_MAP = new ConcurrentHashMap<>();
    
    private static final Map<String, AtomicInteger> GAUGE_MAP = new ConcurrentHashMap<>();
    
    private static final Map<String, LongAdder> HISTOGRAM_MAP = new ConcurrentHashMap<>();
    
    @Override
    public void registerGauge(final String name, final String[] labelNames, final String document) {
        GAUGE_MAP.put(name, new AtomicInteger());
    }
    
    @Override
    public void registerCounter(final String name, final String[] labelNames, final String document) {
        COUNTER_MAP.put(name, new DoubleAdder());
    }
    
    @Override
    public void registerHistogram(final String name, final String[] labelNames, final String document) {
        HISTOGRAM_MAP.put(name, new LongAdder());
    }
    
    @Override
    public void counterIncrement(final String name, final String[] labelValues) {
        DoubleAdder doubleAdder = COUNTER_MAP.get(name);
        if (null != doubleAdder) {
            doubleAdder.add(1.0d);
        }
    }
    
    @Override
    public void counterIncrement(final String name, final String[] labelValues, final long count) {
        DoubleAdder doubleAdder = COUNTER_MAP.get(name);
        if (null != doubleAdder) {
            doubleAdder.add(count);
        }
    }
    
    @Override
    public void gaugeIncrement(final String name, final String[] labelValues) {
        AtomicInteger atomicInteger = GAUGE_MAP.get(name);
        if (null != atomicInteger) {
            atomicInteger.incrementAndGet();
        }
    }
    
    @Override
    public void gaugeDecrement(final String name, final String[] labelValues) {
        AtomicInteger atomicInteger = GAUGE_MAP.get(name);
        if (null != atomicInteger) {
            atomicInteger.decrementAndGet();
        }
    }
    
    @Override
    public void recordTime(final String name, final String[] labelValues, final long duration) {
        LongAdder longAdder = HISTOGRAM_MAP.get(name);
        if (null != longAdder) {
            longAdder.add(duration);
        }
    }
}
