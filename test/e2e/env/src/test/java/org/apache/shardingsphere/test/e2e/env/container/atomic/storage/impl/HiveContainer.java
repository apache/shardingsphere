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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;

import java.io.IOException;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Hive container.
 */
public final class HiveContainer extends DockerStorageContainer {
    
    public static final int HIVE_EXPOSED_PORT = 10000;
    
    private final StorageContainerConfiguration storageContainerConfig;
    
    public HiveContainer(final String containerImage, final StorageContainerConfiguration storageContainerConfig) {
        super(TypedSPILoader.getService(DatabaseType.class, "Hive"), Strings.isNullOrEmpty(containerImage) ? "apache/hive:4.0.1" : containerImage);
        this.storageContainerConfig = storageContainerConfig;
    }
    
    @Override
    protected void configure() {
        setCommands(storageContainerConfig.getContainerCommand());
        addEnvs(storageContainerConfig.getContainerEnvironments());
        mapResources(storageContainerConfig.getMountedResources());
        withExposedPorts(getExposedPort());
        withStartupTimeout(Duration.of(180L, ChronoUnit.SECONDS));
        setWaitStrategy(new JdbcConnectionWaitStrategy(
                () -> DriverManager.getConnection(DataSourceEnvironment.getURL(getDatabaseType(), "localhost", getFirstMappedPort()), getUsername(), getPassword())));
    }
    
    @Override
    protected Collection<String> getDatabaseNames() {
        return storageContainerConfig.getDatabaseTypes().entrySet().stream()
                .filter(entry -> entry.getValue() == getDatabaseType())
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }
    
    @Override
    protected Collection<String> getExpectedDatabaseNames() {
        return storageContainerConfig.getExpectedDatabaseTypes().entrySet().stream()
                .filter(entry -> entry.getValue() == getDatabaseType())
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }
    
    @Override
    public int getExposedPort() {
        return HIVE_EXPOSED_PORT;
    }
    
    @Override
    public int getMappedPort() {
        return getMappedPort(HIVE_EXPOSED_PORT);
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.empty();
    }
    
    @Override
    protected void postStart() {
        try {
            execInContainer("bash", "-c",
                    "beeline -u \"jdbc:hive2://localhost:10000/default\" -e \"CREATE DATABASE IF NOT EXISTS encrypt; CREATE DATABASE IF NOT EXISTS expected_dataset;\"");
            System.out.println("Databases created successfully in postStart()");
        } catch (final InterruptedException | IOException ex) {
            System.err.println("Failed to create databases in postStart(): " + ex.getMessage());
        }
        super.postStart();
        System.out.println("Hive container postStart completed successfully");
    }
}
