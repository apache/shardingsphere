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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Storage container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerConfigurationFactory {
    
    private static final Collection<String> TO_BE_MOUNTED_COMMON_SQL_FILES = Arrays.asList("00-common-init-authority.sql", "99-common-check-ready.sql");
    
    private static final String TO_BE_MOUNTED_STANDARD_ENV_SQL_FILE = "20-env-initdb.sql";
    
    private static final String TO_BE_MOUNTED_ACTUAL_SCENARIO_SQL_FILE = "50-scenario-actual-init.sql";
    
    private static final String TO_BE_MOUNTED_EXPECTED_SCENARIO_SQL_FILE = "60-scenario-expected-init.sql";
    
    /**
     * Create new instance of storage container configuration.
     *
     * @param option storage container configuration option
     * @param databaseType database type
     * @param scenario scenario
     * @return created storage container configuration
     */
    public static StorageContainerConfiguration newInstance(final StorageContainerConfigurationOption option, final DatabaseType databaseType, final String scenario) {
        return newInstance(option, databaseType, findMajorVersion(option, 0).orElse(0), scenario);
    }
    
    /**
     * Create new instance of storage container configuration.
     *
     * @param option storage container configuration option
     * @param databaseType database type
     * @param majorVersion majorVersion
     * @param scenario scenario
     * @return created storage container configuration
     */
    public static StorageContainerConfiguration newInstance(final StorageContainerConfigurationOption option, final DatabaseType databaseType, final int majorVersion, final String scenario) {
        Collection<String> toBeMountedSQLFiles = getToBeMountedSQLFiles(databaseType, option, majorVersion, scenario);
        Map<String, DatabaseType> actualDatabaseTypes = null == scenario ? Collections.emptyMap() : DatabaseEnvironmentManager.getDatabaseTypes(scenario, databaseType, Type.ACTUAL);
        Map<String, DatabaseType> expectedDatabaseTypes = null == scenario ? Collections.emptyMap() : DatabaseEnvironmentManager.getDatabaseTypes(scenario, databaseType, Type.EXPECTED);
        return new StorageContainerConfiguration(scenario, option, toBeMountedSQLFiles, actualDatabaseTypes, expectedDatabaseTypes);
    }
    
    private static Collection<String> getToBeMountedSQLFiles(final DatabaseType databaseType, final StorageContainerConfigurationOption option, final int majorVersion, final String scenario) {
        Collection<String> result = new LinkedList<>();
        for (String each : TO_BE_MOUNTED_COMMON_SQL_FILES) {
            findToBeMountedCommonSQLFile(databaseType, each).ifPresent(optional -> result.add("/" + optional));
        }
        String toBeMountedStandardEnvSQLFilePath = String.format("env/container/%s/init-sql/%s", databaseType.getType().toLowerCase(), TO_BE_MOUNTED_STANDARD_ENV_SQL_FILE);
        if (null != Thread.currentThread().getContextClassLoader().getResource(toBeMountedStandardEnvSQLFilePath)) {
            result.add("/" + toBeMountedStandardEnvSQLFilePath);
        }
        for (String each : option.getAdditionalEnvMountedSQLResources(findMajorVersion(option, majorVersion).orElse(0))) {
            getToBeMountedAdditionalEnvSQLFile(databaseType, each).ifPresent(optional -> result.add("/" + optional));
        }
        for (String each : getToBeMountedScenarioSQLFiles(databaseType, scenario)) {
            result.add("/" + each);
        }
        return result;
    }
    
    private static Optional<String> findToBeMountedCommonSQLFile(final DatabaseType databaseType, final String toBeMountedSQLFile) {
        String toBeMountedFilePath = String.format("container/%s/init-sql/%s", databaseType.getType().toLowerCase(), toBeMountedSQLFile);
        return null == Thread.currentThread().getContextClassLoader().getResource(toBeMountedFilePath) ? Optional.empty() : Optional.of(toBeMountedFilePath);
    }
    
    private static Optional<String> getToBeMountedAdditionalEnvSQLFile(final DatabaseType databaseType, final String sqlFile) {
        String toBeMountedFilePath = String.format("env/container/%s/init-sql/%s", databaseType.getType().toLowerCase(), sqlFile);
        return null == Thread.currentThread().getContextClassLoader().getResource(toBeMountedFilePath) ? Optional.empty() : Optional.of(toBeMountedFilePath);
    }
    
    private static Collection<String> getToBeMountedScenarioSQLFiles(final DatabaseType databaseType, final String scenario) {
        Collection<String> result = new LinkedList<>();
        String actualScenarioFile = new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, databaseType) + "/" + TO_BE_MOUNTED_ACTUAL_SCENARIO_SQL_FILE;
        if (null != Thread.currentThread().getContextClassLoader().getResource(actualScenarioFile)) {
            result.add(actualScenarioFile);
        }
        String expectedScenarioFile = new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, databaseType) + "/" + TO_BE_MOUNTED_EXPECTED_SCENARIO_SQL_FILE;
        if (null != Thread.currentThread().getContextClassLoader().getResource(expectedScenarioFile)) {
            result.add(expectedScenarioFile);
        }
        return result;
    }
    
    private static Optional<Integer> findMajorVersion(final StorageContainerConfigurationOption option, final int majorVersion) {
        if (option.getSupportedMajorVersions().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(option.getSupportedMajorVersions().stream().filter(optional -> optional == majorVersion).findAny().orElse(option.getSupportedMajorVersions().get(0)));
    }
}
