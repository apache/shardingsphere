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
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.test.natived.jdbc.commons.FileTestUtils;
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class EtcdTest {
    
    private static final Integer ETCD_CLIENT_PORT_ON_HOST = 62290;
    
    private static final Integer ETCD_CLIENT_PORT = 2379;
    
    private static final Integer ETCD_PEER_PORT = 2380;
    
    private TestShardingService testShardingService;
    
    @SuppressWarnings("resource")
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException, IOException {
        String node = "etcd0";
        Collection<String> nodes = Collections.singletonList(node);
        try (
                GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.11"))
                        .withCreateContainerCmdModifier(
                                cmd -> cmd.withHostConfig(new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(ETCD_CLIENT_PORT_ON_HOST), new ExposedPort(ETCD_CLIENT_PORT)))))
                        .withNetworkAliases(node)
                        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(getClass())).withPrefix(node))
                        .withCommand(createCommand(node, nodes))
                        .withEnv("ETCD_LOG_LEVEL", "info")
                        .withEnv("ETCD_LOGGER", "zap")
                        .waitingFor(Wait.forHttp("/health").forPort(ETCD_CLIENT_PORT))) {
            container.start();
            beforeAll();
            DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(FileTestUtils.readFromFileURLString("test-native/yaml/mode/cluster/etcd.yaml"));
            testShardingService = new TestShardingService(dataSource);
            initEnvironment();
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
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(this::verifyEtcdClusterRunning);
    }
    
    @SuppressWarnings("HttpUrlsUsage")
    private String[] createCommand(final String node, final Collection<String> nodes) {
        List<String> cmd = new ArrayList<>();
        cmd.add("etcd");
        cmd.add("--name");
        cmd.add(node);
        cmd.add("--advertise-client-urls");
        cmd.add("http" + "://0.0.0.0:" + ETCD_CLIENT_PORT);
        cmd.add("--listen-client-urls");
        cmd.add("http" + "://0.0.0.0:" + ETCD_CLIENT_PORT);
        Collection<String> shouldMountDataDirectory = Arrays.asList("--data-dir", "/tmp/etcd-data");
        cmd.addAll(shouldMountDataDirectory);
        if (nodes.size() > 1) {
            cmd.add("--initial-advertise-peer-urls");
            cmd.add("http://" + node + ":" + ETCD_PEER_PORT);
            cmd.add("--listen-peer-urls");
            cmd.add("http://0.0.0.0:" + ETCD_PEER_PORT);
            cmd.add("--initial-cluster");
            cmd.add(nodes.stream().map(e -> e + "=http://" + e + ":" + ETCD_PEER_PORT).collect(Collectors.joining(",")));
            cmd.add("--initial-cluster-state");
            cmd.add("new");
            Collection<String> clusterToken = Arrays.asList("--initial-cluster-token", UUID.randomUUID().toString());
            cmd.addAll(clusterToken);
        }
        return cmd.toArray(new String[0]);
    }
    
    private Boolean verifyEtcdClusterRunning() throws IOException {
        boolean flag = false;
        HttpGet httpGet = new HttpGet("http://localhost:" + ETCD_CLIENT_PORT_ON_HOST + "/health");
        try (
                CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response = httpclient.execute(httpGet)) {
            if (HttpStatus.SC_OK == response.getCode()) {
                flag = true;
            }
            EntityUtils.consume(response.getEntity());
        }
        return flag;
    }
}
