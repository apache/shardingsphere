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
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class ZookeeperTest {
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.mode.cluster.zookeeper.";
    
    private TestShardingService testShardingService;
    
    /**
     * TODO On low-performance devices in Github Actions, `INSERT` related SQLs may throw a table not found error under nativeTest.
     *  So that we need to wait for a period of time after executing `CREATE TABLE` related SQLs before executing `INSERT` related SQLs.
     *  This may mean that the implementation of {@link org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository} needs optimization.
     *
     * @see org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository
     */
    @Test
    void assertShardingInLocalTransactions() throws Exception {
        try (TestingServer testingServer = new TestingServer()) {
            String connectString = testingServer.getConnectString();
            DataSource dataSource = createDataSource(connectString);
            testShardingService = new TestShardingService(dataSource);
            initEnvironment();
            Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(() -> {
                dataSource.getConnection().close();
                return true;
            });
            testShardingService.processSuccess();
            testShardingService.cleanEnvironment();
        }
    }
    
    private void initEnvironment() throws SQLException {
        testShardingService.getOrderRepository().createTableIfNotExistsInMySQL();
        testShardingService.getOrderItemRepository().createTableIfNotExistsInMySQL();
        testShardingService.getAddressRepository().createTableIfNotExists();
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
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/modes/cluster/zookeeper.yaml?placeholder-type=system_props");
        try {
            assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists"), is(nullValue()));
            System.setProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists", connectString);
            return new HikariDataSource(config);
        } finally {
            System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists");
        }
    }
}
