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

import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereDatabase.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSphereDatabaseTest {
    
    @Test
    void assertIsComplete() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertTrue(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    void assertIsNotCompleteWithoutRule() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    void assertIsNotCompleteWithoutDataSource() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        assertFalse(new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap()).isComplete());
    }
    
    @Test
    void assertReloadRules() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("ds", new MockedDataSource()));
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        rules.add(mock(MutableDataNodeRule.class, RETURNS_DEEP_STUBS));
        rules.add(mock(DataSourceContainedRule.class, RETURNS_DEEP_STUBS));
        rules.add(mock(TableContainedRule.class, RETURNS_DEEP_STUBS));
        RuleMetaData ruleMetaData = new RuleMetaData(rules);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap());
        database.reloadRules(MutableDataNodeRule.class);
        assertThat(database.getRuleMetaData().getRules().size(), is(3));
    }
    
    @Test
    void assertGetPostgreSQLDefaultSchema() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        ShardingSphereDatabase actual = ShardingSphereDatabase.create("foo_db", databaseType, Collections.singletonMap("", databaseType),
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertNotNull(actual.getSchema("public"));
    }
    
    @Test
    void assertGetMySQLDefaultSchema() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ShardingSphereDatabase actual = ShardingSphereDatabase.create("foo_db", databaseType, Collections.singletonMap("", databaseType),
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertNotNull(actual.getSchema("foo_db"));
    }
}
