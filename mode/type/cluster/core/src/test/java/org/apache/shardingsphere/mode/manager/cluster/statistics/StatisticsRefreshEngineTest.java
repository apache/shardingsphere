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

package org.apache.shardingsphere.mode.manager.cluster.statistics;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorEngine;
import org.apache.shardingsphere.mode.exclusive.callback.ExclusiveOperationVoidCallback;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class StatisticsRefreshEngineTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private DatabaseType databaseType;
    
    @Mock
    private DialectDatabaseStatisticsCollector collector;
    
    @BeforeEach
    void setUp() throws SQLException {
        Properties props = PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED.getKey(), Boolean.TRUE.toString()));
        when(metaData.getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(props));
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(schema.getName()).thenReturn("public");
        when(DatabaseTypedSPILoader.findService(DialectDatabaseStatisticsCollector.class, databaseType)).thenReturn(Optional.of(collector));
        ExclusiveOperatorEngine exclusiveOperatorEngine = contextManager.getExclusiveOperatorEngine();
        doAnswer(invocation -> {
            invocation.<ExclusiveOperationVoidCallback>getArgument(2).execute();
            return null;
        }).when(exclusiveOperatorEngine).operate(any(RefreshStatisticsOperation.class), eq(5000L), any(ExclusiveOperationVoidCallback.class));
    }
    
    @Test
    void assertRefreshWithDatabaseRemovedAfterEnumeration() {
        ShardingSphereTable table = new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        when(schema.getAllTables()).thenReturn(Collections.singleton(table));
        new StatisticsRefreshEngine(contextManager).refresh();
        verify(collector).collectRowColumnValues("foo_db", "public", "foo_table", metaData);
        verify(contextManager, never()).getPersistServiceFacade();
    }
    
    @Test
    void assertRefreshContinuesAfterCollectionFailure() {
        ShardingSphereTable firstTable = new ShardingSphereTable("first_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable secondTable = new ShardingSphereTable("second_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        when(schema.getAllTables()).thenReturn(Arrays.asList(firstTable, secondTable));
        when(collector.collectRowColumnValues("foo_db", "public", "first_table", metaData)).thenThrow(new IllegalStateException("Collection failed."));
        new StatisticsRefreshEngine(contextManager).refresh();
        verify(collector).collectRowColumnValues("foo_db", "public", "first_table", metaData);
        verify(collector).collectRowColumnValues("foo_db", "public", "second_table", metaData);
        verify(contextManager, never()).getPersistServiceFacade();
    }
}
