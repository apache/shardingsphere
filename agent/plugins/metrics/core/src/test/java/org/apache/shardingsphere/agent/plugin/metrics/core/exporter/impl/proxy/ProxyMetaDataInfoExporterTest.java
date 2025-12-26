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
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ProxyMetaDataInfoExporterTest {
    
    @AfterEach
    void reset() {
        MetricConfiguration config = new MetricConfiguration("proxy_meta_data_info", MetricCollectorType.GAUGE_METRIC_FAMILY, null, Collections.singletonList("name"), Collections.emptyMap());
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    /**
     * Assert export returns empty when ProxyContext has no ContextManager.
     */
    @Test
    void assertExportWithoutContextManager() {
        // Arrange
        when(ProxyContext.getInstance().getContextManager()).thenReturn(null);
        
        // Act
        Optional<GaugeMetricFamilyMetricsCollector> result = new ProxyMetaDataInfoExporter().export("FIXTURE");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    /**
     * Assert export returns metrics with correct database and storage unit counts.
     */
    @Test
    void assertExportWithContextManager() {
        // Arrange
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ProxyMetaDataInfoExporter exporter = new ProxyMetaDataInfoExporter();
        
        // Act
        Optional<GaugeMetricFamilyMetricsCollector> result = exporter.export("FIXTURE");
        
        // Assert
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), is("database_count=1, storage_unit_count=1"));
    }
    
    /**
     * Assert export correctly calculates storage unit count across multiple databases.
     */
    @Test
    void assertExportWithMultipleDatabases() {
        // Arrange: Mock multiple databases with different storage unit counts
        ShardingSphereDatabase database1 = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database2 = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        
        when(database1.getResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        when(database1.getResourceMetaData().getStorageUnits())
                .thenReturn(createStorageUnitMap("ds_0", "ds_1"));
        when(database1.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        
        when(database2.getResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        when(database2.getResourceMetaData().getStorageUnits())
                .thenReturn(createStorageUnitMap("ds_0", "ds_1", "ds_2"));
        when(database2.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getAllDatabases()).thenReturn(java.util.Arrays.asList(database1, database2));
        
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        
        ProxyMetaDataInfoExporter exporter = new ProxyMetaDataInfoExporter();
        
        // Act
        Optional<GaugeMetricFamilyMetricsCollector> result = exporter.export("FIXTURE");
        
        // Assert
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), is("database_count=2, storage_unit_count=5"));
    }
    
    /**
     * Assert export correctly handles zero storage units.
     */
    @Test
    void assertExportWithZeroStorageUnits() {
        // Arrange
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        
        ProxyMetaDataInfoExporter exporter = new ProxyMetaDataInfoExporter();
        
        // Act
        Optional<GaugeMetricFamilyMetricsCollector> result = exporter.export("FIXTURE");
        
        // Assert
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), is("database_count=1, storage_unit_count=0"));
    }
    
    /**
     * Assert export correctly handles empty database list.
     */
    @Test
    void assertExportWithNoDatabases() {
        // Arrange
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getAllDatabases()).thenReturn(Collections.emptyList());
        
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        
        ProxyMetaDataInfoExporter exporter = new ProxyMetaDataInfoExporter();
        
        // Act
        Optional<GaugeMetricFamilyMetricsCollector> result = exporter.export("FIXTURE");
        
        // Assert
        assertTrue(result.isPresent());
        assertThat(result.get().toString(), is("database_count=0, storage_unit_count=0"));
    }
    
    private ContextManager mockContextManager() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("ds_0", mock(StorageUnit.class)));
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private java.util.Map<String, StorageUnit> createStorageUnitMap(final String... unitNames) {
        java.util.Map<String, StorageUnit> result = new java.util.HashMap<>();
        for (String name : unitNames) {
            result.put(name, mock(StorageUnit.class));
        }
        return result;
    }
}
