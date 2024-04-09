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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.mariadb;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MariaDBContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.ContainerUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * MariaDB container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MariaDBContainerConfigurationFactory {
    
    /**
     * Create new instance of MariaDB container configuration.
     *
     * @return created instance
     */
    public static StorageContainerConfiguration newInstance() {
        return new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(), Collections.emptyMap(), Collections.emptyMap());
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
    
    private static Map<String, String> getMountedResources() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("/env/mysql/mysql8/my.cnf", MariaDBContainer.MARIADB_CONF_IN_CONTAINER);
        result.put("/env/mysql/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql");
        return result;
    }
}
