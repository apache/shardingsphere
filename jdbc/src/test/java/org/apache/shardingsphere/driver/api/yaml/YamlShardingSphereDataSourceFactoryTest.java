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
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlShardingSphereDataSourceFactoryTest {
    
    private static final String YAML_FILE = "config/factory/database-for-factory-test.yaml";
    
    @Test
    void assertCreateDataSourceWithFile() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(SystemResourceFileUtils.getPath(YAML_FILE).toFile()));
    }
    
    @Test
    void assertCreateDataSourceWithBytes() throws SQLException, IOException {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(SystemResourceFileUtils.readFile(YAML_FILE).getBytes()));
    }
    
    @Test
    void assertCreateDataSourceWithFileForExternalDataSources() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, SystemResourceFileUtils.getPath(YAML_FILE).toFile()));
    }
    
    @Test
    void assertCreateDataSourceWithFileForExternalSingleDataSource() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), SystemResourceFileUtils.getPath(YAML_FILE).toFile()));
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalDataSources() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, SystemResourceFileUtils.readFile(YAML_FILE).getBytes()));
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalSingleDataSource() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), SystemResourceFileUtils.readFile(YAML_FILE).getBytes()));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertDataSource(final DataSource dataSource) {
        assertThat(Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("databaseName"), dataSource), is("logic_db"));
    }
}
