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

package org.apache.shardingsphere.driver.readwrite;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import java.util.Objects;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class YamlReadwriteSplittingIntegrateTest extends AbstractYamlDataSourceTest {
    
    private final String filePath;
    
    private final boolean hasDataSource;
    
    @Parameters(name = "{index}:{0}-{1}")
    public static Collection<Object[]> init() {
        return Arrays.asList(new Object[][]{
                {"/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithoutProps.yaml", true},
                {"/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithoutProps.yaml", true},
                {"/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithProps.yaml", true},
                {"/yaml/integrate/readwrite_splitting/configWithReadwriteSplittingDataSourceWithProps.yaml", true},
        });
    }
    
    @Test
    public void assertWithDataSource() throws Exception {
        File yamlFile = new File(Objects.requireNonNull(YamlReadwriteSplittingIntegrateTest.class.getResource(filePath)).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
        } else {
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(
                    Maps.asMap(Sets.newHashSet("db_write", "read_ds_0", "read_ds_1"), AbstractYamlDataSourceTest::createDataSource), yamlFile);
        }
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT * FROM t_order");
            statement.executeQuery("SELECT * FROM t_order_item");
            statement.executeQuery("SELECT * FROM t_config");
        }
        ((ShardingSphereDataSource) dataSource).close();
    }
    
    @Test
    public void assertWithDataSourceByYamlBytes() throws Exception {
        File yamlFile = new File(Objects.requireNonNull(YamlReadwriteSplittingIntegrateTest.class.getResource(filePath)).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
        } else {
            dataSource = YamlShardingSphereDataSourceFactory.createDataSource(
                    Maps.asMap(Sets.newHashSet("db_write", "read_ds_0", "read_ds_1"), AbstractYamlDataSourceTest::createDataSource), yamlFile);
        }
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT * FROM t_order");
            statement.executeQuery("SELECT * FROM t_order_item");
            statement.executeQuery("SELECT * FROM t_config");
        }
        ((ShardingSphereDataSource) dataSource).close();
    }
}
