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

package org.apache.shardingsphere.driver.jdbc.core.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.state.instance.InstanceState;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereDataSourceTest {
    
    @Test
    void assertNewConstructorWithModeConfigurationOnly() throws Exception {
        try (ShardingSphereDataSource actual = new ShardingSphereDataSource("foo_db", null)) {
            ContextManager contextManager = getContextManager(actual);
            assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db"));
            assertThat(contextManager.getStateContext().getState(), is(ShardingSphereState.OK));
            assertThat(contextManager.getComputeNodeInstanceContext().getInstance().getState().getCurrentState(), is(InstanceState.OK));
            assertTrue(contextManager.getStorageUnits("foo_db").isEmpty());
        }
    }
    
    @Test
    void assertNewConstructorWithAllArguments() throws Exception {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        try (ShardingSphereDataSource actual = createShardingSphereDataSource(new MockedDataSource(connection))) {
            ContextManager contextManager = getContextManager(actual);
            assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db"));
            assertThat(contextManager.getStateContext().getState(), is(ShardingSphereState.OK));
            assertThat(contextManager.getComputeNodeInstanceContext().getInstance().getState().getCurrentState(), is(InstanceState.OK));
            assertThat(contextManager.getStorageUnits("foo_db").size(), is(1));
            assertThat(contextManager.getStorageUnits("foo_db").get("ds").getDataSource().getConnection().getMetaData().getURL(), is("jdbc:mock://127.0.0.1/foo_ds"));
        }
    }
    
    @Test
    void assertRemoveGlobalRuleConfiguration() throws Exception {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        CacheOption cacheOption = new CacheOption(1024, 1024L);
        SQLParserRuleConfiguration sqlParserRuleConfig = new SQLParserRuleConfiguration(cacheOption, cacheOption);
        try (
                ShardingSphereDataSource actual = new ShardingSphereDataSource("foo_db",
                        null, Collections.singletonMap("ds", new MockedDataSource(connection)), Arrays.asList(mock(ShardingRuleConfiguration.class), sqlParserRuleConfig), new Properties())) {
            assertThat(getContextManager(actual).getMetaDataContexts().getMetaData().getDatabase("foo_db").getRuleMetaData().getConfigurations().size(), is(2));
        }
    }
    
    @Test
    void assertGetConnectionWithUsernameAndPassword() throws Exception {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        try (ShardingSphereDataSource actual = createShardingSphereDataSource(new MockedDataSource(connection))) {
            assertThat(((ShardingSphereConnection) actual.getConnection("", "")).getDatabaseConnectionManager().getConnections("foo_db", "ds", 0, 1, ConnectionMode.MEMORY_STRICTLY)
                    .get(0), is(connection));
        }
    }
    
    private ShardingSphereDataSource createShardingSphereDataSource(final DataSource dataSource) throws SQLException {
        return new ShardingSphereDataSource("foo_db", null, Collections.singletonMap("ds", dataSource), Collections.singleton(mock(RuleConfiguration.class)), new Properties());
    }
    
    @Test
    void assertEmptyDataSourceMap() throws Exception {
        try (ShardingSphereDataSource actual = new ShardingSphereDataSource("foo_db", null)) {
            assertTrue(getContextManager(actual).getStorageUnits("foo_db").isEmpty());
            assertThat(actual.getLoginTimeout(), is(0));
        }
    }
    
    @Test
    void assertNotEmptyDataSourceMap() throws Exception {
        try (ShardingSphereDataSource actual = createShardingSphereDataSource(createHikariDataSource())) {
            assertThat(getContextManager(actual).getStorageUnits("foo_db").size(), is(1));
            assertThat(actual.getLoginTimeout(), is(15));
        }
    }
    
    @Test
    void assertSetLoginTimeout() throws Exception {
        try (ShardingSphereDataSource actual = createShardingSphereDataSource(createHikariDataSource())) {
            actual.setLoginTimeout(30);
            assertThat(actual.getLoginTimeout(), is(30));
        }
    }
    
    @Test
    void assertClose() throws Exception {
        try (HikariDataSource dataSource = createHikariDataSource()) {
            ShardingSphereDataSource actual = createShardingSphereDataSource(dataSource);
            actual.close();
            Map<StorageNode, DataSource> dataSourceMap = getContextManager(actual).getMetaDataContexts().getMetaData()
                    .getDatabase("foo_db").getResourceMetaData().getDataSources();
            assertTrue(((HikariDataSource) dataSourceMap.get(new StorageNode("ds"))).isClosed());
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ContextManager getContextManager(final ShardingSphereDataSource dataSource) {
        return (ContextManager) Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("contextManager"), dataSource);
    }
    
    private HikariDataSource createHikariDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setMinimumIdle(2);
        result.setConnectionTimeout(15L * 1000L);
        result.setIdleTimeout(40L * 1000L);
        return result;
    }
}
