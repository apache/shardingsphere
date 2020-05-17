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

public final class ShardingDatasourceCounterMetricsTrackerTest extends AbstractPrometheusCollectorRegistry {
    
    @Test
    public void assertShardingCounter() {
        ShardingDatasourceCounterMetricsTracker tracker = new ShardingDatasourceCounterMetricsTracker();
        assertThat(tracker.metricsLabel(), is(MetricsLabelEnum.SHARDING_DATASOURCE.getName()));
        assertThat(tracker.metricsType(), is(MetricsTypeEnum.COUNTER.name()));
        String name = "sharding_datasource";
        String[] labelNames = {"datasource"};
        String[] labelValues0 = {"ds_0"};
        tracker.inc(1.0, labelValues0);
        Double ds0 = getCollectorRegistry().getSampleValue(name, labelNames, labelValues0);
        assertThat(ds0, is(1.0));
        String[] labelValues1 = {"ds_1"};
        tracker.inc(3.0, labelValues1);
        Double ds1 = getCollectorRegistry().getSampleValue(name, labelNames, labelValues1);
        assertThat(ds1, is(3.0));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNoLabels() {
        ShardingDatasourceCounterMetricsTracker tracker = new ShardingDatasourceCounterMetricsTracker();
        tracker.inc(1.0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertMoreLabels() {
        ShardingDatasourceCounterMetricsTracker tracker = new ShardingDatasourceCounterMetricsTracker();
        tracker.inc(1.0, "ds0", " ds1");
    }
}

