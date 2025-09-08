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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Storage container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerConfigurationFactory {
    
    /**
     * Create new instance of storage container configuration.
     *
     * @param option storage container configuration option
     * @param databaseType database type
     * @param majorVersion majorVersion
     * @return created storage container configuration
     */
    public static StorageContainerConfiguration newInstance(final StorageContainerConfigurationOption option, final DatabaseType databaseType, final int majorVersion) {
        Map<String, String> mountedResources = getMountedResources(databaseType, option, majorVersion, "");
        return new StorageContainerConfiguration(option.getCommand(), option.getContainerEnvironments(), mountedResources, Collections.emptyMap(), Collections.emptyMap());
    }
    
    /**
     * Create new instance of storage container configuration.
     *
     * @param option storage container configuration option
     * @param databaseType database type
     * @param scenario  scenario
     * @return created storage container configuration
     */
    public static StorageContainerConfiguration newInstance(final StorageContainerConfigurationOption option, final DatabaseType databaseType, final String scenario) {
        Map<String, DatabaseType> databaseTypes = DatabaseEnvironmentManager.getDatabaseTypes(scenario, databaseType);
        Map<String, DatabaseType> expectedDatabaseTypes = DatabaseEnvironmentManager.getExpectedDatabaseTypes(scenario, databaseType);
        Map<String, String> mountedResources = getMountedResources(databaseType, option, 0, scenario);
        return option.isEmbeddedStorageContainer()
                ? new StorageContainerConfiguration(scenario, option.getCommand(), option.getContainerEnvironments(), mountedResources, databaseTypes, expectedDatabaseTypes)
                : new StorageContainerConfiguration(option.getCommand(), option.getContainerEnvironments(), mountedResources, databaseTypes, expectedDatabaseTypes);
    }
    
    private static Map<String, String> getMountedResources(final DatabaseType databaseType, final StorageContainerConfigurationOption option, final int majorVersion, final String scenario) {
        Map<String, String> mountConfigResources = getToBeMountedConfigurationFiles(databaseType, option, majorVersion, scenario);
        Map<String, String> mountSQLResources = getToBeMountedSQLFiles(databaseType, option, majorVersion, scenario);
        Map<String, String> result = new HashMap<>(mountConfigResources.size() + mountSQLResources.size(), 1F);
        result.putAll(mountConfigResources);
        result.putAll(mountSQLResources);
        return result;
    }
    
    private static Map<String, String> getToBeMountedConfigurationFiles(final DatabaseType databaseType,
                                                                        final StorageContainerConfigurationOption option, final int majorVersion, final String scenario) {
        Map<String, String> mountedConfigurationResources = option.getMountedConfigurationResources();
        Map<String, String> result = new HashMap<>(mountedConfigurationResources.size(), 1F);
        for (Entry<String, String> entry : mountedConfigurationResources.entrySet()) {
            Optional<Integer> foundMajorVersion = findMajorVersion(option, majorVersion);
            String configFilePath = foundMajorVersion.map(optional -> String.format("container/%s/cnf/%d/%s", databaseType.getType().toLowerCase(), optional, entry.getKey()))
                    .orElseGet(() -> String.format("container/%s/cnf/%s", databaseType.getType().toLowerCase(), entry.getKey()));
            result.put(getToBeMountedConfigurationFile(configFilePath, scenario), entry.getValue());
        }
        return result;
    }
    
    private static String getToBeMountedConfigurationFile(final String toBeMountedConfigFile, final String scenario) {
        String scenarioConfigFilePath = String.format("scenario/%s/%s", scenario, toBeMountedConfigFile);
        if (null != Thread.currentThread().getContextClassLoader().getResource(scenarioConfigFilePath)) {
            return "/" + scenarioConfigFilePath;
        }
        String envConfigFilePath = String.format("env/%s", toBeMountedConfigFile);
        if (null != Thread.currentThread().getContextClassLoader().getResource(envConfigFilePath)) {
            return "/" + envConfigFilePath;
        }
        return "/" + toBeMountedConfigFile;
    }
    
    private static Map<String, String> getToBeMountedSQLFiles(final DatabaseType databaseType, final StorageContainerConfigurationOption option, final int majorVersion, final String scenario) {
        int foundMajorVersion = findMajorVersion(option, majorVersion).orElse(0);
        return getToBeMountedSQLFiles(databaseType, foundMajorVersion, option, scenario);
    }
    
    private static Map<String, String> getToBeMountedSQLFiles(final DatabaseType databaseType, final int majorVersion, final StorageContainerConfigurationOption option, final String scenario) {
        Collection<String> mountedSQLResources = option.getMountedSQLResources(majorVersion);
        Map<String, String> result = new HashMap<>(mountedSQLResources.size(), 1F);
        for (String each : mountedSQLResources) {
            getToBeMountedSQLFile(databaseType, each, scenario).ifPresent(optional -> result.put("/" + optional, "/docker-entrypoint-initdb.d/" + each));
        }
        return result;
    }
    
    private static Optional<String> getToBeMountedSQLFile(final DatabaseType databaseType, final String sqlFile, final String scenario) {
        String actualScenarioFile = new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, databaseType) + "/" + sqlFile;
        if (null != Thread.currentThread().getContextClassLoader().getResource(actualScenarioFile)) {
            return Optional.of(actualScenarioFile);
        }
        String expectedScenarioFile = new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, databaseType) + "/" + sqlFile;
        if (null != Thread.currentThread().getContextClassLoader().getResource(expectedScenarioFile)) {
            return Optional.of(expectedScenarioFile);
        }
        String envFile = String.format("env/%s/%s", databaseType.getType().toLowerCase(), sqlFile);
        if (null != Thread.currentThread().getContextClassLoader().getResource(envFile)) {
            return Optional.of(envFile);
        }
        return Optional.empty();
    }
    
    private static Optional<Integer> findMajorVersion(final StorageContainerConfigurationOption option, final int majorVersion) {
        if (option.getSupportedMajorVersions().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(option.getSupportedMajorVersions().stream().filter(optional -> optional == majorVersion).findAny().orElse(option.getSupportedMajorVersions().get(0)));
    }
}
