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

package org.apache.shardingsphere.driver.api;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingSphereDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceWithMultipleActualDataSources() throws SQLException {
        Properties props = new Properties();
        ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource(
                getDataSourceMap(), Collections.singleton(createShardingRuleConfiguration()), props);
        assertThat(dataSource.getMetaDataContexts().getProps().getProps(), is(props));
    }
    
    @Test
    public void assertCreateDataSourceWithSingleActualDataSource() throws SQLException {
        Properties props = new Properties();
        ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource(
                new MockedDataSource(), Collections.singleton(createShardingRuleConfiguration()), props);
        assertThat(dataSource.getMetaDataContexts().getProps().getProps(), is(props));
    }
    
    private Map<String, DataSource> getDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        result.put(DefaultSchema.LOGIC_NAME, new MockedDataSource());
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("logicTable", "logic_db.table_${0..2}"));
        return result;
    }
}
