/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.api;

import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import io.shardingsphere.api.config.rule.TableRuleConfiguration;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingDataSourceFactoryTest {
    
    @Before
    public void setUp() {
        ConfigMapContext.getInstance().getConfigMap().clear();
    }
    
    @Test
    public void assertCreateDataSourceWithShardingRuleAndConfigMapAndProperties() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfig();
        Properties props = new Properties();
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        DataSource dataSource = ShardingDataSourceFactory.createDataSource(getDataSourceMap(), shardingRuleConfig, configMap, props);
        assertNotNull(getShardingRule(dataSource));
        assertThat(ConfigMapContext.getInstance().getConfigMap(), is(configMap));
        assertThat(getShardingProperties(dataSource), is(props));
    }
    
    private Map<String, DataSource> getDataSourceMap() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.getResultSet()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(Mockito.anyString())).thenReturn(resultSet);
        when(statement.getConnection()).thenReturn(connection);
        when(statement.getConnection().getMetaData().getTables(
                ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String[]>any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        Map<String, DataSource> result = new HashMap<>(1);
        when(statement.getConnection().getMetaData().getURL()).thenReturn("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.put("ds", dataSource);
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfig() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds.table_${0..2}");
        result.getTableRuleConfigs().add(tableRuleConfig);
        return result;
    }
    
    @SneakyThrows
    private ShardingRule getShardingRule(final DataSource dataSource) {
        Field field = dataSource.getClass().getDeclaredField("shardingContext");
        field.setAccessible(true);
        return ((ShardingContext) field.get(dataSource)).getShardingRule();
    }
    
    @SneakyThrows
    private Properties getShardingProperties(final DataSource dataSource) {
        Field shardingContextField = dataSource.getClass().getDeclaredField("shardingContext");
        shardingContextField.setAccessible(true);
        ShardingContext shardingContext = (ShardingContext) shardingContextField.get(dataSource);
        return shardingContext.getShardingProperties().getProps();
    }
}
