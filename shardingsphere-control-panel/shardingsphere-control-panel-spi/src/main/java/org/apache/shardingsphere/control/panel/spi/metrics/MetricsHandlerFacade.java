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

package org.apache.shardingsphere.control.panel.spi.metrics;

import java.util.function.Supplier;

/**
 * Metrics Handler facade.
 */
public interface MetricsHandlerFacade {
    
    /**
     * Increment of counter metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues label values
     */
    void counterIncrement(String metricsLabel, String... labelValues);
    
    /**
     * Increment of gauge metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues label values
     */
    void gaugeIncrement(String metricsLabel, String... labelValues);
    
    /**
     * Decrement of gauge metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues label values
     */
    void gaugeDecrement(String metricsLabel, String... labelValues);
    
    /**
     * Start timer of histogram metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues label values
     * @return histogram metrics tracker delegate
     */
    Supplier<Boolean> histogramStartTimer(String metricsLabel, String... labelValues);
    
    /**
     * Start timer of summary metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues label values
     * @return summary metrics tracker delegate
     */
    Supplier<Boolean> summaryStartTimer(String metricsLabel, String... labelValues);
}

