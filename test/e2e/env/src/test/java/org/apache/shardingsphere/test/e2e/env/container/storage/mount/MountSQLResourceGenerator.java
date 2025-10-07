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

package org.apache.shardingsphere.test.e2e.env.container.storage.mount;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerCreateOption;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath.Type;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Mount SQL resource generator.
 */
@RequiredArgsConstructor
public final class MountSQLResourceGenerator {
    
    private static final Collection<String> TO_BE_MOUNTED_COMMON_SQL_FILES = Arrays.asList("00-common-init-authority.sql", "99-common-check-ready.sql");
    
    private static final String TO_BE_MOUNTED_STANDARD_ENV_SQL_FILE = "20-env-initdb.sql";
    
    private static final String TO_BE_MOUNTED_ACTUAL_SCENARIO_SQL_FILE = "50-scenario-actual-init.sql";
    
    private static final String TO_BE_MOUNTED_EXPECTED_SCENARIO_SQL_FILE = "60-scenario-expected-init.sql";
    
    private final DatabaseType databaseType;
    
    private final StorageContainerCreateOption option;
    
    /**
     * Generate mount SQL resource map.
     *
     * @param majorVersion major version
     * @param scenario scenario
     * @return generated resource map
     */
    public Map<String, String> generate(final int majorVersion, final String scenario) {
        Collection<String> toBeMountedSQLFiles = new LinkedList<>();
        for (String each : TO_BE_MOUNTED_COMMON_SQL_FILES) {
            findToBeMountedCommonSQLFile(each).ifPresent(optional -> toBeMountedSQLFiles.add("/" + optional));
        }
        String toBeMountedStandardEnvSQLFilePath = String.format("env/container/%s/init-sql/%s", databaseType.getType().toLowerCase(), TO_BE_MOUNTED_STANDARD_ENV_SQL_FILE);
        if (null != Thread.currentThread().getContextClassLoader().getResource(toBeMountedStandardEnvSQLFilePath)) {
            toBeMountedSQLFiles.add("/" + toBeMountedStandardEnvSQLFilePath);
        }
        for (String each : option.getAdditionalEnvMountedSQLResources(majorVersion)) {
            getToBeMountedAdditionalEnvSQLFile(each).ifPresent(optional -> toBeMountedSQLFiles.add("/" + optional));
        }
        for (String each : getToBeMountedScenarioSQLFiles(scenario)) {
            toBeMountedSQLFiles.add("/" + each);
        }
        return new TreeMap<>(toBeMountedSQLFiles.stream().collect(Collectors.toMap(each -> each, each -> "/docker-entrypoint-initdb.d/" + new File(each).getName())));
    }
    
    private Optional<String> findToBeMountedCommonSQLFile(final String toBeMountedSQLFile) {
        String toBeMountedFilePath = String.format("container/%s/init-sql/%s", databaseType.getType().toLowerCase(), toBeMountedSQLFile);
        return null == Thread.currentThread().getContextClassLoader().getResource(toBeMountedFilePath) ? Optional.empty() : Optional.of(toBeMountedFilePath);
    }
    
    private Optional<String> getToBeMountedAdditionalEnvSQLFile(final String sqlFile) {
        String toBeMountedFilePath = String.format("env/container/%s/init-sql/%s", databaseType.getType().toLowerCase(), sqlFile);
        return null == Thread.currentThread().getContextClassLoader().getResource(toBeMountedFilePath) ? Optional.empty() : Optional.of(toBeMountedFilePath);
    }
    
    private Collection<String> getToBeMountedScenarioSQLFiles(final String scenario) {
        Collection<String> result = new LinkedList<>();
        String actualScenarioFile = new ScenarioDataPath(scenario, Type.ACTUAL).getInitSQLResourcePath(databaseType) + "/" + TO_BE_MOUNTED_ACTUAL_SCENARIO_SQL_FILE;
        if (null != Thread.currentThread().getContextClassLoader().getResource(actualScenarioFile)) {
            result.add(actualScenarioFile);
        }
        String expectedScenarioFile = new ScenarioDataPath(scenario, Type.EXPECTED).getInitSQLResourcePath(databaseType) + "/" + TO_BE_MOUNTED_EXPECTED_SCENARIO_SQL_FILE;
        if (null != Thread.currentThread().getContextClassLoader().getResource(expectedScenarioFile)) {
            result.add(expectedScenarioFile);
        }
        return result;
    }
}
