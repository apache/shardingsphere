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

package org.apache.shardingsphere.example.orchestration.raw.jdbc.config.local;

import org.apache.shardingsphere.driver.governance.api.OrchestrationShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class LocalShadowConfiguration implements ExampleConfiguration {
    
    private final OrchestrationConfiguration orchestrationConfiguration;
    
    public LocalShadowConfiguration(final OrchestrationConfiguration orchestrationConfiguration) {
        this.orchestrationConfiguration = orchestrationConfiguration;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return OrchestrationShardingSphereDataSourceFactory.createDataSource(
                createDataSourceMap(), Collections.singleton(getShadowRuleConfiguration()), new Properties(), orchestrationConfiguration);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds", DataSourceUtil.createDataSource("ds"));
        result.put("shadow_ds", DataSourceUtil.createDataSource("shadow_ds"));
        return result;
    }
    
    private ShadowRuleConfiguration getShadowRuleConfiguration() {
        return new ShadowRuleConfiguration("shadow", Collections.singletonMap("ds", "shadow_ds"));
    }
}
