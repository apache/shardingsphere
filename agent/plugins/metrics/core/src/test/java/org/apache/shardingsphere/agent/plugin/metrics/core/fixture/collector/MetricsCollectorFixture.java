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

package org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.CounterMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.HistogramMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.SummaryMetricsCollector;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MetricsCollectorFixture implements CounterMetricsCollector, GaugeMetricsCollector, HistogramMetricsCollector, SummaryMetricsCollector, GaugeMetricFamilyMetricsCollector {
    
    private int value;
    
    private final Map<String, Integer> labeledValues = new LinkedHashMap<>();
    
    @Override
    public void inc() {
        value++;
    }
    
    @Override
    public void inc(final String... labels) {
        String key = String.join(".", labels);
        labeledValues.put(key, labeledValues.getOrDefault(key, 0) + 1);
    }
    
    @Override
    public void dec() {
        value--;
    }
    
    @Override
    public void dec(final String... labels) {
        String key = String.join(".", labels);
        labeledValues.put(key, labeledValues.getOrDefault(key, 0) - 1);
    }
    
    @Override
    public void observe(final double value) {
        this.value = (int) value;
    }
    
    @Override
    public void addMetric(final List<String> labelValues, final double value) {
        for (String each : labelValues) {
            labeledValues.put(each, labeledValues.getOrDefault(each, 0) + (int) value);
        }
    }
    
    @Override
    public Object getRawMetricFamilyObject() {
        return null;
    }
    
    @Override
    public String toString() {
        return labeledValues.isEmpty() ? String.valueOf(value) : String.join(", ", getLabeledContents());
    }
    
    private Collection<String> getLabeledContents() {
        return labeledValues.entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
    
    /**
     * Reset.
     */
    public void reset() {
        value = 0;
        labeledValues.clear();
    }
}
