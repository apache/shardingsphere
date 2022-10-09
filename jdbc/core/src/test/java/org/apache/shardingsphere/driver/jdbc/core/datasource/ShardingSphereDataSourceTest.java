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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.After;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereDataSourceTest {
    
    @After
    public void tearDown() {
        TransactionTypeHolder.set(null);
    }
    
    @Test
    public void assertNewConstructorWithModeConfigurationOnly() throws SQLException {
        ShardingSphereDataSource actual = new ShardingSphereDataSource(DefaultDatabase.LOGIC_NAME, null);
        ContextManager contextManager = getContextManager(actual);
        assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME));
        assertThat(contextManager.getInstanceContext().getInstance().getState().getCurrentState(), is(StateType.OK));
        assertTrue(contextManager.getDataSourceMap(DefaultDatabase.LOGIC_NAME).isEmpty());
    }
    
    @Test
    public void assertNewConstructorWithAllArguments() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        ShardingSphereDataSource actual = createShardingSphereDataSource(new MockedDataSource(connection));
        ContextManager contextManager = getContextManager(actual);
        assertNotNull(contextManager.getMetaDataContexts().getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME));
        assertThat(contextManager.getInstanceContext().getInstance().getState().getCurrentState(), is(StateType.OK));
        assertThat(contextManager.getDataSourceMap(DefaultDatabase.LOGIC_NAME).size(), is(1));
        assertThat(contextManager.getDataSourceMap(DefaultDatabase.LOGIC_NAME).get("ds").getConnection().getMetaData().getURL(), is("jdbc:mock://127.0.0.1/foo_ds"));
    }
    
    @Test
    public void assertRemoveGlobalRuleConfiguration() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        CacheOption cacheOption = new CacheOption(1024, 1024);
        SQLParserRuleConfiguration sqlParserRuleConfig = new SQLParserRuleConfiguration(true, cacheOption, cacheOption);
        ShardingSphereDataSource actual = new ShardingSphereDataSource(DefaultDatabase.LOGIC_NAME,
                null, Collections.singletonMap("ds", new MockedDataSource(connection)), Arrays.asList(mock(ShardingRuleConfiguration.class), sqlParserRuleConfig), new Properties());
        assertThat(getContextManager(actual).getMetaDataContexts().getMetaData().getDatabase(DefaultDatabase.LOGIC_NAME).getRuleMetaData().getConfigurations().size(), is(2));
    }
    
    @Test
    public void assertGetConnectionWithUsernameAndPassword() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
        assertThat(((ShardingSphereConnection) createShardingSphereDataSource(
                new MockedDataSource(connection)).getConnection("", "")).getConnectionManager().getConnections("ds", 1, ConnectionMode.MEMORY_STRICTLY).get(0), is(connection));
    }
    
    private ShardingSphereDataSource createShardingSphereDataSource(final DataSource dataSource) throws SQLException {
        return new ShardingSphereDataSource(DefaultDatabase.LOGIC_NAME, null, Collections.singletonMap("ds", dataSource), Collections.singleton(mock(RuleConfiguration.class)), new Properties());
    }
    
    @Test
    public void assertEmptyDataSourceMap() throws SQLException {
        ShardingSphereDataSource actual = new ShardingSphereDataSource(DefaultDatabase.LOGIC_NAME, null);
        assertTrue(getContextManager(actual).getDataSourceMap(DefaultDatabase.LOGIC_NAME).isEmpty());
        assertThat(actual.getLoginTimeout(), is(0));
    }
    
    @Test
    public void assertNotEmptyDataSourceMap() throws SQLException {
        ShardingSphereDataSource actual = createShardingSphereDataSource(createHikariDataSource());
        assertThat(getContextManager(actual).getDataSourceMap(DefaultDatabase.LOGIC_NAME).size(), is(1));
        assertThat(actual.getLoginTimeout(), is(15));
    }
    
    @Test
    public void assertSetLoginTimeout() throws SQLException {
        ShardingSphereDataSource actual = createShardingSphereDataSource(createHikariDataSource());
        actual.setLoginTimeout(30);
        assertThat(actual.getLoginTimeout(), is(30));
    }
    
    @Test
    public void assertClose() throws Exception {
        ShardingSphereDataSource actual = createShardingSphereDataSource(createHikariDataSource());
        actual.close();
        Map<String, DataSource> dataSourceMap = getContextManager(actual).getDataSourceMap(DefaultDatabase.LOGIC_NAME);
        assertTrue(((HikariDataSource) dataSourceMap.get("ds")).isClosed());
    }
    
    @Test
    public void assertCloseWithDataSourceNames() throws Exception {
        ShardingSphereDataSource actual = createShardingSphereDataSource(createHikariDataSource());
        actual.close(Collections.singleton("ds"));
        Map<String, DataSource> dataSourceMap = getContextManager(actual).getDataSourceMap(DefaultDatabase.LOGIC_NAME);
        assertTrue(((HikariDataSource) dataSourceMap.get("ds")).isClosed());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ContextManager getContextManager(final ShardingSphereDataSource dataSource) {
        Field field = ShardingSphereDataSource.class.getDeclaredField("contextManager");
        field.setAccessible(true);
        return (ContextManager) field.get(dataSource);
    }
    
    private DataSource createHikariDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setMinimumIdle(2);
        result.setConnectionTimeout(15 * 1000);
        result.setIdleTimeout(40 * 1000);
        return result;
    }
}
