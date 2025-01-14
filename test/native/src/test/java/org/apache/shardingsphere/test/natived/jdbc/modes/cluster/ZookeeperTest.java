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

package org.apache.shardingsphere.test.natived.jdbc.modes.cluster;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.test.natived.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@EnabledInNativeImage
class ZookeeperTest {
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.mode.cluster.zookeeper.";
    
    private static DataSource logicDataSource;
    
    private TestShardingService testShardingService;
    
    @BeforeAll
    static void beforeAll() {
        assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists"), is(nullValue()));
    }
    
    @AfterAll
    static void afterAll() throws SQLException {
        try (Connection connection = logicDataSource.getConnection()) {
            connection.unwrap(ShardingSphereConnection.class).getContextManager().close();
        }
        System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists");
    }
    
    @Test
    void assertShardingInLocalTransactions() throws Exception {
        try (TestingServer testingServer = new TestingServer()) {
            String connectString = testingServer.getConnectString();
            logicDataSource = createDataSource(connectString);
            testShardingService = new TestShardingService(logicDataSource);
            initEnvironment();
            testShardingService.processSuccess();
            testShardingService.cleanEnvironment();
        }
    }
    
    /**
     * TODO On low-performance devices in Github Actions, `TRUNCATE TABLE` related SQLs may throw a `java.sql.SQLException: Table or view 't_address' does not exist.` error under nativeTest.
     *  So that we need to wait for a period of time after executing `CREATE TABLE` related SQLs before executing `TRUNCATE TABLE` related SQLs.
     *  This may mean that the implementation of {@link org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository} needs optimization.
     *
     * @see org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository
     * @throws SQLException SQL exception
     */
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInMySQL();
        testShardingService.getAddressRepository().createTableIfNotExistsInMySQL();
        Awaitility.await().pollDelay(Duration.ofSeconds(5L)).until(() -> true);
        testShardingService.getOrderRepository().truncateTable();
        testShardingService.getOrderItemRepository().truncateTable();
        testShardingService.getAddressRepository().truncateTable();
    }
    
    private DataSource createDataSource(final String connectString) {
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(() -> {
            try (CuratorFramework client = CuratorFrameworkFactory.newClient(connectString, new ExponentialBackoffRetry(1000, 3))) {
                client.start();
                return client.blockUntilConnected(5, TimeUnit.SECONDS);
            }
        });
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/jdbc/modes/cluster/zookeeper.yaml?placeholder-type=system_props");
        System.setProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists", connectString);
        return new HikariDataSource(config);
    }
}
