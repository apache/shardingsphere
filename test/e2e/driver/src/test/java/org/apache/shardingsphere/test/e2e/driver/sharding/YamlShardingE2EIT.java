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

package org.apache.shardingsphere.test.e2e.driver.sharding;

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

public final class YamlShardingE2EIT extends AbstractYamlDataSourceE2EIT {
    
    @ParameterizedTest(name = "{index}:{0}-{1}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertWithDataSource(final String filePath, final boolean hasDataSource) throws Exception {
        File yamlFile = new File(Objects.requireNonNull(YamlShardingE2EIT.class.getResource(filePath)).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
        } else {
            Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
            dataSourceMap.put("db0", createDataSource("db0"));
            dataSourceMap.put("db1", createDataSource("db1"));
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, yamlFile);
        }
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(String.format("INSERT INTO t_order(user_id,status) values(%d, %s)", 10, "'insert'"));
            statement.executeQuery("SELECT * FROM t_order");
            statement.executeQuery("SELECT * FROM t_order_item");
            statement.executeQuery("SELECT * FROM config");
        }
        ((ShardingSphereDataSource) dataSource).close();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of("/yaml/integrate/sharding/configWithDataSourceWithoutProps.yaml", true),
                    Arguments.of("/yaml/integrate/sharding/configWithoutDataSourceWithoutProps.yaml", false),
                    Arguments.of("/yaml/integrate/sharding/configWithDataSourceWithProps.yaml", true),
                    Arguments.of("/yaml/integrate/sharding/configWithoutDataSourceWithProps.yaml", false));
        }
    }
}
