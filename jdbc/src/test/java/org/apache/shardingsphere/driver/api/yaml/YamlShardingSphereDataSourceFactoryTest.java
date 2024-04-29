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
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlShardingSphereDataSourceFactoryTest {
    
    @Test
    void assertCreateDataSourceWithFile() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new File(getYamlFileUrl().toURI())));
    }
    
    @Test
    void assertCreateDataSourceWithBytes() throws SQLException, IOException {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(readFile(getYamlFileUrl()).getBytes()));
    }
    
    @Test
    void assertCreateDataSourceWithFileForExternalDataSources() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, new File(getYamlFileUrl().toURI())));
    }
    
    @Test
    void assertCreateDataSourceWithFileForExternalSingleDataSource() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), new File(getYamlFileUrl().toURI())));
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalDataSources() throws Exception {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, readFile(getYamlFileUrl()).getBytes()));
    }
    
    @Test
    void assertCreateDataSourceWithBytesForExternalSingleDataSource() throws Exception {
        assertDataSource(YamlShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), readFile(getYamlFileUrl()).getBytes()));
    }
    
    private URL getYamlFileUrl() {
        return Objects.requireNonNull(YamlShardingSphereDataSourceFactoryTest.class.getResource("/config/factory/database-for-factory-test.yaml"));
    }
    
    private String readFile(final URL url) throws IOException {
        StringBuilder result = new StringBuilder();
        try (
                FileReader fileReader = new FileReader(url.getFile());
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                result.append(line).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertDataSource(final DataSource dataSource) {
        assertThat(Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("databaseName"), dataSource), is("logic_db"));
    }
}
