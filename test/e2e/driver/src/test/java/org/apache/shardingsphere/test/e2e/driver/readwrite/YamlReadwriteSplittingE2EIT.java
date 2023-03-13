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

package org.apache.shardingsphere.test.e2e.driver.readwrite;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.test.e2e.driver.AbstractYamlDataSourceE2EIT;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class YamlReadwriteSplittingE2EIT extends AbstractYamlDataSourceE2EIT {
    
    @ParameterizedTest(name = "{index}:{0}-{1}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertWithDataSource(final String filePath, final boolean hasDataSource) throws Exception {
        File yamlFile = new File(Objects.requireNonNull(YamlReadwriteSplittingE2EIT.class.getResource(filePath)).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
        } else {
            Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
            dataSourceMap.put("db_write", createDataSource("db_write"));
            dataSourceMap.put("read_ds_0", createDataSource("read_ds_0"));
            dataSourceMap.put("read_ds_1", createDataSource("read_ds_1"));
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, yamlFile);
        }
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT * FROM t_order");
            statement.executeQuery("SELECT * FROM t_order_item");
            statement.executeQuery("SELECT * FROM t_config");
        }
        ((ShardingSphereDataSource) dataSource).close();
    }
    
    @ParameterizedTest(name = "{index}:{0}-{1}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertWithDataSourceByYamlBytes(final String filePath, final boolean hasDataSource) throws Exception {
        File yamlFile = new File(Objects.requireNonNull(YamlReadwriteSplittingE2EIT.class.getResource(filePath)).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
        } else {
            Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1);
            dataSourceMap.put("db_write", createDataSource("db_write"));
            dataSourceMap.put("read_ds_0", createDataSource("read_ds_0"));
            dataSourceMap.put("read_ds_1", createDataSource("read_ds_1"));
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, yamlFile);
        }
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT * FROM t_order");
            statement.executeQuery("SELECT * FROM t_order_item");
            statement.executeQuery("SELECT * FROM t_config");
        }
        ((ShardingSphereDataSource) dataSource).close();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of("/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithoutProps.yaml", true),
                    Arguments.of("/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithoutProps.yaml", true),
                    Arguments.of("/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithProps.yaml", true),
                    Arguments.of("/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithProps.yaml", true));
        }
    }
}
