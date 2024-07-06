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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@EnabledInNativeImage
@Testcontainers
class SeataTest {
    
    /**
     * TODO Further processing of `/health` awaits <a href="https://github.com/apache/incubator-seata/pull/6356">apache/incubator-seata#6356</a>.
     */
    @SuppressWarnings("resource")
    @Container
    public static final GenericContainer<?> CONTAINER = new GenericContainer<>("seataio/seata-server:1.8.0")
            .withExposedPorts(7091, 8091)
            .waitingFor(Wait.forHttp("/health").forPort(7091).forStatusCode(HttpStatus.SC_UNAUTHORIZED));
    
    private static final String SERVICE_DEFAULT_GROUP_LIST_KEY = "service.default.grouplist";
    
    private TestShardingService testShardingService;
    
    @BeforeAll
    static void beforeAll() {
        assertThat(System.getProperty(SERVICE_DEFAULT_GROUP_LIST_KEY), is(nullValue()));
    }
    
    @AfterAll
    static void afterAll() {
        System.clearProperty(SERVICE_DEFAULT_GROUP_LIST_KEY);
    }
    
    @Test
    void assertShardingInSeataTransactions() throws SQLException {
        DataSource dataSource = createDataSource(CONTAINER.getMappedPort(8091));
        testShardingService = new TestShardingService(dataSource);
        initEnvironment();
        testShardingService.processSuccess();
        testShardingService.cleanEnvironment();
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInPostgres();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInPostgres();
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private DataSource createDataSource(final int hostPort) {
        System.setProperty(SERVICE_DEFAULT_GROUP_LIST_KEY, "127.0.0.1:" + hostPort);
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/transactions/base/seata.yaml");
        return new HikariDataSource(config);
    }
}
