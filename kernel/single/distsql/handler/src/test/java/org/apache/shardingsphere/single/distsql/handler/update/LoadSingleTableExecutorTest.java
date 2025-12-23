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

package org.apache.shardingsphere.single.distsql.handler.update;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecuteEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.PhysicalDataSourceAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.distsql.segment.SingleTableSegment;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({SingleTableDataNodeLoader.class, SingleTableLoadUtils.class, PhysicalDataSourceAggregator.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class LoadSingleTableExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(database.getSchema("foo_db")).thenReturn(schema);
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
    }
    
    private ContextManager mockContextManager(final SingleRule rule) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        if (null == rule) {
            when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        } else {
            when(rule.getAttributes()).thenReturn(new RuleAttributes());
            when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        }
        return result;
    }
    
    @Test
    void assertExecuteUpdateWithInvalidTableNodeFormatWhenSchemaNotSupported() {
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singleton(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl")));
        assertThrows(InvalidDataNodeFormatException.class, () -> new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", mockContextManager(mock(SingleRule.class)), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithExistedLogicTables() {
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Arrays.asList(
                new SingleTableSegment("*", "*"), new SingleTableSegment("*", "*", "*"), new SingleTableSegment("foo_ds", "*"), new SingleTableSegment("foo_ds", "foo_tbl")));
        assertThrows(TableExistsException.class, () -> new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", mockContextManager(mock(SingleRule.class)), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithNotExistedActualTables() {
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(PhysicalDataSourceAggregator.getAggregatedDataSources(any(), any())).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singleton(new SingleTableSegment("foo_ds", "foo_tbl")));
        assertThrows(TableNotFoundException.class, () -> new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", mockContextManager(mock(SingleRule.class)), null).executeUpdate());
    }
    
    @Test
    void assertExecuteUpdateWithSingleRule() throws SQLException {
        Collection<String> currentTables = new LinkedList<>(Collections.singleton("foo_ds.foo_tbl"));
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(SingleTableDataNodeLoader.load(eq("foo_db"), any(), any())).thenReturn(Collections.singletonMap("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl"))));
        when(SingleTableLoadUtils.convertToDataNodes(eq("foo_db"), any(), any())).thenReturn(Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration(currentTables, null);
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singleton(new SingleTableSegment("*", "bar_tbl")));
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        ContextManager contextManager = mockContextManager(rule);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, null).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).alterRuleConfiguration(any(), any());
    }
    
    @Test
    void assertExecuteUpdateWithoutSingleRule() throws SQLException {
        Collection<String> currentTables = new LinkedList<>(Collections.singleton("foo_ds.foo_tbl"));
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(SingleTableDataNodeLoader.load(eq("foo_db"), any(), any())).thenReturn(Collections.singletonMap("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl"))));
        when(SingleTableLoadUtils.convertToDataNodes(eq("foo_db"), any(), any())).thenReturn(Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        SingleRuleConfiguration currentConfig = new SingleRuleConfiguration(currentTables, null);
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singleton(new SingleTableSegment("*", "bar_tbl")));
        SingleRule rule = mock(SingleRule.class);
        when(rule.getConfiguration()).thenReturn(currentConfig);
        ContextManager contextManager = mockContextManager(null);
        new DistSQLUpdateExecuteEngine(sqlStatement, "foo_db", contextManager, null).executeUpdate();
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        verify(metaDataManagerPersistService).alterRuleConfiguration(any(), any());
    }
}
