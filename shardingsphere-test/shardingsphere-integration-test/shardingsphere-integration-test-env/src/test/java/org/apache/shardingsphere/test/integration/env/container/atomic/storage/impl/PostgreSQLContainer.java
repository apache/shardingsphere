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

package org.apache.shardingsphere.test.integration.env.container.atomic.storage.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.StorageContainerConfiguration;

import java.util.Optional;

/**
 * PostgreSQL container.
 */
public final class PostgreSQLContainer extends DockerStorageContainer {
    
    private final StorageContainerConfiguration storageContainerConfiguration;
    
    public PostgreSQLContainer(final String containerImage, final String scenario, final StorageContainerConfiguration storageContainerConfiguration) {
        super(DatabaseTypeFactory.getInstance("PostgreSQL"), Strings.isNullOrEmpty(containerImage) ? "postgres:12-alpine" : containerImage, scenario);
        this.storageContainerConfiguration = storageContainerConfiguration;
    }
    
    @Override
    protected void configure() {
        setCommands(storageContainerConfiguration.getContainerCommand());
        addEnvs(storageContainerConfiguration.getContainerEnvironments());
        mapResources(storageContainerConfiguration.getMountedResources());
        super.configure();
    }
    
    @Override
    public int getExposedPort() {
        return StorageContainerConstants.POSTGRESQL_EXPOSED_PORT;
    }
    
    @Override
    public int getMappedPort() {
        return getMappedPort(StorageContainerConstants.POSTGRESQL_EXPOSED_PORT);
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.of("postgres");
    }
}
