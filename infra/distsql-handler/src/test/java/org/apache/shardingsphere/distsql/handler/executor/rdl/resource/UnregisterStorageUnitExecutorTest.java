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

package org.apache.shardingsphere.distsql.handler.executor.rdl.resource;

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.distsql.handler.fixture.DistSQLHandlerFixtureRule;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.InUsedStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnregisterStorageUnitExecutorTest {
    
    private final UnregisterStorageUnitExecutor executor = new UnregisterStorageUnitExecutor();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private ContextManager contextManager;
    
    @Mock
    private MetaDataManagerPersistService metaDataManagerPersistService;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("foo_db");
        StorageUnit storageUnit = createStorageUnit();
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(new DistSQLHandlerFixtureRule()));
        contextManager = mockContextManager();
        executor.setDatabase(database);
    }
    
    private static StorageUnit createStorageUnit() {
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.emptyMap());
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/foo_db", "username", "test"));
        return new StorageUnit(mock(StorageNode.class), dataSourcePoolProps, new MockedDataSource());
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerPersistService);
        when(result.getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    @Test
    void assertExecuteUpdateSuccess() {
        when(database.getRuleMetaData().getInUsedStorageUnitNameAndRulesMap()).thenReturn(Collections.emptyMap());
        UnregisterStorageUnitStatement sqlStatement = new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false);
        executor.executeUpdate(sqlStatement, contextManager);
        verify(metaDataManagerPersistService).unregisterStorageUnits(database, sqlStatement.getStorageUnitNames());
    }
    
    @Test
    void assertExecuteUpdateWithStorageUnitNotExisted() {
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        assertThrows(MissingRequiredStorageUnitsException.class,
                () -> executor.executeUpdate(new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWithStorageUnitInUsed() {
        ShardingSphereRule rule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        assertThrows(InUsedStorageUnitException.class, () -> executor.executeUpdate(new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWithStorageUnitInUsedWithoutIgnoredTables() {
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new DistSQLHandlerFixtureRule())));
        assertThrows(InUsedStorageUnitException.class, () -> executor.executeUpdate(new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), false, false), mock(ContextManager.class)));
    }
    
    @Test
    void assertExecuteUpdateWithStorageUnitInUsedWithIgnoredTables() {
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(new DistSQLHandlerFixtureRule())));
        UnregisterStorageUnitStatement sqlStatement = new UnregisterStorageUnitStatement(Collections.singleton("foo_ds"), true, false);
        executor.executeUpdate(sqlStatement, contextManager);
        verify(metaDataManagerPersistService).unregisterStorageUnits(database, sqlStatement.getStorageUnitNames());
    }
    
    @Test
    void assertExecuteUpdateWithIfExists() {
        UnregisterStorageUnitStatement sqlStatement = new UnregisterStorageUnitStatement(true, Collections.singleton("foo_ds"), true, false);
        executor.executeUpdate(sqlStatement, contextManager);
        verify(metaDataManagerPersistService).unregisterStorageUnits(database, sqlStatement.getStorageUnitNames());
    }
    
    @Test
    void assertExecuteUpdateWithStorageUnitInUsedWithIfExists() {
        ShardingSphereRule rule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("", Collections.singleton("foo_ds")));
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        assertThrows(InUsedStorageUnitException.class, () -> executor.executeUpdate(new UnregisterStorageUnitStatement(true, Collections.singleton("foo_ds"), true, false), contextManager));
    }
}
