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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.proxy;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.util.DatabaseTypeUtil;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public final class ShardingSphereProxyLocalContainer {
    
    private final DatabaseType databaseType;
    
    private final String defaultDatabaseName;
    
    private volatile boolean started;
    
    @SneakyThrows
    public ShardingSphereProxyLocalContainer(final DatabaseType databaseType) {
        this.databaseType = databaseType;
        if (DatabaseTypeUtil.isPostgreSQL(databaseType)) {
            defaultDatabaseName = "postgres";
        } else {
            defaultDatabaseName = "";
        }
    }
    
    /**
     * Wait proxy started.
     *
     * @param databaseType database type
     */
    @SneakyThrows
    public void waitProxyStarted(final DatabaseType databaseType) {
        for (int retry = 0; retry < 60; retry++) {
            try (Connection connection = DriverManager.getConnection(DataSourceEnvironment.getURL(databaseType, "localhost", 3307, defaultDatabaseName), "root", "root")) {
                log.info("Container ready");
                started = true;
                return;
                // CHECKSTYLE:OFF
            } catch (final Exception ignored) {
                // CHECKSTYLE:ON
            }
            TimeUnit.SECONDS.sleep(1);
        }
        throw new RuntimeException("Proxy not started");
    }
    
    /**
     * Start proxy.
     */
    public void start() {
        if (started) {
            return;
        }
        synchronized (this) {
            if (started) {
                return;
            }
            new Thread(() -> {
                try {
                    YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(String.format("/env/%s", databaseType.getName().toLowerCase()));
                    yamlConfig.getServerConfiguration().getMode().getRepository().getProps().setProperty("server-lists", "localhost:2181");
                    new BootstrapInitializer().init(yamlConfig, 3307);
                    new ShardingSphereProxy().start(3307);
                } catch (final IOException | SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
            waitProxyStarted(databaseType);
        }
    }
}
