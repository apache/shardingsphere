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

package org.apache.shardingsphere.driver.orchestration.api.yaml;

import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.junit.AfterClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public final class YamlOrchestrationShardingSphereDataSourceFactoryTest extends AbstractYamlDataSourceTest {
    
    private static final List<String> CONFIG_FILES = Arrays.asList("/yaml/integrate/sharding_ms/configWithDataSourceWithProps.yaml",
            "/yaml/integrate/sharding_ms/configWithoutDataSourceWithProps.yaml",
            "/yaml/integrate/sharding_ms/configWithDataSourceWithoutRules.yaml");
    
    private static DataSource dataSource;
    
    @Test
    public void assertCreateDataSource() {
        CONFIG_FILES.forEach(each -> {
            try {
                File yamlFile = new File(YamlOrchestrationShardingSphereDataSourceFactoryTest.class.getResource(each).toURI());
                executeSQL(yamlFile);
            } catch (final URISyntaxException | SQLException | IOException ex) {
                throw new ShardingSphereException(ex);
            }
        });
    }
    
    @Test
    public void assertCreateDataSourceByYamlBytes() {
        CONFIG_FILES.forEach(each -> {
            try {
                File yamlFile = new File(YamlOrchestrationShardingSphereDataSourceFactoryTest.class.getResource(each).toURI());
                executeSQL(getYamlBytes(yamlFile));
            } catch (final URISyntaxException | SQLException | IOException ex) {
                throw new ShardingSphereException(ex);
            }
        });
    }
    
    private void executeSQL(final File yamlFile) throws SQLException, IOException {
        dataSource = YamlOrchestrationShardingSphereDataSourceFactory.createDataSource(yamlFile);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(String.format("INSERT INTO t_order(user_id,status) values(%d, %s)", 10, "'insert'"));
            statement.executeQuery("SELECT * FROM t_order");
            statement.executeQuery("SELECT * FROM t_order_item");
            statement.executeQuery("SELECT * FROM config");
        }
    }
    
    private void executeSQL(final byte[] yamlBytes) throws SQLException, IOException {
        dataSource = YamlOrchestrationShardingSphereDataSourceFactory.createDataSource(yamlBytes);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(String.format("INSERT INTO t_order(user_id,status) values(%d, %s)", 10, "'insert'"));
            statement.executeQuery("SELECT * FROM t_order");
            statement.executeQuery("SELECT * FROM t_order_item");
            statement.executeQuery("SELECT * FROM config");
        }
    }
    
    @AfterClass
    public static void close() throws Exception {
        ((OrchestrationShardingSphereDataSource) dataSource).close();
    }
}
