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
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledInNativeImage
class ClickHouseTest {
    
    private TestShardingService testShardingService;
    
    /**
     * TODO Need to fix `shardingsphere-parser-sql-clickhouse` module to use {@link TestShardingService#cleanEnvironment()}
     *      after {@link TestShardingService#processSuccessInClickHouse()}.
     *
     */
    @Test
    void assertShardingInLocalTransactions() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/databases/clickhouse.yaml");
        DataSource dataSource = new HikariDataSource(config);
        testShardingService = new TestShardingService(dataSource);
        assertDoesNotThrow(() -> testShardingService.processSuccessInClickHouse());
    }
    
    /**
     * TODO Need to fix `shardingsphere-parser-sql-clickhouse` module to use `initEnvironment()`
     * before {@link TestShardingService#processSuccessInClickHouse()}.
     *
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    @SuppressWarnings("unused")
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInClickHouse();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInClickHouse();
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
}
