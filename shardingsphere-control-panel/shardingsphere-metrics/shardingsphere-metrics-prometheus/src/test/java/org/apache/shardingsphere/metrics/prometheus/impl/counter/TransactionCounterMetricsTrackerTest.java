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

package org.apache.shardingsphere.metrics.prometheus.impl.counter;

import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.metrics.enums.MetricsTypeEnum;
import org.apache.shardingsphere.metrics.prometheus.impl.AbstractPrometheusCollectorRegistry;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TransactionCounterMetricsTrackerTest extends AbstractPrometheusCollectorRegistry {
    
    @Test
    public void assertShardingCounter() {
        TransactionCounterMetricsTracker tracker = new TransactionCounterMetricsTracker();
        assertThat(tracker.metricsLabel(), is(MetricsLabelEnum.TRANSACTION.getName()));
        assertThat(tracker.metricsType(), is(MetricsTypeEnum.COUNTER.name()));
        String name = "transaction";
        String[] labelNames = {"status"};
        String[] beginValues = {"begin"};
        tracker.inc(1.0, beginValues);
        Double beginValue = getValue(name, labelNames, beginValues);
        assertThat(beginValue, is(1.0));
        
        String[] commitValues = {"commit"};
        tracker.inc(2.0, commitValues);
        Double commitValue = getValue(name, labelNames, commitValues);
        assertThat(commitValue, is(2.0));
    
        String[] rollbackValues = {"rollback"};
        tracker.inc(3.0, rollbackValues);
        Double rollbackValue = getValue(name, labelNames, rollbackValues);
        assertThat(rollbackValue, is(3.0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNoLabels() {
        TransactionCounterMetricsTracker tracker = new TransactionCounterMetricsTracker();
        tracker.inc(1.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertModeLabels() {
        TransactionCounterMetricsTracker tracker = new TransactionCounterMetricsTracker();
        String[] labelValues = {"begin", "rollback"};
        tracker.inc(1.0, labelValues);
    }
    
    private Double getValue(final String name, final String[] labelNames, final String[] labelValue) {
        return getCollectorRegistry().getSampleValue(name, labelNames, labelValue);
    }
}

