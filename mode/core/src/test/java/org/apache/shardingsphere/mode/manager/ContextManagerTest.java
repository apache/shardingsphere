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

package org.apache.shardingsphere.mode.manager;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextManagerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShardingSphereDatabase database = mockDatabase();
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getDatabases().values()).thenReturn(Collections.singleton(database));
        when(computeNodeInstanceContext.getInstance()).thenReturn(new ComputeNodeInstance(new ProxyInstanceMetaData("foo_id", 3307), Collections.emptyList()));
        when(computeNodeInstanceContext.getModeConfiguration()).thenReturn(mock(ModeConfiguration.class));
        contextManager = new ContextManager(metaDataContexts, computeNodeInstanceContext, mock(PersistRepository.class));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(ruleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(mock(DataNode.class)));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        when(result.containsSchema("foo_schema")).thenReturn(true);
        when(result.getSchemas()).thenReturn(new HashMap<>(Collections.singletonMap("foo_schema", new ShardingSphereSchema(DefaultDatabase.LOGIC_NAME))));
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        return result;
    }
    
    @Test
    void assertRenewMetaDataContexts() {
        MetaDataContexts contexts = mock(MetaDataContexts.class);
        contextManager.renewMetaDataContexts(contexts);
        assertThat(contextManager.getMetaDataContexts(), is(contexts));
    }
    
    @Test
    void assertGetDatabase() {
        assertNotNull(contextManager.getDatabase("foo_db"));
    }
    
    @Test
    void assertGetDatabaseWithNull() {
        assertThrows(NoDatabaseSelectedException.class, () -> contextManager.getDatabase(null));
    }
    
    @Test
    void assertGetDatabaseWhenNotExisted() {
        assertThrows(UnknownDatabaseException.class, () -> contextManager.getDatabase("bar_db"));
    }
    
    @Test
    void assertGetStorageUnits() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), resourceMetaData, mock(RuleMetaData.class), Collections.emptyMap());
        when(metaDataContexts.getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(database);
        when(metaDataContexts.getMetaData().containsDatabase(DefaultDatabase.LOGIC_NAME)).thenReturn(true);
        assertThat(contextManager.getStorageUnits(DefaultDatabase.LOGIC_NAME).size(), is(1));
    }
    
    @Test
    void assertReloadSchema() {
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getName()).thenReturn("foo_db");
        ShardingSphereDatabase database = mockDatabase();
        contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        verify(contextManager.getPersistServiceFacade().getRepository()).delete(DatabaseMetaDataNode.getMetaDataSchemaPath("foo_db", "foo_schema"));
    }
    
    @Test
    void assertReloadTable() {
        ShardingSphereDatabase database = mockDatabase();
        contextManager.reloadTable(database, "foo_schema", "foo_table");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits().containsKey("foo_ds"));
    }
    
    @Test
    void assertReloadTableWithDataSourceName() {
        ShardingSphereDatabase database = mockDatabase();
        contextManager.reloadTable(database, "foo_schema", "foo_ds", "foo_table");
        assertTrue(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits().containsKey("foo_ds"));
    }
    
    @Test
    void assertGetPreSelectedDatabaseNameWithJDBC() {
        when(computeNodeInstanceContext.getInstance()).thenReturn(new ComputeNodeInstance(new JDBCInstanceMetaData("foo_id", "foo_db"), Collections.emptyList()));
        when(metaDataContexts.getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db", mock(ShardingSphereDatabase.class)));
        assertThat(contextManager.getPreSelectedDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertGetPreSelectedDatabaseNameWithProxy() {
        assertNull(contextManager.getPreSelectedDatabaseName());
    }
    
    @Test
    void assertClose() {
        contextManager.close();
        verify(metaDataContexts).close();
    }
}
