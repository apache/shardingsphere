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

package org.apache.shardingsphere.agent.plugin.metrics.core.exporter.impl.proxy;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.util.Collections;
import java.util.Optional;

/**
 * Proxy meta data information exporter.
 */
public final class ProxyMetaDataInfoExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("proxy_meta_data_info",
            MetricCollectorType.GAUGE_METRIC_FAMILY, "Meta data information of ShardingSphere-Proxy. database_count is logic number of databases; storage_unit_count is number of storage units",
            Collections.singletonList("name"), Collections.emptyMap());
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        if (null == ProxyContext.getInstance().getContextManager()) {
            return Optional.empty();
        }
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        result.cleanMetrics();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        result.addMetric(Collections.singletonList("database_count"), metaDataContexts.getMetaData().getDatabases().size());
        result.addMetric(Collections.singletonList("storage_unit_count"), getStorageUnitCount(metaDataContexts));
        return Optional.of(result);
    }
    
    private int getStorageUnitCount(final MetaDataContexts metaDataContexts) {
        return metaDataContexts.getMetaData().getDatabases().values().stream().map(each -> each.getResourceMetaData().getStorageUnits().size()).reduce(0, Integer::sum);
    }
}
