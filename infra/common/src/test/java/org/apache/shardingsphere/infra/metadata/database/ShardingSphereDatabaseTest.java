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

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereDatabaseFactory.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSphereDatabaseTest {
    
    @Test
    void assertGetAllSchemas() {
        DatabaseType databaseType = mock(DatabaseType.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), ruleMetaData, Collections.singleton(new ShardingSphereSchema("foo_schema")));
        List<ShardingSphereSchema> actual = new LinkedList<>(database.getAllSchemas());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getName(), is("foo_schema"));
    }
    
    @Test
    void assertContainsSchema() {
        DatabaseType databaseType = mock(DatabaseType.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), ruleMetaData, Collections.singleton(new ShardingSphereSchema("foo_schema")));
        assertTrue(database.containsSchema("foo_schema"));
        assertFalse(database.containsSchema("non_existent_schema"));
    }
    
    @Test
    void assertGetSchema() {
        DatabaseType databaseType = mock(DatabaseType.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema");
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), ruleMetaData, Collections.singleton(schema));
        assertThat(database.getSchema("foo_schema"), is(schema));
        assertNull(database.getSchema("non_existent_schema"));
    }
    
    @Test
    void assertAddSchema() {
        DatabaseType databaseType = mock(DatabaseType.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereSchema schema = new ShardingSphereSchema("new_schema");
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), ruleMetaData, Collections.emptyList());
        assertFalse(database.containsSchema("new_schema"));
        database.addSchema(schema);
        assertTrue(database.containsSchema("new_schema"));
        assertThat(database.getSchema("new_schema"), is(schema));
    }
    
    @Test
    void assertDropSchema() {
        DatabaseType databaseType = mock(DatabaseType.class);
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), ruleMetaData, Collections.singleton(new ShardingSphereSchema("foo_schema")));
        assertTrue(database.containsSchema("foo_schema"));
        database.dropSchema("foo_schema");
        assertFalse(database.containsSchema("foo_schema"));
    }
    
    @Test
    void assertIsComplete() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertTrue(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyList()).isComplete());
    }
    
    @Test
    void assertIsNotCompleteWithoutRule() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyList()).isComplete());
    }
    
    @Test
    void assertIsNotCompleteWithoutDataSource() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyList()).isComplete());
    }
    
    @Test
    void assertNotContainsDataSource() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyList()).containsDataSource());
    }
    
    @Test
    void assertContainsDataSource() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertTrue(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyList()).containsDataSource());
    }
    
    @Test
    void assertReloadRules() {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        ShardingSphereRule rule0 = mock(ShardingSphereRule.class);
        when(rule0.getConfiguration()).thenReturn(mock(RuleConfiguration.class));
        when(rule0.getAttributes()).thenReturn(new RuleAttributes(mock(MutableDataNodeRuleAttribute.class)));
        rules.add(rule0);
        ShardingSphereRule rule1 = mock(ShardingSphereRule.class);
        when(rule1.getConfiguration()).thenReturn(mock(RuleConfiguration.class));
        when(rule1.getAttributes()).thenReturn(new RuleAttributes());
        rules.add(rule1);
        RuleMetaData ruleMetaData = new RuleMetaData(rules);
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyList());
        database.reloadRules();
        assertThat(database.getRuleMetaData().getRules().size(), is(2));
    }
    
    @Test
    void assertGetPostgreSQLDefaultSchema() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create("foo_db", databaseType,
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(ComputeNodeInstanceContext.class));
        assertNotNull(actual.getSchema("public"));
    }
    
    @Test
    void assertGetMySQLDefaultSchema() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereDatabase actual = ShardingSphereDatabaseFactory.create("foo_db", databaseType,
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(ComputeNodeInstanceContext.class));
        assertNotNull(actual.getSchema("foo_db"));
    }
}
