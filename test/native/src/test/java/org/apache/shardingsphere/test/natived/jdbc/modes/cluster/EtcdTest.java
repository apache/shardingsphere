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
import io.etcd.jetcd.launcher.Etcd;
import io.etcd.jetcd.launcher.EtcdCluster;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.shardingsphere.test.natived.jdbc.commons.TestShardingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class EtcdTest {
    
    private static final String SYSTEM_PROP_KEY_PREFIX = "fixture.test-native.yaml.mode.cluster.etcd.";
    
    private TestShardingService testShardingService;
    
    /**
     * TODO On low-performance devices in Github Actions, `INSERT` related SQLs may throw a table not found error under nativeTest.
     *  So that we need to wait for a period of time after executing `CREATE TABLE` related SQLs before executing `INSERT` related SQLs.
     *  This may mean that the implementation of {@link org.apache.shardingsphere.mode.repository.cluster.etcd.EtcdRepository} needs optimization.
     *
     * @see org.apache.shardingsphere.mode.repository.cluster.etcd.EtcdRepository
     */
    @Test
    @EnabledInNativeImage
    void assertShardingInLocalTransactions() throws SQLException {
        try (
                EtcdCluster etcd = Etcd.builder()
                        .withNodes(1)
                        .withMountedDataDirectory(false)
                        .build()) {
            etcd.start();
            DataSource dataSource = createDataSource(etcd.clientEndpoints());
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
    
    private DataSource createDataSource(final List<URI> clientEndpoints) {
        URI clientEndpoint = clientEndpoints.get(0);
        Awaitility.await().atMost(Duration.ofSeconds(30L)).ignoreExceptions().until(() -> verifyEtcdClusterRunning(clientEndpoint));
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:test-native/yaml/modes/cluster/etcd.yaml?placeholder-type=system_props");
        try {
            assertThat(System.getProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists"), is(nullValue()));
            System.setProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists", clientEndpoint.toString());
            return new HikariDataSource(config);
        } finally {
            System.clearProperty(SYSTEM_PROP_KEY_PREFIX + "server-lists");
        }
    }
    
    private Boolean verifyEtcdClusterRunning(final URI clientEndpoint) throws IOException {
        boolean flag = false;
        HttpGet httpGet = new HttpGet(clientEndpoint.toString() + "/health");
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
