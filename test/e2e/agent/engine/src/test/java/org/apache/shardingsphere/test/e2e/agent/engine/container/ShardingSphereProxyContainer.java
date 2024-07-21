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

package org.apache.shardingsphere.test.e2e.agent.engine.container;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.output.OutputFrame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * ShardingSphere proxy container.
 */
// TODO Merge test container: merge with ShardingSphereProxyClusterContainer
public final class ShardingSphereProxyContainer extends DockerITContainer {
    
    private static final int EXPOSED_PORT = 3307;
    
    private static final String DATABASE_NAME = "sharding_db";
    
    private static final String READY_USER = "root";
    
    private static final String READY_USER_PASSWORD = "root";
    
    private final String plugin;
    
    private final Consumer<OutputFrame> consumer;
    
    public ShardingSphereProxyContainer(final String image, final String plugin, final Consumer<OutputFrame> consumer) {
        super("proxy", image);
        this.consumer = consumer;
        this.plugin = plugin;
    }
    
    @Override
    protected void configure() {
        createResourceMappingForProxy().forEach((key, value) -> withClasspathResourceMapping(key, value, BindMode.READ_ONLY));
        Optional.ofNullable(consumer).ifPresent(optional -> withLogConsumer(consumer));
        withExposedPorts(EXPOSED_PORT, 19090);
        setWaitStrategy(new JdbcConnectionWaitStrategy(
                () -> DriverManager.getConnection(DataSourceEnvironment.getURL(new MySQLDatabaseType(), getHost(), getMappedPort(EXPOSED_PORT)), READY_USER, READY_USER_PASSWORD)));
    }
    
    private Map<String, String> createResourceMappingForProxy() {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put("/env/proxy/conf/global.yaml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "global.yaml");
        result.put("/env/proxy/conf/database-db.yaml", ProxyContainerConstants.CONFIG_PATH_IN_CONTAINER + "database-db.yaml");
        if (!Strings.isNullOrEmpty(plugin)) {
            result.put(String.format("/env/agent/conf/%s/agent.yaml", plugin), ProxyContainerConstants.AGENT_CONFIG_PATH_IN_CONTAINER + "agent.yaml");
        }
        return result;
    }
    
    /**
     * Get connection.
     *
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DataSourceEnvironment.getURL(new MySQLDatabaseType(), getHost(), getMappedPort(EXPOSED_PORT), DATABASE_NAME), READY_USER, READY_USER_PASSWORD);
    }
    
    @Override
    public String getAbbreviation() {
        return "proxy";
    }
}
