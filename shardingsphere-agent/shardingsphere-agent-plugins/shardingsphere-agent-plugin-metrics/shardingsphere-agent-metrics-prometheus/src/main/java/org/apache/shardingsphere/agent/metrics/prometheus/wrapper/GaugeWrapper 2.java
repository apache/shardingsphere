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

package org.apache.shardingsphere.agent.metrics.prometheus.wrapper;

import io.prometheus.client.Gauge;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.metrics.api.MetricsWrapper;

/**
 * Prometheus gauge wrapper.
 */
@RequiredArgsConstructor
public final class GaugeWrapper implements MetricsWrapper {
    
    private final Gauge gauge;
    
    @Override
    public void inc(final double value) {
        gauge.inc(value);
    }
    
    @Override
    public void inc(final double value, final String... labels) {
        gauge.labels(labels).inc(value);
    }
    
    @Override
    public void dec(final double value) {
        gauge.dec(value);
    }
    
    @Override
    public void dec(final double value, final String... labels) {
        gauge.labels(labels).dec(value);
    }
}
