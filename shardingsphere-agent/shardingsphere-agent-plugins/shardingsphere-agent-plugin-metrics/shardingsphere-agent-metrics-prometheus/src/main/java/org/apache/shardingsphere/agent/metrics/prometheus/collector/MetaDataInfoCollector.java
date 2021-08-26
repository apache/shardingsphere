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

package org.apache.shardingsphere.agent.metrics.prometheus.collector;

import com.zaxxer.hikari.HikariDataSource;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.util.MetricsUtil;
import org.apache.shardingsphere.agent.metrics.prometheus.wrapper.PrometheusWrapperFactory;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Meta data information collector.
 */
@Slf4j
public final class MetaDataInfoCollector extends Collector {
    
    private static final String LOGIC_DB_COUNT = "schema_count";
    
    private static final String ACTUAL_DB_COUNT = "database_count";
    
    private static final PrometheusWrapperFactory FACTORY = new PrometheusWrapperFactory();
    
    private static final String PROXY_CONTEXT_CLASS_STR = "org.apache.shardingsphere.proxy.backend.context.ProxyContext";
    
    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> result = new LinkedList<>();
        Optional<GaugeMetricFamily> metaDataInfo = FACTORY.createGaugeMetricFamily(MetricIds.METADATA_INFO);
        if (MetricsUtil.classNotExist(PROXY_CONTEXT_CLASS_STR) || !metaDataInfo.isPresent()) {
            return result;
        }
        collectProxy(metaDataInfo.get());
        result.add(metaDataInfo.get());
        return result;
    }
    
    private void collectProxy(final GaugeMetricFamily metricFamily) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        metricFamily.addMetric(Collections.singletonList(LOGIC_DB_COUNT), metaDataContexts.getMetaDataMap().size());
        Set<String> databaseSet = new HashSet<>();
        metaDataContexts.getMetaDataMap().values().forEach(each -> each.getResource().getDataSources().values()
                .forEach(dataSource -> MetaDataInfoCollector.this.countDatabase(databaseSet, dataSource)));
        metricFamily.addMetric(Collections.singletonList(ACTUAL_DB_COUNT), databaseSet.size());
    }
    
    private void countDatabase(final Set<String> databaseSet, final DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            String jdbcUrl = ((HikariDataSource) dataSource).getJdbcUrl();
            try {
                URI uri = new URI(jdbcUrl.substring(5));
                if (null != uri.getPath()) {
                    databaseSet.add(uri.getPath());
                }
            } catch (URISyntaxException | NullPointerException e) {
                log.info("Unsupported jdbc url by URI: {}", jdbcUrl);
            }
        }
    }
}
