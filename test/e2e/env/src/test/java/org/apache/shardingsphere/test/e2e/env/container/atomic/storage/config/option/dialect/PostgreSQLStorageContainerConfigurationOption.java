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

import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.StorageContainerConfigurationOption;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage container configuration option for  PostgreSQL.
 */
public final class PostgreSQLStorageContainerConfigurationOption implements StorageContainerConfigurationOption {
    
    @Override
    public int getPort() {
        return 5432;
    }
    
    @Override
    public String getDefaultImageName() {
        return "postgres:12-alpine";
    }
    
    @Override
    public String getCommand() {
        return "-c config_file=/etc/postgresql/postgresql.conf";
    }
    
    @Override
    public Map<String, String> getEnvironments() {
        Map<String, String> result = new HashMap<>(2, 1F);
        result.put("POSTGRES_HOST", StorageContainerConstants.OPERATION_USER);
        result.put("POSTGRES_PASSWORD", StorageContainerConstants.OPERATION_PASSWORD);
        return result;
    }
    
    @Override
    public Collection<String> getMountedConfigurationResources() {
        return Collections.singleton("/etc/postgresql/postgresql.conf");
    }
    
    @Override
    public Collection<String> getAdditionalEnvMountedSQLResources(final int majorVersion) {
        return Collections.emptyList();
    }
    
    @Override
    public List<Integer> getSupportedMajorVersions() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean withPrivilegedMode() {
        return false;
    }
    
    @Override
    public Optional<String> getDefaultDatabaseName(final int majorVersion) {
        return Optional.of("postgres");
    }
    
    @Override
    public long getStartupTimeoutSeconds() {
        return 120L;
    }
}
