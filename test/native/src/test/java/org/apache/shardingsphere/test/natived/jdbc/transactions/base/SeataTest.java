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

package org.apache.shardingsphere.test.natived.jdbc.transactions.base;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import javax.sql.DataSource;
import java.sql.SQLException;

class SeataTest {
    
    private TestShardingService testShardingService;
    
    /**
     * Further processing of `/health` awaits <a href="https://github.com/apache/incubator-seata/pull/6356">apache/incubator-seata#6356</a>.
     * @throws SQLException An exception that provides information on a database access error or other errors.
     */
    @SuppressWarnings({"resource", "deprecation"})
    @Test
    @EnabledInNativeImage
    void assertShardingInSeataTransactions() throws SQLException {
        try (
                GenericContainer<?> container = new FixedHostPortGenericContainer<>("seataio/seata-server:1.8.0")
                        .withFixedExposedPort(39567, 8091)
                        .withExposedPorts(7091)
                        .waitingFor(Wait.forHttp("/health").forPort(7091).forStatusCode(HttpStatus.SC_UNAUTHORIZED))) {
            container.start();
            DataSource dataSource = createDataSource();
            testShardingService = new TestShardingService(dataSource);
            this.initEnvironment();
            testShardingService.processSuccess();
            testShardingService.cleanEnvironment();
        }
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInPostgres();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInPostgres();
        testShardingService.getAddressRepository().createTableIfNotExists();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/transactions/base/seata.yaml");
        return new HikariDataSource(config);
    }
}
