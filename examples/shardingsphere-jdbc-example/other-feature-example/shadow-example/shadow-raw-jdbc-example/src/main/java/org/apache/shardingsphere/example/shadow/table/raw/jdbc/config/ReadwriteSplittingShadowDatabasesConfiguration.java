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

package org.apache.shardingsphere.example.shadow.table.raw.jdbc.config;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ReadwriteSplittingShadowDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(4, 1);
        dataSourceMap.put("write_ds", DataSourceUtil.createDataSource("demo_write_ds"));
        dataSourceMap.put("read_ds", DataSourceUtil.createDataSource("demo_read_ds"));
        dataSourceMap.put("shadow_write_ds", DataSourceUtil.createDataSource("demo_shadow_write_ds"));
        dataSourceMap.put("shadow_read_ds", DataSourceUtil.createDataSource("demo_shadow_read_ds"));
        ShadowRuleConfiguration shadowRuleConfig = new ShadowRuleConfiguration("shadow", Arrays.asList("write_ds", "read_ds"), Arrays.asList("shadow_write_ds", "shadow_read_ds"));
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Arrays.asList(shadowRuleConfig, getReadwriteSplittingRuleConfiguration()), null);
    }
    
    private ReadwriteSplittingRuleConfiguration getReadwriteSplittingRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration config = new ReadwriteSplittingDataSourceRuleConfiguration("pr_ds", "", "write_ds", Collections.singletonList("read_ds"), null);
        return new ReadwriteSplittingRuleConfiguration(Collections.singletonList(config), Collections.emptyMap());
    }
}
