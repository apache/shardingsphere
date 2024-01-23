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

package org.apache.shardingsphere.test.natived.jdbc.mode.cluster;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.test.natived.jdbc.commons.FileTestUtils;
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

class ZookeeperTest {
    
    private TestShardingService testShardingService;
    
    /**
     * TODO On low-performance devices in Github Actions, `INSERT` related SQLs may throw a table not found error under nativeTest.
     *  So that we need to wait for a period of time after executing `CREATE TABLE` related SQLs before executing `INSERT` related SQLs.
     *  This may mean that the implementation of {@link org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository} needs optimization.
     *
     * @see org.apache.shardingsphere.mode.repository.cluster.zookeeper.ZookeeperRepository
     */
    @SuppressWarnings("resource")
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException, IOException {
        try (
                GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("zookeeper:3.9.1-jre-17"))
                        .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(62372), new ExposedPort(2181)))))) {
            container.start();
            beforeAll();
            DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("test-native/yaml/mode/cluster/zookeeper.yaml"));
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
    
    private void beforeAll() {
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(() -> {
            try (CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:" + 62372, new ExponentialBackoffRetry(1000, 3))) {
                client.start();
                return client.blockUntilConnected(5, TimeUnit.SECONDS);
            }
        });
    }
}
