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

package org.apache.shardingsphere.metrics.api;

import org.apache.shardingsphere.metrics.enums.MetricsTypeEnum;

/**
 * Summary metrics tracker.
 */
public interface SummaryMetricsTracker extends MetricsTracker {
    
    /**
     * Start timer with summary.
     *
     * @param labelValues label values
     * @return Summary metrics tracker delegate
     */
    default SummaryMetricsTrackerDelegate startTimer(final String... labelValues) {
        return new NoneSummaryMetricsTrackerDelegate();
    }
    
    /**
     * Observe the given amount.
     *
     * @param amount amount
     */
    default void observer(final long amount) {
    }
    
    /**
     * Metrics type.
     *
     * @return metrics type
     */
    default String metricsType() {
        return MetricsTypeEnum.SUMMARY.name();
    }
}
