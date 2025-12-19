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

package org.apache.shardingsphere.test.e2e.env.container.storage.option.dialect.mysql;

import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerCreateOption;
import org.apache.shardingsphere.test.e2e.env.container.util.ContainerUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage container create option for MySQL.
 */
public final class MySQLStorageContainerCreateOption implements StorageContainerCreateOption {
    
    @Override
    public int getPort() {
        return 3306;
    }
    
    @Override
    public String getDefaultImageName() {
        return "mysql:8.0.40";
    }
    
    @Override
    public String getCommand() {
        return "--server-id=" + ContainerUtils.generateMySQLServerId();
    }
    
    @Override
    public Map<String, String> getEnvironments() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("LANG", "C.UTF-8");
        result.put("MYSQL_RANDOM_ROOT_PASSWORD", "yes");
        return result;
    }
    
    @Override
    public Collection<String> getMountedConfigurationResources() {
        return Collections.singleton("/etc/mysql/my.cnf");
    }
    
    @Override
    public Collection<String> getAdditionalEnvMountedSQLResources(final int majorVersion) {
        Collection<String> result = new LinkedList<>();
        if (majorVersion > 5) {
            result.add("21-env-grant-xa-privilege.sql");
        }
        return result;
    }
    
    @Override
    public List<Integer> getSupportedMajorVersions() {
        return Arrays.asList(8, 5);
    }
    
    @Override
    public boolean withPrivilegedMode() {
        return false;
    }
    
    @Override
    public Optional<String> getDefaultDatabaseName(final int majorVersion) {
        return Optional.empty();
    }
    
    @Override
    public long getStartupTimeoutSeconds() {
        return 120L;
    }
}
