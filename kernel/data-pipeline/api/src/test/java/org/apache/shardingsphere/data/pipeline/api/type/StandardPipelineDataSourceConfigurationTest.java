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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class StandardPipelineDataSourceConfigurationTest {
    
    private static final String JDBC_URL = "jdbc:mock://127.0.0.1/foo_ds";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "123456";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstanceWithYAML() {
        assertPipelineDataSourceConfiguration(new StandardPipelineDataSourceConfiguration(SystemResourceFileUtils.readFile("yaml/standard-pipeline-datasource-config.yaml")));
    }
    
    @Test
    void assertNewInstanceWithMap() {
        JdbcQueryPropertiesExtension queryPropsExtension = mock(JdbcQueryPropertiesExtension.class);
        when(DatabaseTypedSPILoader.findService(JdbcQueryPropertiesExtension.class, databaseType)).thenReturn(Optional.of(queryPropsExtension));
        Map<String, Object> yamlDataSourceConfig = new HashMap<>(5, 1F);
        yamlDataSourceConfig.put("url", JDBC_URL);
        yamlDataSourceConfig.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        yamlDataSourceConfig.put("username", USERNAME);
        yamlDataSourceConfig.put("password", PASSWORD);
        yamlDataSourceConfig.put("minPoolSize", "20");
        Map<String, Object> backupDataSource = new HashMap<>(yamlDataSourceConfig);
        StandardPipelineDataSourceConfiguration actual = new StandardPipelineDataSourceConfiguration(yamlDataSourceConfig);
        assertParameterUnchanged(backupDataSource, yamlDataSourceConfig);
        assertPipelineDataSourceConfiguration(actual);
    }
    
    private void assertParameterUnchanged(final Map<String, Object> backupDataSource, final Map<String, Object> handledDataSource) {
        assertThat(handledDataSource.size(), is(backupDataSource.size()));
        for (Entry<String, Object> entry : backupDataSource.entrySet()) {
            Object actual = handledDataSource.get(entry.getKey());
            assertNotNull(actual, "value of '" + entry.getKey() + "' doesn't exist");
            assertThat("value of '" + entry.getKey() + "' doesn't match", actual, is(entry.getValue()));
        }
    }
    
    private void assertPipelineDataSourceConfiguration(final StandardPipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getType(), is("FIXTURE"));
        assertThat(actual.getType(), is(StandardPipelineDataSourceConfiguration.TYPE));
        DataSourcePoolProperties props = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(actual.getUrl(), is(JDBC_URL));
        assertThat(props.getPoolClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
        assertDataSourcePoolProperties(props);
    }
    
    private void assertDataSourcePoolProperties(final DataSourcePoolProperties props) {
        Map<String, Object> actual = new YamlDataSourceConfigurationSwapper().swapToMap(props);
        assertThat(actual.get("minPoolSize"), is("1"));
    }
}
