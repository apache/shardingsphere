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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MySQL container.
 */
public final class MySQLContainer extends DockerStorageContainer {
    
    public static final int MYSQL_EXPOSED_PORT = 3306;
    
    public static final String MYSQL_CONF_IN_CONTAINER = "/etc/mysql/my.cnf";
    
    private final StorageContainerConfiguration storageContainerConfig;
    
    public MySQLContainer(final String containerImage, final StorageContainerConfiguration storageContainerConfig) {
        super(TypedSPILoader.getService(DatabaseType.class, "MySQL"), Strings.isNullOrEmpty(containerImage) ? "mysql:5.7" : containerImage);
        this.storageContainerConfig = storageContainerConfig;
    }
    
    @Override
    protected void configure() {
        setCommands(storageContainerConfig.getContainerCommand());
        addEnvs(storageContainerConfig.getContainerEnvironments());
        mapResources(storageContainerConfig.getMountedResources());
        super.configure();
    }
    
    @Override
    protected Collection<String> getDatabaseNames() {
        return storageContainerConfig.getDatabaseTypes().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof MySQLDatabaseType).map(Entry::getKey).collect(Collectors.toList());
    }
    
    @Override
    protected Collection<String> getExpectedDatabaseNames() {
        return storageContainerConfig.getExpectedDatabaseTypes().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof MySQLDatabaseType).map(Entry::getKey).collect(Collectors.toList());
    }
    
    @Override
    public int getExposedPort() {
        return MYSQL_EXPOSED_PORT;
    }
    
    @Override
    public int getMappedPort() {
        return getMappedPort(MYSQL_EXPOSED_PORT);
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.empty();
    }
}
