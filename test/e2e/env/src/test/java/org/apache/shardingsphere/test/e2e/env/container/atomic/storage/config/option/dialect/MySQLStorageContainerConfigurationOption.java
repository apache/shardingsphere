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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.ContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage container configuration option for MySQL.
 */
public final class MySQLStorageContainerConfigurationOption implements StorageContainerConfigurationOption {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Override
    public String getCommand() {
        return "--server-id=" + ContainerUtils.generateMySQLServerId();
    }
    
    @Override
    public Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("LANG", "C.UTF-8");
        result.put("MYSQL_RANDOM_ROOT_PASSWORD", "yes");
        return result;
    }
    
    @Override
    public Map<String, String> getMountedResources(final String scenario) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, databaseType) + "/01-actual-init.sql", "/docker-entrypoint-initdb.d/01-actual-init.sql");
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, databaseType) + "/01-expected-init.sql", "/docker-entrypoint-initdb.d/01-expected-init.sql");
        URL url = Thread.currentThread().getContextClassLoader().getResource("env/mysql/8/my.cnf");
        if (null != url) {
            result.put("/env/mysql/8/my.cnf", MySQLContainer.MYSQL_CONF_IN_CONTAINER);
        } else {
            result.put("/container/mysql/cnf/8/my.cnf", MySQLContainer.MYSQL_CONF_IN_CONTAINER);
        }
        return result;
    }
    
    @Override
    public Map<String, String> getMountedResources(final int majorVersion) {
        Map<String, String> result = new HashMap<>(3, 1F);
        URL url = Thread.currentThread().getContextClassLoader().getResource(String.format("env/mysql/%s/my.cnf", majorVersion));
        if (null != url) {
            result.put(String.format("/env/mysql/%s/my.cnf", majorVersion), MySQLContainer.MYSQL_CONF_IN_CONTAINER);
        } else {
            result.put(String.format("/container/mysql/cnf/%s/my.cnf", majorVersion), MySQLContainer.MYSQL_CONF_IN_CONTAINER);
        }
        result.put("/env/mysql/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql");
        if (majorVersion > 5) {
            result.put("/env/mysql/02-grant-xa-privilege.sql", "/docker-entrypoint-initdb.d/02-grant-xa-privilege.sql");
        }
        return result;
    }
    
    @Override
    public boolean isEmbeddedStorageContainer() {
        return false;
    }
}
