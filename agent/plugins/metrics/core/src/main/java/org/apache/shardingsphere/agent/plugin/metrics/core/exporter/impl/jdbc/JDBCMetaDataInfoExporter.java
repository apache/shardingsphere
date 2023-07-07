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

import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtils;
import org.apache.shardingsphere.agent.plugin.core.util.ShardingSphereDriverUtil;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.apache.shardingsphere.driver.ShardingSphereDriver;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.jdbc.core.driver.DriverDataSourceCache;
import org.apache.shardingsphere.mode.manager.ContextManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * JDBC meta data information exporter.
 */
public final class JDBCMetaDataInfoExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("jdbc_meta_data_info",
            MetricCollectorType.GAUGE_METRIC_FAMILY, "Meta data information of ShardingSphere-JDBC",
            Arrays.asList("database", "type"), Collections.emptyMap());
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        Optional<ShardingSphereDriver> driver = ShardingSphereDriverUtil.getShardingSphereDriver();
        if (!driver.isPresent()) {
            return Optional.empty();
        }
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        result.cleanMetrics();
        DriverDataSourceCache dataSourceCache = AgentReflectionUtils.getFieldValue(driver.get(), "dataSourceCache");
        Map<String, DataSource> dataSourceMap = AgentReflectionUtils.getFieldValue(dataSourceCache, "dataSourceMap");
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            ShardingSphereDataSource dataSource = (ShardingSphereDataSource) entry.getValue();
            String databaseName = AgentReflectionUtils.getFieldValue(dataSource, "databaseName");
            ContextManager contextManager = AgentReflectionUtils.getFieldValue(dataSource, "contextManager");
            result.addMetric(Arrays.asList(databaseName, "storage_unit_count"), contextManager.getDataSourceMap(databaseName).size());
        }
        return Optional.of(result);
    }
}
