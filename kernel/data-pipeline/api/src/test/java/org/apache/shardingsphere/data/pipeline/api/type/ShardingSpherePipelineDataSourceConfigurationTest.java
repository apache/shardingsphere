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

package org.apache.shardingsphere.data.pipeline.api.type;

import org.apache.shardingsphere.data.pipeline.spi.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class ShardingSpherePipelineDataSourceConfigurationTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstance() {
        ShardingSpherePipelineDataSourceConfiguration actual = createShardingSpherePipelineDataSourceConfiguration();
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getType(), is(ShardingSpherePipelineDataSourceConfiguration.TYPE));
        assertThat(actual.getDataSourceConfiguration(), instanceOf(YamlRootConfiguration.class));
        Map<String, Map<String, Object>> dataSources = actual.getRootConfig().getDataSources();
        assertThat(dataSources.size(), is(2));
        assertTrue(dataSources.containsKey("ds_0"));
        assertTrue(dataSources.containsKey("ds_1"));
        for (Map<String, Object> queryProps : dataSources.values()) {
            for (String each : Arrays.asList("minPoolSize", "minimumIdle")) {
                assertThat(queryProps.get(each), is("1"));
            }
        }
    }
    
    @Test
    void assertGetActualDataSourceConfiguration() {
        ShardingSpherePipelineDataSourceConfiguration config = createShardingSpherePipelineDataSourceConfiguration();
        StandardPipelineDataSourceConfiguration actual = config.getActualDataSourceConfiguration("ds_0");
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
    }
    
    private ShardingSpherePipelineDataSourceConfiguration createShardingSpherePipelineDataSourceConfiguration() {
        JdbcQueryPropertiesExtension queryPropsExtension = mock(JdbcQueryPropertiesExtension.class);
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(queryPropsExtension));
        YamlRootConfiguration rootConfig = YamlEngine.unmarshal(getDataSourceYAML(), YamlRootConfiguration.class, true);
        return new ShardingSpherePipelineDataSourceConfiguration(rootConfig);
    }
    
    private String getDataSourceYAML() {
        return "dataSources:\n"
                + "  ds_0:\n"
                + "    minPoolSize: 20\n"
                + "    minimumIdle: 20\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mock://127.0.0.1/ds_0\n"
                + "  ds_1:\n"
                + "    minPoolSize: 20\n"
                + "    minimumIdle: 20\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    jdbcUrl: jdbc:mock://127.0.0.1/ds_1\n";
    }
}
