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

import org.apache.shardingsphere.agent.plugin.metrics.core.ProxyContextRestorer;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricFamilyMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProxyMetaDataInfoExporterTest extends ProxyContextRestorer {
    
    @After
    public void reset() {
        MetricConfiguration config = new MetricConfiguration("proxy_meta_data_info", MetricCollectorType.GAUGE_METRIC_FAMILY, null, Collections.singletonList("name"), Collections.emptyMap());
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @Test
    public void assertExportWithoutContextManager() {
        ProxyContext.init(null);
        assertFalse(new ProxyMetaDataInfoExporter().export("FIXTURE").isPresent());
    }
    
    @Test
    public void assertExportWithContextManager() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.emptyMap());
        // TODO mock schema_count and database_count
        ProxyContext.init(contextManager);
        Optional<GaugeMetricFamilyMetricsCollector> collector = new ProxyMetaDataInfoExporter().export("FIXTURE");
        assertTrue(collector.isPresent());
        assertThat(collector.get().toString(), is("schema_count=0, database_count=0"));
    }
}
