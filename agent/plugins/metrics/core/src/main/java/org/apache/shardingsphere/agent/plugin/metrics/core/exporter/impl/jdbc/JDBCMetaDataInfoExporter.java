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
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * JDBC meta data information exporter.
 */
public final class JDBCMetaDataInfoExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("jdbc_meta_data_info", MetricCollectorType.GAUGE_METRIC_FAMILY,
            "Meta data information of ShardingSphere-JDBC",
            Arrays.asList("driver_instance", "database", "type"));
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        result.cleanMetrics();
        for (Entry<String, ShardingSphereDataSourceContext> entry : ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().entrySet()) {
            Optional.ofNullable(entry.getValue().getContextManager().getDatabase(entry.getValue().getDatabaseName()))
                    .ifPresent(optional -> result.addMetric(Arrays.asList(entry.getKey(), optional.getName(), "storage_unit_count"), optional.getResourceMetaData().getStorageUnits().size()));
        }
        return Optional.of(result);
    }
}
