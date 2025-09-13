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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.docker.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.docker.DockerStorageContainer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Hive container.
 */
@Slf4j
public final class HiveContainer extends DockerStorageContainer {
    
    public static final int EXPOSED_PORT = 10000;
    
    public HiveContainer(final String containerImage, final StorageContainerConfiguration storageContainerConfig) {
        super(TypedSPILoader.getService(DatabaseType.class, "Hive"), Strings.isNullOrEmpty(containerImage) ? "apache/hive:4.0.1" : containerImage, storageContainerConfig);
    }
    
    @Override
    protected Collection<String> getActualDatabaseNames() {
        return getStorageContainerConfig().getActualDatabaseTypes().entrySet().stream().filter(entry -> entry.getValue() == getDatabaseType()).map(Entry::getKey).collect(Collectors.toList());
    }
    
    @Override
    protected Collection<String> getExpectedDatabaseNames() {
        return getStorageContainerConfig().getExpectedDatabaseTypes().entrySet().stream().filter(entry -> entry.getValue() == getDatabaseType()).map(Entry::getKey).collect(Collectors.toList());
    }
    
    @Override
    public int getExposedPort() {
        return EXPOSED_PORT;
    }
    
    @Override
    public int getMappedPort() {
        return getMappedPort(EXPOSED_PORT);
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.empty();
    }
    
    @Override
    protected void postStart() {
        try {
            createDatabasesFromConfiguration();
            log.info("Databases created successfully in postStart()");
            executeMountedSQLScripts();
            log.info("Mounted SQL scripts executed successfully");
        } catch (final InterruptedException | IOException ex) {
            log.error("Failed to create databases in postStart()", ex);
        }
        super.postStart();
        log.info("Hive container postStart completed successfully");
    }
    
    private void createDatabasesFromConfiguration() throws InterruptedException, IOException {
        Collection<String> actualDatabaseNames = getDatabaseNames();
        Collection<String> expectedDatabaseNames = getExpectedDatabaseNames();
        Collection<String> allDatabaseNames = new HashSet<>();
        allDatabaseNames.addAll(actualDatabaseNames);
        allDatabaseNames.addAll(expectedDatabaseNames);
        if (allDatabaseNames.isEmpty()) {
            log.warn("No databases configured for Hive container");
            return;
        }
        StringBuilder createDatabaseSQL = new StringBuilder();
        for (String databaseName : allDatabaseNames) {
            createDatabaseSQL.append("CREATE DATABASE IF NOT EXISTS ").append(databaseName).append("; ");
        }
        String command = String.format("beeline -u \"jdbc:hive2://localhost:10000/default\" -e \"%s\"", createDatabaseSQL.toString());
        execInContainer("bash", "-c", command);
        log.info("Created databases: {}", allDatabaseNames);
    }
    
    private void executeMountedSQLScripts() throws InterruptedException, IOException {
        execInContainer("bash", "-c",
                "if [ -f /docker-entrypoint-initdb.d/50-scenario-actual-init.sql ]; then beeline -u \"jdbc:hive2://localhost:10000/default\" -f "
                        + "/docker-entrypoint-initdb.d/50-scenario-actual-init.sql; fi");
        execInContainer("bash", "-c",
                "if [ -f /docker-entrypoint-initdb.d/60-scenario-expected-init.sql ]; then beeline -u \"jdbc:hive2://localhost:10000/default\" -f "
                        + "/docker-entrypoint-initdb.d/60-scenario-expected-init.sql; fi");
    }
}
