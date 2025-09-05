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
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.PostgreSQLContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Storage container configuration option for  PostgreSQL.
 */
public final class PostgreSQLStorageContainerConfigurationOption implements StorageContainerConfigurationOption {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Override
    public String getCommand() {
        return "-c config_file=" + PostgreSQLContainer.POSTGRESQL_CONF_IN_CONTAINER + " --max_connections=600 --max_prepared_transactions=600 --wal_level=logical";
    }
    
    @Override
    public Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("POSTGRES_HOST", StorageContainerConstants.USERNAME);
        result.put("POSTGRES_PASSWORD", StorageContainerConstants.PASSWORD);
        return result;
    }
    
    @Override
    public Map<String, String> getMountedResources() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("/env/postgresql/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql");
        result.put("/env/postgresql/postgresql.conf", PostgreSQLContainer.POSTGRESQL_CONF_IN_CONTAINER);
        return result;
    }
    
    @Override
    public Map<String, String> getMountedResources(final String scenario) {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, databaseType) + "/01-actual-init.sql", "/docker-entrypoint-initdb.d/01-actual-init.sql");
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, databaseType) + "/01-expected-init.sql", "/docker-entrypoint-initdb.d/01-expected-init.sql");
        result.put("/env/postgresql/postgresql.conf", PostgreSQLContainer.POSTGRESQL_CONF_IN_CONTAINER);
        return result;
    }
    
    @Override
    public Map<String, String> getMountedResources(final int majorVersion) {
        return getMountedResources();
    }
    
    @Override
    public boolean isEmbeddedStorageContainer() {
        return false;
    }
    
    @Override
    public boolean isRecognizeMajorVersion() {
        return false;
    }
}
