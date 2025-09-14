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
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.util.Collections;
import java.util.Map;

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
     * @param scenario scenario
     * @return created storage container configuration
     */
    public static StorageContainerConfiguration newInstance(final StorageContainerConfigurationOption option, final DatabaseType databaseType, final String scenario) {
        Map<String, DatabaseType> actualDatabaseTypes = null == scenario ? Collections.emptyMap() : DatabaseEnvironmentManager.getDatabaseTypes(scenario, databaseType, Type.ACTUAL);
        Map<String, DatabaseType> expectedDatabaseTypes = null == scenario ? Collections.emptyMap() : DatabaseEnvironmentManager.getDatabaseTypes(scenario, databaseType, Type.EXPECTED);
        return new StorageContainerConfiguration(scenario, option, actualDatabaseTypes, expectedDatabaseTypes);
    }
}
