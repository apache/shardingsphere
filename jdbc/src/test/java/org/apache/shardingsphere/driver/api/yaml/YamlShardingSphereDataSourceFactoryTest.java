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

package org.apache.shardingsphere.driver.api.yaml;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class YamlShardingSphereDataSourceFactoryTest {
    
    private static final String YAML_FILE = "config/factory/database-for-factory-test.yaml";
    
    @Test
    void assertCreateDataSourceWithFile() throws SQLException, IOException {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(SystemResourceFileUtils.getPath(YAML_FILE).toFile()));
    }
    
    @Test
    void assertCreateDataSourceWithBytes() throws SQLException, IOException {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(SystemResourceFileUtils.readFile(YAML_FILE).getBytes()));
    }
    
    @Test
    void assertCreateDataSourceWithFileAndInvalidRules() {
        IllegalArgumentException expected = new IllegalArgumentException("Invalid rules");
        try (
                MockedStatic<DataSourcePoolCreator> mockedCreator = mockStatic(DataSourcePoolCreator.class);
                MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockConstruction(YamlRuleConfigurationSwapperEngine.class,
                        (mock, context) -> when(mock.swapToRuleConfigurations(anyCollection())).thenThrow(expected))) {
            IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                    () -> YamlShardingSphereDataSourceFactory.createDataSource(SystemResourceFileUtils.getPath(YAML_FILE).toFile()));
            assertThat(actual, sameInstance(expected));
            mockedCreator.verifyNoInteractions();
        }
    }
    
    @Test
    void assertCreateDataSourceWithBytesAndInvalidRules() throws IOException {
        byte[] yamlBytes = SystemResourceFileUtils.readFile(YAML_FILE).getBytes();
        IllegalArgumentException expected = new IllegalArgumentException("Invalid rules");
        try (
                MockedStatic<DataSourcePoolCreator> mockedCreator = mockStatic(DataSourcePoolCreator.class);
                MockedConstruction<YamlRuleConfigurationSwapperEngine> ignored = mockConstruction(YamlRuleConfigurationSwapperEngine.class,
                        (mock, context) -> when(mock.swapToRuleConfigurations(anyCollection())).thenThrow(expected))) {
            IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> YamlShardingSphereDataSourceFactory.createDataSource(yamlBytes));
            assertThat(actual, sameInstance(expected));
            mockedCreator.verifyNoInteractions();
        }
    }
    
    @Test
    void assertCreateDataSourceWithInvalidDatabaseNameBeforeCreatingDataSources() {
        try (MockedStatic<DataSourcePoolCreator> mockedCreator = mockStatic(DataSourcePoolCreator.class)) {
            assertThrows(IllegalArgumentException.class, () -> YamlShardingSphereDataSourceFactory.createDataSource("databaseName: logic-db".getBytes()));
            mockedCreator.verifyNoInteractions();
        }
    }
    
    @Test
    void assertCreateDataSourceWithFileForExternalDataSources() throws SQLException, IOException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, SystemResourceFileUtils.getPath(YAML_FILE).toFile()));
    }
    
    @Test
    void assertCreateDataSourceWithFileForExternalSingleDataSource() throws SQLException, IOException {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), SystemResourceFileUtils.getPath(YAML_FILE).toFile()));
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalDataSources() throws SQLException, IOException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, SystemResourceFileUtils.readFile(YAML_FILE).getBytes()));
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalSingleDataSource() throws SQLException, IOException {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), SystemResourceFileUtils.readFile(YAML_FILE).getBytes()));
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalDataSourcesIgnoringYamlDataSources() throws SQLException, IOException {
        MockedDataSource dataSource = new MockedDataSource();
        byte[] yamlBytes = "databaseName: logic_db\ndataSources:\n  unused:\n    url: ignored".getBytes();
        try (ShardingSphereDataSource actual = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(Collections.singletonMap("ds_0", dataSource), yamlBytes)) {
            assertDataSource(actual);
            assertFalse(dataSource.isClosed());
        }
        assertTrue(dataSource.isClosed());
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalSingleDataSourceWithDefaultDatabaseName() throws SQLException, IOException, ReflectiveOperationException {
        MockedDataSource dataSource = new MockedDataSource();
        byte[] yamlBytes = "dataSources:\n  unused:\n    url: ignored".getBytes();
        try (ShardingSphereDataSource actual = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSource, yamlBytes)) {
            assertThat(Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("databaseName"), actual), is(DefaultDatabase.LOGIC_NAME));
            assertFalse(dataSource.isClosed());
        }
        assertTrue(dataSource.isClosed());
    }
    
    @Test
    void assertCreateDataSourceWithHyphenDatabaseName() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), "databaseName: logic-db".getBytes()));
        assertThat(actual.getMessage(),
                is("Database name `logic-db` is invalid, the database name should start with a letter and can contain letters, numbers and underscores only."));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertDataSource(final DataSource dataSource) {
        assertThat(Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("databaseName"), dataSource), is("logic_db"));
    }
}
