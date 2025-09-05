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

import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MariaDBContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.ContainerUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage container configuration option for MariaDB.
 */
public final class MariaDBStorageContainerConfigurationOption implements StorageContainerConfigurationOption {
    
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
    public Map<String, String> getMountedResources() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("/env/mysql/mysql8/my.cnf", MariaDBContainer.MARIADB_CONF_IN_CONTAINER);
        result.put("/env/mysql/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql");
        return result;
    }
    
    @Override
    public Map<String, String> getMountedResources(final String scenario) {
        return Collections.emptyMap();
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
