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

package org.apache.shardingsphere.test.e2e.transaction.framework.container.config.postgresql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.OpenGaussContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * PostgreSQL container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLContainerConfigurationFactory {
    
    /**
     * Create new instance of PostgreSQL container configuration.
     *
     * @param scenario scenario
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance(final String scenario) {
        return new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(scenario), DatabaseEnvironmentManager.getDatabaseNames(scenario),
                DatabaseEnvironmentManager.getExpectedDatabaseNames(scenario));
    }
    
    private static String getCommand() {
        return "--max_connections=600 --max_prepared_transactions=600 --wal_level=logical";
    }
    
    private static Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("POSTGRES_HOST", StorageContainerConstants.USERNAME);
        result.put("POSTGRES_PASSWORD", StorageContainerConstants.PASSWORD);
        return result;
    }
    
    private static Map<String, String> getMountedResources(final String scenario) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, new PostgreSQLDatabaseType()) + "/01-actual-init.sql",
                "/docker-entrypoint-initdb.d/01-actual-init.sql");
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, new PostgreSQLDatabaseType()) + "/01-expected-init.sql",
                "/docker-entrypoint-initdb.d/01-expected-init.sql");
        result.put("/env/postgresql/postgresql.conf", OpenGaussContainer.OPENGAUSS_CONF_IN_CONTAINER);
        return result;
    }
}
