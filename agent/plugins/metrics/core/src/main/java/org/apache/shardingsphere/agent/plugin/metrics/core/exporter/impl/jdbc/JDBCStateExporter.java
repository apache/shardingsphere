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

import org.apache.shardingsphere.agent.plugin.core.holder.ContextManagerHolder;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * JDBC state exporter.
 */
public final class JDBCStateExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("jdbc_state", MetricCollectorType.GAUGE_METRIC_FAMILY,
            "State of ShardingSphere-JDBC. 0 is OK; 1 is CIRCUIT BREAK", Collections.singletonList("database"));
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        result.cleanMetrics();
        for (Entry<String, ContextManager> entry : ContextManagerHolder.getDatabaseContextManager().entrySet()) {
            addMetric(result, entry.getKey(), entry.getValue());
        }
        return Optional.of(result);
    }
    
    private void addMetric(final GaugeMetricFamilyMetricsCollector collector, final String database, final ContextManager contextManager) {
        ShardingSphereDatabase shardingSphereDatabase = contextManager.getDatabase(database);
        if (null != shardingSphereDatabase) {
            collector.addMetric(Collections.singletonList(database), contextManager.getComputeNodeInstanceContext().getInstance().getState().getCurrentState().ordinal());
        }
    }
}
