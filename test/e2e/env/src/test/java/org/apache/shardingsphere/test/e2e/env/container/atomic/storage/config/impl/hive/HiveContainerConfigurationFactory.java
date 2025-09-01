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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.hive;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.HiveContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Hive container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HiveContainerConfigurationFactory {
    
    /**
     * Create new instance of Hive container configuration.
     *
     * @param scenario scenario
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance(final String scenario) {
        return new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(scenario),
            DatabaseEnvironmentManager.getDatabaseTypes(scenario, TypedSPILoader.getService(DatabaseType.class, "Hive")),
            DatabaseEnvironmentManager.getExpectedDatabaseTypes(scenario, TypedSPILoader.getService(DatabaseType.class, "Hive"))
        );
    }
    
    /**
     * Create new instance of Hive container configuration.
     *
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance() {
        return new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(), Collections.emptyMap(), Collections.emptyMap()
        );
    }
    
    private static String getCommand() {
        return "--service hiveserver2";
    }
    
    private static Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(4, 1F);
        result.put("SERVICE_NAME", "hiveserver2");
        result.put("SERVICE_OPTS", "-Dhive.support.concurrency=true " +
                                   "-Dhive.exec.dynamic.partition.mode=nonstrict " +
                                   "-Dhive.txn.manager=org.apache.hadoop.hive.ql.lockmgr.DbTxnManager");
        result.put("LANG", "C.UTF-8");
        return result;
    }
    
    private static Map<String, String> getMountedResources() {
        Map<String, String> result = new HashMap<>(1, 1F);
        String path = "env/hive/01-initdb.sql";
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (null != url) {
            result.put(path, "/docker-entrypoint-initdb.d/01-initdb.sql");
        }
        return result;
    }
    
    private static Map<String, String> getMountedResources(final String scenario) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, TypedSPILoader.getService(DatabaseType.class, "Hive")) + "/01-actual-init.sql",
                "/docker-entrypoint-initdb.d/01-actual-init.sql");
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, TypedSPILoader.getService(DatabaseType.class, "Hive")) + "/01-expected-init.sql",
                "/docker-entrypoint-initdb.d/01-expected-init.sql");
        result.put("/env/hive/hive-site.xml", HiveContainer.HIVE_CONF_IN_CONTAINER);
        return result;
    }
}
