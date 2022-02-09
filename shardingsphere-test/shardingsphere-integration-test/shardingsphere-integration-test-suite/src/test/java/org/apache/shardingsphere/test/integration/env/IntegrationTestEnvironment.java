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

package org.apache.shardingsphere.test.integration.env;

import com.google.common.base.Splitter;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.props.EnvironmentProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Integration test running environment.
 */
@Getter
public final class IntegrationTestEnvironment {
    
    private static final IntegrationTestEnvironment INSTANCE = new IntegrationTestEnvironment();
    
    private final EnvironmentType envType;
    
    private final Collection<String> adapters;
    
    private final Collection<String> scenarios;
    
    private final boolean runAdditionalTestCases;
    
    private final Set<DatabaseType> databaseTypes;
    
    private IntegrationTestEnvironment() {
        Properties engineEnvProps = EnvironmentProperties.loadProperties("env/engine-env.properties");
        envType = getEnvironmentType(engineEnvProps);
        adapters = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.adapters"));
        scenarios = getScenarios(engineEnvProps);
        runAdditionalTestCases = Boolean.parseBoolean(engineEnvProps.getProperty("it.run.additional.cases"));
        databaseTypes = getDatabaseTypes(engineEnvProps);
    }
    
    private EnvironmentType getEnvironmentType(final Properties engineEnvProps) {
        try {
            return EnvironmentType.valueOf(engineEnvProps.getProperty("it.env.type"));
        } catch (final IllegalArgumentException ignored) {
            return EnvironmentType.NATIVE;
        }
    }
    
    private Collection<String> getScenarios(final Properties engineEnvProps) {
        Collection<String> result = Splitter.on(",").trimResults().splitToList(engineEnvProps.getProperty("it.scenarios"));
        for (String each : result) {
            EnvironmentPath.assertScenarioDirectoryExisted(each);
        }
        return result;
    }
    
    private Set<DatabaseType> getDatabaseTypes(final Properties engineEnvProps) {
        return Arrays.stream(engineEnvProps.getProperty("it.databases").split(",")).map(each -> DatabaseTypeRegistry.getActualDatabaseType(each.trim())).collect(Collectors.toSet());
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static IntegrationTestEnvironment getInstance() {
        return INSTANCE;
    }
    
}
