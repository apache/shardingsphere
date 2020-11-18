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
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ReplicaQueryShadowDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(4, 1);
        dataSourceMap.put("primary_ds", DataSourceUtil.createDataSource("demo_primary_ds"));
        dataSourceMap.put("replica_ds", DataSourceUtil.createDataSource("demo_replica_ds"));
        dataSourceMap.put("shadow_primary_ds", DataSourceUtil.createDataSource("demo_shadow_primary_ds"));
        dataSourceMap.put("shadow_replica_ds", DataSourceUtil.createDataSource("demo_shadow_replica_ds"));
        ShadowRuleConfiguration shadowRuleConfig = new ShadowRuleConfiguration("shadow", Arrays.asList("primary_ds", "replica_ds"), Arrays.asList("shadow_primary_ds", "shadow_replica_ds"));
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Arrays.asList(shadowRuleConfig, getReplicaQueryRuleConfiguration()), null);
    }
    
    private ReplicaQueryRuleConfiguration getReplicaQueryRuleConfiguration() {
        ReplicaQueryDataSourceRuleConfiguration config = new ReplicaQueryDataSourceRuleConfiguration("pr_ds", "primary_ds", Collections.singletonList("replica_ds"), null);
        return new ReplicaQueryRuleConfiguration(Collections.singletonList(config), Collections.emptyMap());
    }
}
