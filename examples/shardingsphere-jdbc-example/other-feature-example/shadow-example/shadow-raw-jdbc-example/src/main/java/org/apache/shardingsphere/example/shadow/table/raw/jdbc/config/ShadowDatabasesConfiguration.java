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

import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ShadowDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", Collections.singletonList("ds"), Collections.singletonList("ds_0"));
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds", DataSourceUtil.createDataSource("demo_ds"));
        dataSourceMap.put("ds_0", DataSourceUtil.createDataSource("shadow_demo_ds"));
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shadowRuleConfiguration), null);
    }
}
