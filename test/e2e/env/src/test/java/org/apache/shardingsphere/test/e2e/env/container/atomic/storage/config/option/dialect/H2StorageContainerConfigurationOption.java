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
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage container configuration option for H2.
 */
public final class H2StorageContainerConfigurationOption implements StorageContainerConfigurationOption {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Override
    public String getCommand() {
        return "";
    }
    
    @Override
    public Map<String, String> getContainerEnvironments() {
        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, String> getMountedResources() {
        return Collections.singletonMap("/env/mysql/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql");
    }
    
    @Override
    public Map<String, String> getMountedResources(final String scenario) {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, databaseType) + "/01-actual-init.sql", "/docker-entrypoint-initdb.d/01-actual-init.sql");
        result.put(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, databaseType) + "/01-expected-init.sql", "/docker-entrypoint-initdb.d/01-expected-init.sql");
        return result;
    }
    
    @Override
    public Map<String, String> getMountedResources(final int majorVersion) {
        return getMountedResources();
    }
    
    @Override
    public boolean isEmbeddedStorageContainer() {
        return true;
    }
    
    @Override
    public boolean isRecognizeMajorVersion() {
        return false;
    }
}
