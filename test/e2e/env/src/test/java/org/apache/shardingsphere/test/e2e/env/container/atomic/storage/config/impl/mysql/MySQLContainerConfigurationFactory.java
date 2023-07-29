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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.ContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLContainerConfigurationFactory {
    
    /**
     * Create new instance of MySQL container configuration.
     *
     * @param scenario scenario
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance(final String scenario) {
        return new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(scenario), DatabaseEnvironmentManager.getDatabaseNames(scenario),
                DatabaseEnvironmentManager.getExpectedDatabaseNames(scenario));
    }
    
    /**
     * Create new instance of MySQL container configuration with major version.
     *
     * @param majorVersion major version
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance(final int majorVersion) {
        return new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(majorVersion), new ArrayList<>(), new ArrayList<>());
    }
    
    private static String getCommand() {
        return "--server-id=" + ContainerUtils.generateMySQLServerId();
    }
    
    private static Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("LANG", "C.UTF-8");
        result.put("MYSQL_RANDOM_ROOT_PASSWORD", "yes");
        return result;
    }
    
    private static Map<String, String> getMountedResources(final int majorVersion) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(String.format("/env/mysql/mysql%s/my.cnf", majorVersion), MySQLContainer.MYSQL_CONF_IN_CONTAINER);
        result.put("/env/mysql/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql");
        if (majorVersion > 5) {
            result.put("/env/mysql/mysql8/02-initdb.sql", "/docker-entrypoint-initdb.d/02-initdb.sql");
        }
        return result;
    }
    
    private static Map<String, String> getMountedResources(final String scenario) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, new MySQLDatabaseType()) + "/01-actual-init.sql", "/docker-entrypoint-initdb.d/01-actual-init.sql");
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, new MySQLDatabaseType()) + "/01-expected-init.sql",
                "/docker-entrypoint-initdb.d/01-expected-init.sql");
        result.put("/env/mysql/my.cnf", MySQLContainer.MYSQL_CONF_IN_CONTAINER);
        return result;
    }
}
