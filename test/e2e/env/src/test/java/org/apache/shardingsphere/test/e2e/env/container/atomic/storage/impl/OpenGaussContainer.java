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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * OpenGauss container.
 */
public final class OpenGaussContainer extends DockerStorageContainer {
    
    public static final int OPENGAUSS_EXPOSED_PORT = 5432;
    
    public static final String OPENGAUSS_CONF_IN_CONTAINER = "/usr/local/opengauss/share/postgresql/postgresql.conf.sample";
    
    public static final String OPENGAUSS_HBA_IN_CONF_CONTAINER = "/usr/local/opengauss/share/postgresql/pg_hba.conf.sample";
    
    private final StorageContainerConfiguration storageContainerConfig;
    
    public OpenGaussContainer(final String containerImage, final String scenario, final StorageContainerConfiguration storageContainerConfig) {
        super(TypedSPILoader.getService(DatabaseType.class, "openGauss"), Strings.isNullOrEmpty(containerImage) ? "enmotech/opengauss:3.0.0" : containerImage, scenario);
        this.storageContainerConfig = storageContainerConfig;
    }
    
    @Override
    protected void configure() {
        setCommands(storageContainerConfig.getContainerCommand());
        addEnvs(storageContainerConfig.getContainerEnvironments());
        mapResources(storageContainerConfig.getMountedResources());
        withPrivilegedMode(true);
        super.configure();
        withStartupTimeout(Duration.of(120, ChronoUnit.SECONDS));
    }
    
    @Override
    public int getExposedPort() {
        return OPENGAUSS_EXPOSED_PORT;
    }
    
    @Override
    public int getMappedPort() {
        return getMappedPort(OPENGAUSS_EXPOSED_PORT);
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.of(StorageContainerConstants.USERNAME);
    }
    
    @Override
    public String getJdbcUrl(final String dataSourceName) {
        return DataSourceEnvironment.getURL(getDatabaseType(), getHost(), getMappedPort(), Strings.isNullOrEmpty(dataSourceName) ? StorageContainerConstants.USERNAME : dataSourceName);
    }
}
