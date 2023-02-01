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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.exporter.MetricsExporter;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

/**
 * Proxy meta data information exporter.
 */
@Slf4j
public final class ProxyMetaDataInfoExporter implements MetricsExporter {
    
    private final MetricConfiguration config = new MetricConfiguration("proxy_meta_data_info",
            MetricCollectorType.GAUGE_METRIC_FAMILY, "Meta data information of ShardingSphere-Proxy. schema_count is logic number of databases; database_count is actual number of databases",
            Collections.singletonList("name"), Collections.emptyMap());
    
    @Override
    public Optional<GaugeMetricFamilyMetricsCollector> export(final String pluginType) {
        if (null == ProxyContext.getInstance().getContextManager()) {
            return Optional.empty();
        }
        GaugeMetricFamilyMetricsCollector result = MetricsCollectorRegistry.get(config, pluginType);
        result.cleanMetrics();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        result.addMetric(Collections.singletonList("schema_count"), metaDataContexts.getMetaData().getDatabases().size());
        result.addMetric(Collections.singletonList("database_count"), getDatabaseNames(metaDataContexts).size());
        return Optional.of(result);
    }
    
    private Collection<String> getDatabaseNames(final MetaDataContexts metaDataContexts) {
        Collection<String> result = new HashSet<>();
        for (ShardingSphereDatabase each : metaDataContexts.getMetaData().getDatabases().values()) {
            result.addAll(getDatabaseNames(each));
        }
        return result;
    }
    
    private Collection<String> getDatabaseNames(final ShardingSphereDatabase database) {
        Collection<String> result = new HashSet<>();
        for (DataSource each : database.getResourceMetaData().getDataSources().values()) {
            getDatabaseName(each).ifPresent(result::add);
        }
        return result;
    }
    
    private Optional<String> getDatabaseName(final DataSource dataSource) {
        Object jdbcUrl = DataSourcePropertiesCreator.create(dataSource).getAllStandardProperties().get("url");
        if (null == jdbcUrl) {
            log.info("Can not get JDBC URL.");
            return Optional.empty();
        }
        try {
            URI uri = new URI(jdbcUrl.toString().substring(5));
            if (null != uri.getPath()) {
                return Optional.of(uri.getPath());
            }
        } catch (final URISyntaxException | NullPointerException ignored) {
            log.info("Unsupported JDBC URL by URI: {}.", jdbcUrl);
        }
        return Optional.empty();
    }
}
