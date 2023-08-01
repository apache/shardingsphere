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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.h2;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.h2.type.H2DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * H2 container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class H2ContainerConfigurationFactory {
    
    /**
     * Create new instance of h2 container configuration.
     *
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance() {
        Map<String, String> mountedResources = new HashMap<>(1, 1F);
        mountedResources.put("/env/mysql/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql");
        return new StorageContainerConfiguration("", Collections.emptyMap(), mountedResources, new ArrayList<>(), new ArrayList<>());
    }
    
    /**
     * Create new instance of h2 container configuration.
     *
     * @param scenario scenario
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance(final String scenario) {
        Map<String, String> mountedResources = new HashMap<>(2, 1F);
        mountedResources.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, new H2DatabaseType()) + "/01-actual-init.sql", "/docker-entrypoint-initdb.d/01-actual-init.sql");
        mountedResources.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, new H2DatabaseType()) + "/01-expected-init.sql",
                "/docker-entrypoint-initdb.d/01-expected-init.sql");
        return new StorageContainerConfiguration(scenario, "", Collections.emptyMap(), mountedResources, DatabaseEnvironmentManager.getDatabaseNames(scenario),
                DatabaseEnvironmentManager.getExpectedDatabaseNames(scenario));
    }
}
