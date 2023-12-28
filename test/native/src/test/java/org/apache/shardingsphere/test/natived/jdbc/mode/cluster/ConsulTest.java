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

import com.ecwid.consul.transport.HttpResponse;
import com.ecwid.consul.v1.ConsulRawClient;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.test.natived.jdbc.commons.FileTestUtils;
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.apache.shardingsphere.test.natived.jdbc.commons.testcontainers.ShardingSphereConsulContainer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

public class ConsulTest {
    
    private static final int CONSUL_HOST_HTTP_PORT = 62391;
    
    private TestShardingService testShardingService;
    
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException, IOException {
        try (
                GenericContainer<?> consulContainer = new ShardingSphereConsulContainer(DockerImageName.parse("hashicorp/consul:1.10.12"))) {
            consulContainer.start();
            beforeAll();
            DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("test-native/yaml/mode/cluster/consul.yaml"));
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
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(this::verifyConsulAgentRunning);
    }
    
    private boolean verifyConsulAgentRunning() {
        boolean flag = false;
        HttpResponse httpResponse = new ConsulRawClient("http://localhost", CONSUL_HOST_HTTP_PORT).makeGetRequest("/v1/status/leader");
        if (HttpStatus.SC_OK == httpResponse.getStatusCode()) {
            flag = true;
        }
        return flag;
    }
}
