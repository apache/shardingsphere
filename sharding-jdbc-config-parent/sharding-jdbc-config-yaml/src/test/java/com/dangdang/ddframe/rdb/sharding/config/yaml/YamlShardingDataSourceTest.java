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

package com.dangdang.ddframe.rdb.sharding.config.yaml;

import com.dangdang.ddframe.rdb.sharding.api.rule.DynamicDataNode;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.config.yaml.api.YamlShardingDataSource;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingContext;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.h2.Driver;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Slf4j
public class YamlShardingDataSourceTest {
    
    @Test
    public void assertAll() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException {
        ShardingRule shardingRule = getShardingRule("/config/config-all.yaml");
        assertThat(shardingRule.getTableRules().size(), is(3));
        assertThat(shardingRule.getBindingTableRules().size(), is(1));
        assertThat(Arrays.asList(shardingRule.getTableRules().toArray()), hasItems(shardingRule.getBindingTableRules().iterator().next().getTableRules().toArray()));
        assertThat(shardingRule.getDataSourceRule().getDefaultDataSourceName(), is("db0"));
    }
    
    @Test
    public void assertMin() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", createDataSource());
        ShardingRule shardingRule = getShardingRule(dataSourceMap, "/config/config-min.yaml");
        assertThat(shardingRule.getTableRules().size(), is(1));
    }
    
    @Test
    public void assertDynamic() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", createDataSource());
        ShardingRule shardingRule = getShardingRule(dataSourceMap, "/config/config-dynamic.yaml");
        int i = 0;
        for (TableRule each : shardingRule.getTableRules()) {
            i++;
            assertThat(each.getActualTables().size(), is(2));
            assertThat(each.getActualTables(), hasItem(new DynamicDataNode("db0")));
            assertThat(each.getActualTables(), hasItem(new DynamicDataNode("db1")));
            switch (i) {
                case 1:
                    assertThat(each.getLogicTable(), is("config"));
                    break;
                case 2:
                    assertThat(each.getLogicTable(), is("t_order"));
                    break;
                case 3:
                    assertThat(each.getLogicTable(), is("t_order_item"));
                    break;
                default:
                    fail();
            }
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertClassNotFound() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException {
        getShardingRule("/config/config-classNotFound.yaml");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertBindingError() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", createDataSource());
        ShardingRule shardingRule = getShardingRule(dataSourceMap, "/config/config-bindingError.yaml");
        for (TableRule tableRule : shardingRule.getBindingTableRules().iterator().next().getTableRules()) {
            log.info(tableRule.toString());
        }
    }
    
    private ShardingRule getShardingRule(final String fileName) throws NoSuchFieldException, IllegalAccessException, URISyntaxException, IOException {
        return getShardingRule(new YamlShardingDataSource(new File(getClass().getResource(fileName).toURI())));
    }
    
    private ShardingRule getShardingRule(final Map<String, DataSource> dataSourceMap, final String fileName) throws NoSuchFieldException, IllegalAccessException, URISyntaxException, IOException {
        return getShardingRule(new YamlShardingDataSource(dataSourceMap, new File(getClass().getResource(fileName).toURI())));
    }
    
    private ShardingRule getShardingRule(final ShardingDataSource shardingDataSource) throws NoSuchFieldException, IllegalAccessException, URISyntaxException, IOException {
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
