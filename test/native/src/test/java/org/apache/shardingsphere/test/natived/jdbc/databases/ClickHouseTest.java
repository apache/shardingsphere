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

package org.apache.shardingsphere.test.natived.jdbc.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.commons.util.ResourceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.jdbc.ContainerDatabaseDriver;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
class ClickHouseTest {
    
    private DataSource logicDataSource;
    
    @AfterEach
    void afterEach() throws SQLException {
        ResourceUtils.closeJdbcDataSource(logicDataSource);
        ContainerDatabaseDriver.killContainers();
    }
    
    @Test
    void assertShardingInLocalTransactions() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/databases/clickhouse.yaml");
        logicDataSource = new HikariDataSource(config);
        TestShardingService testShardingService = new TestShardingService(logicDataSource);
        assertDoesNotThrow(testShardingService::processSuccessInClickHouse);
    }
}
