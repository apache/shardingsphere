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

package io.shardingjdbc.core.yaml.sharding;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.BindingTableRule;
import io.shardingjdbc.core.rule.ShardingRule;
import org.apache.commons.dbcp.BasicDataSource;
import org.h2.Driver;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class YamlShardingDataSourceTest {
    
    @Test
    public void assertAll() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException, SQLException {
        ShardingRule shardingRule = getShardingRule("/yaml/config/config-all.yaml");
        assertThat(shardingRule.getTableRules().size(), is(3));
        assertThat(shardingRule.getBindingTableRules().size(), is(1));
        assertThat(Arrays.asList(shardingRule.getTableRules().toArray()), hasItems(shardingRule.getBindingTableRules().iterator().next().getTableRules().toArray()));
        assertThat(shardingRule.getDefaultDataSourceName(), is("db0"));
    }
    
    @Test
    public void assertMin() throws IOException, ReflectiveOperationException, URISyntaxException, SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", createDataSource());
        ShardingRule shardingRule = getShardingRule(dataSourceMap, "/yaml/config/config-min.yaml");
        assertThat(shardingRule.getTableRules().size(), is(1));
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void assertClassNotFound() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException, SQLException {
        getShardingRule("/yaml/config/config-classNotFound.yaml");
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertBindingError() throws IOException, ReflectiveOperationException, URISyntaxException, SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", createDataSource());
        ShardingRule shardingRule = getShardingRule(dataSourceMap, "/yaml/config/config-bindingError.yaml");
        for (BindingTableRule each : shardingRule.getBindingTableRules()) {
            each.getBindingActualTable("ds", "t_order_no_exist", "t_order_no_exist_0r");
        }
    }
    
    private ShardingRule getShardingRule(final String fileName) throws NoSuchFieldException, IllegalAccessException, URISyntaxException, IOException, SQLException {
        return getShardingRule(ShardingDataSourceFactory.createDataSource(new File(getClass().getResource(fileName).toURI())));
    }
    
    private ShardingRule getShardingRule(final Map<String, DataSource> dataSourceMap, final String fileName) throws ReflectiveOperationException, URISyntaxException, IOException, SQLException {
        return getShardingRule(ShardingDataSourceFactory.createDataSource(dataSourceMap, new File(getClass().getResource(fileName).toURI())));
    }
    
    private ShardingRule getShardingRule(final DataSource shardingDataSource) throws NoSuchFieldException, IllegalAccessException {
        Field field = ShardingDataSource.class.getDeclaredField("shardingContext");
        field.setAccessible(true);
        ShardingContext shardingContext = (ShardingContext) field.get(shardingDataSource);
        return shardingContext.getShardingRule();
    }
    
    private DataSource createDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(Driver.class.getName());
        result.setUrl("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
}
