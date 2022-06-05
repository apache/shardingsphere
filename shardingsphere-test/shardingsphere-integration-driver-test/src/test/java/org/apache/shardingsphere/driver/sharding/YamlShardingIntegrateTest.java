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

package org.apache.shardingsphere.driver.sharding;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.AbstractYamlDataSourceTest;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class YamlShardingIntegrateTest extends AbstractYamlDataSourceTest {
    
    private final String filePath;
    
    private final boolean hasDataSource;
    
    @Parameters(name = "{index}:{0}-{1}")
    public static Collection<Object[]> init() {
        return Arrays.asList(new Object[][]{
                {"/yaml/integrate/sharding/configWithDataSourceWithoutProps.yaml", true},
                {"/yaml/integrate/sharding/configWithoutDataSourceWithoutProps.yaml", false},
                {"/yaml/integrate/sharding/configWithDataSourceWithProps.yaml", true},
                {"/yaml/integrate/sharding/configWithoutDataSourceWithProps.yaml", false},
        });
    }
    
    @Test
    public void assertWithDataSource() throws Exception {
        File yamlFile = new File(Objects.requireNonNull(YamlShardingIntegrateTest.class.getResource(filePath)).toURI());
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
}
