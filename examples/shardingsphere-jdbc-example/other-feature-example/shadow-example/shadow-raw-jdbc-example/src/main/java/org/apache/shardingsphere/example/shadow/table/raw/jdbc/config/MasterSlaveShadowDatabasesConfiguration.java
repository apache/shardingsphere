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
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.rule.MasterSlaveDataSourceRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MasterSlaveShadowDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_master", DataSourceUtil.createDataSource("demo_ds_master"));
        dataSourceMap.put("ds_slave", DataSourceUtil.createDataSource("demo_ds_slave"));
        dataSourceMap.put("shadow_ds_master", DataSourceUtil.createDataSource("demo_shadow_ds_master"));
        dataSourceMap.put("shadow_ds_slave", DataSourceUtil.createDataSource("demo_shadow_ds_slave"));
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", Arrays.asList("ds_master", "ds_slave"), Arrays.asList("shadow_ds_master", "shadow_ds_slave"));
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Arrays.asList(shadowRuleConfiguration, getMasterSlaveRuleConfiguration()), null);
    }
    
    private MasterSlaveRuleConfiguration getMasterSlaveRuleConfiguration() {
        MasterSlaveDataSourceRuleConfiguration masterSlaveDataSourceRuleConfiguration = new MasterSlaveDataSourceRuleConfiguration("ds_ms", "ds_master", Collections.singletonList("ds_slave"), null);
        return new MasterSlaveRuleConfiguration(Collections.singletonList(masterSlaveDataSourceRuleConfiguration), Collections.emptyMap());
    }
}
