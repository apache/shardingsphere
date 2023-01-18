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

package org.apache.shardingsphere.agent.plugin.metrics.core.fixture;

import lombok.Getter;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.CounterMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.HistogramMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.SummaryMetricsCollector;

@Getter
public final class MetricsCollectorFixture implements CounterMetricsCollector, GaugeMetricsCollector, HistogramMetricsCollector, SummaryMetricsCollector {
    
    private double value;
    
    @Override
    public void inc() {
        value++;
    }
    
    @Override
    public void inc(final String... labels) {
        value++;
    }
    
    @Override
    public void dec() {
        value--;
    }
    
    @Override
    public void dec(final String... labels) {
        value--;
    }
    
    @Override
    public void observe(final double value) {
        this.value = value;
    }
    
    /**
     * Reset.
     */
    public void reset() {
        value = 0d;
    }
}
