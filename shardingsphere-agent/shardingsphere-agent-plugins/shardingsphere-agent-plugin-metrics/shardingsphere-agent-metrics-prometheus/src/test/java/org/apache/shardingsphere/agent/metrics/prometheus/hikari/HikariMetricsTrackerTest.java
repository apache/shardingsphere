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

package org.apache.shardingsphere.agent.metrics.prometheus.hikari;

import com.zaxxer.hikari.metrics.PoolStats;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.apache.shardingsphere.agent.metrics.prometheus.util.ReflectiveUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class HikariMetricsTrackerTest {
    
    @Mock
    private PoolStats poolStats;
    
    @Test
    public void assertCollect() {
        when(poolStats.getActiveConnections()).thenReturn(1);
        when(poolStats.getIdleConnections()).thenReturn(5);
        when(poolStats.getMaxConnections()).thenReturn(100);
        when(poolStats.getMinConnections()).thenReturn(1);
        when(poolStats.getPendingThreads()).thenReturn(0);
        when(poolStats.getTotalConnections()).thenReturn(50);
        HikariMetricsTracker tracker = new HikariMetricsTracker("pool-1", poolStats, CollectorRegistry.defaultRegistry);
        HikariPoolStatCollector collector = (HikariPoolStatCollector) ReflectiveUtil.getFieldValue(tracker, "HIKARI_POOL_STAT_COLLECTOR");
        assertNotNull(collector);
        List<Collector.MetricFamilySamples> data = collector.collect();
        assertEquals(data.size(), 6);
        assertEquals(data.get(0).samples.size(), 1);
        tracker.close();
        data = collector.collect();
        assertEquals(data.size(), 6);
        assertEquals(data.get(0).samples.size(), 0);
    }
}
