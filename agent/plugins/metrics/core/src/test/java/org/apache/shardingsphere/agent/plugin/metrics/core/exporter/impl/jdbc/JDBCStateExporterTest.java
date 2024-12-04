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

package org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.jdbc;

import org.apache.shardingsphere.agent.plugin.core.context.ShardingSphereDataSourceContext;
import org.apache.shardingsphere.agent.plugin.core.holder.ShardingSphereDataSourceContextHolder;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JDBCStateExporterTest {
    
    private String instanceId;
    
    private String databaseName;
    
    @BeforeEach
    void setUp() {
        instanceId = UUID.randomUUID().toString();
        databaseName = "foo_db";
        ContextManager contextManager = mockContextManager(instanceId, databaseName);
        ShardingSphereDataSourceContextHolder.put(instanceId, new ShardingSphereDataSourceContext(databaseName, contextManager));
    }
    
    private ContextManager mockContextManager(final String instanceId, final String databaseName) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = new ShardingSphereDatabase(databaseName, mock(), mock(), mock(), Collections.emptyList());
        when(result.getDatabase(databaseName)).thenReturn(database);
        when(result.getComputeNodeInstanceContext().getInstance().getMetaData().getId()).thenReturn(instanceId);
        when(result.getComputeNodeInstanceContext().getInstance().getState().getCurrentState().ordinal()).thenReturn(InstanceState.OK.ordinal());
        return result;
    }
    
    @AfterEach
    void clean() {
        MetricConfiguration config = new MetricConfiguration("jdbc_state", MetricCollectorType.GAUGE_METRIC_FAMILY, "State of ShardingSphere-JDBC. 0 is OK; 1 is CIRCUIT BREAK");
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
        ShardingSphereDataSourceContextHolder.remove(instanceId);
    }
    
    @Test
    void assertExport() {
        Optional<GaugeMetricFamilyMetricsCollector> collector = new JDBCStateExporter().export("FIXTURE");
        assertTrue(collector.isPresent());
        assertThat(collector.get().toString(), containsString(instanceId));
        assertThat(collector.get().toString(), containsString(databaseName));
    }
}
