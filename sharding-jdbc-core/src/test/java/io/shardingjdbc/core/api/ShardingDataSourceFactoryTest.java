/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.api;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.rule.ShardingRule;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceWithShardingRuleAndConfigMapAndProperties() throws SQLException, NoSuchFieldException, IllegalAccessException {
        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfig();
        Properties props = new Properties();
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        DataSource dataSource = ShardingDataSourceFactory.createDataSource(getDataSourceMap(), shardingRuleConfig, configMap, props);
        assertNotNull(getShardingRule(dataSource));
        assertThat(ConfigMapContext.getInstance().getShardingConfig(), is(configMap));
        assertThat(getShardingProperties(dataSource), is(props));
    }
    
    private Map<String, DataSource> getDataSourceMap() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        Map<String, DataSource> result = new HashMap<>(1);
        result.put("ds", dataSource);
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfig() throws SQLException {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logicTable");
        tableRuleConfig.setActualDataNodes("ds.table_${0..2}");
        result.getTableRuleConfigs().add(tableRuleConfig);
        return result;
    }
    
    private ShardingRule getShardingRule(final DataSource dataSource) throws NoSuchFieldException, IllegalAccessException {
        Field field = dataSource.getClass().getDeclaredField("shardingContext");
        field.setAccessible(true);
        return ((ShardingContext) field.get(dataSource)).getShardingRule();
    }
    
    private Properties getShardingProperties(final DataSource dataSource) throws NoSuchFieldException, IllegalAccessException {
        Field shardingPropertiesField = dataSource.getClass().getDeclaredField("shardingProperties");
        shardingPropertiesField.setAccessible(true);
        Field propsField = shardingPropertiesField.get(dataSource).getClass().getDeclaredField("props");
        propsField.setAccessible(true);
        return (Properties) propsField.get(shardingPropertiesField.get(dataSource));
    }
}
