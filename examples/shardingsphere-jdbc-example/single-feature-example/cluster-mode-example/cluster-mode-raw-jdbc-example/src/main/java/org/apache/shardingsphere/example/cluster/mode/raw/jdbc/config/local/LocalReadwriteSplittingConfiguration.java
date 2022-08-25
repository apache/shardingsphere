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

package org.apache.shardingsphere.example.cluster.mode.raw.jdbc.config.local;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Properties;

public final class LocalReadwriteSplittingConfiguration implements ExampleConfiguration {
    
    private final ModeConfiguration modeConfig;
    
    public LocalReadwriteSplittingConfiguration(final ModeConfiguration modeConfig) {
        this.modeConfig = modeConfig;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceConfig = new ReadwriteSplittingDataSourceRuleConfiguration(
                "demo_readwrite_splitting_ds", new StaticReadwriteSplittingStrategyConfiguration("demo_write_ds",
                Arrays.asList("demo_read_ds_0", "demo_read_ds_1")), null, null);
        ReadwriteSplittingRuleConfiguration ruleConfig = new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceConfig), Collections.emptyMap());
        return ShardingSphereDataSourceFactory.createDataSource(modeConfig, createDataSourceMap(), Collections.singleton(ruleConfig), new Properties());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("demo_write_ds", DataSourceUtil.createDataSource("demo_write_ds"));
        result.put("demo_read_ds_0", DataSourceUtil.createDataSource("demo_read_ds_0"));
        result.put("demo_read_ds_1", DataSourceUtil.createDataSource("demo_read_ds_1"));
        return result;
    }
}
