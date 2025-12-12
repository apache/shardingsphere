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

import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterStorageUnitExecutorTest {
    
    private final RegisterStorageUnitExecutor executor = new RegisterStorageUnitExecutor();
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(mock(RuleMetaData.class));
        executor.setDatabase(database);
        Plugins.getMemberAccessor().set(RegisterStorageUnitExecutor.class.getDeclaredField("validateHandler"), executor, mock(DistSQLDataSourcePoolPropertiesValidator.class));
    }
    
    @Test
    void assertExecuteUpdateSuccess() {
        assertDoesNotThrow(() -> executor.executeUpdate(createRegisterStorageUnitStatement(), mock(ContextManager.class, RETURNS_DEEP_STUBS)));
    }
    
    @Test
    void assertExecuteUpdateWithDuplicateStorageUnitNamesInStatement() {
        assertThrows(DuplicateStorageUnitException.class, () -> executor.executeUpdate(createRegisterStorageUnitStatementWithDuplicateStorageUnitNames(), mock(ContextManager.class)));
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertExecuteUpdateWithDuplicateStorageUnitNamesWithResourceMetaData() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getStorageUnits("foo_db").keySet()).thenReturn(Collections.singleton("ds_0"));
        assertThrows(DuplicateStorageUnitException.class, () -> executor.executeUpdate(createRegisterStorageUnitStatement(), contextManager));
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertExecuteUpdateWithDuplicateStorageUnitNamesWithDataSourceContainedRule() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class, RETURNS_DEEP_STUBS));
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("ds_0", Collections.emptyList()));
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        assertThrows(DuplicateStorageUnitException.class, () -> executor.executeUpdate(createRegisterStorageUnitStatement(), contextManager));
    }
    
    private RegisterStorageUnitStatement createRegisterStorageUnitStatement() {
        return new RegisterStorageUnitStatement(false, Collections.singleton(new URLBasedDataSourceSegment("ds_0", "jdbc:mock://127.0.0.1:3306/test0", "root", "", new Properties())),
                Collections.emptySet());
    }
    
    private RegisterStorageUnitStatement createRegisterStorageUnitStatementWithDuplicateStorageUnitNames() {
        return new RegisterStorageUnitStatement(false, Arrays.asList(
                new HostnameAndPortBasedDataSourceSegment("ds_0", "127.0.0.1", "3306", "ds_0", "root", "", new Properties()),
                new URLBasedDataSourceSegment("ds_0", "jdbc:mock://127.0.0.1:3306/ds_1", "root", "", new Properties())), Collections.emptySet());
    }
}
