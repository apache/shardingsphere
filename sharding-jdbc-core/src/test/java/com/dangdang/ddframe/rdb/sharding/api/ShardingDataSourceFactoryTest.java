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

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.ShardingContext;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceWithShardingRuleOnly() throws SQLException, NoSuchFieldException, IllegalAccessException {
        ShardingRule shardingRule = createShardingRule();
        assertThat(getShardingRule(ShardingDataSourceFactory.createDataSource(shardingRule)), is(shardingRule));
    }
    
    @Test
    public void assertCreateDataSourceWithShardingRuleAndProperties() throws SQLException, NoSuchFieldException, IllegalAccessException {
        ShardingRule shardingRule = createShardingRule();
        Properties props = new Properties();
        DataSource dataSource = ShardingDataSourceFactory.createDataSource(shardingRule, props);
        assertThat(getShardingRule(dataSource), is(shardingRule));
        assertThat(getShardingProperties(dataSource), is(props));
    }
    
    private ShardingRule createShardingRule() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", dataSource);
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule tableRule = TableRule.builder("logicTable").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(dataSourceRule).build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(tableRule)).build();
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
