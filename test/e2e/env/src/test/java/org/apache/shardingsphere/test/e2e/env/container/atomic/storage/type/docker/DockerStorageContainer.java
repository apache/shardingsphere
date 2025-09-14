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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.docker;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.mount.MountConfigurationResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.mount.MountSQLResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DockerImageVersion;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Docker storage container.
 */
public abstract class DockerStorageContainer extends DockerITContainer implements StorageContainer {
    
    private final DatabaseType databaseType;
    
    private final StorageContainerConfiguration storageContainerConfig;
    
    private final int majorVersion;
    
    @Getter
    private final Map<String, DataSource> actualDataSourceMap = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, DataSource> expectedDataSourceMap = new LinkedHashMap<>();
    
    protected DockerStorageContainer(final DatabaseType databaseType, final String containerImage, final StorageContainerConfiguration storageContainerConfig) {
        super(databaseType.getType().toLowerCase(), containerImage);
        this.databaseType = databaseType;
        this.storageContainerConfig = storageContainerConfig;
        majorVersion = new DockerImageVersion(containerImage).getMajorVersion();
    }
    
    @Override
    protected final void configure() {
        setCommands();
        addEnvironments();
        mapResources(new MountConfigurationResourceGenerator(storageContainerConfig.getConfigurationOption(), databaseType).generate(majorVersion, storageContainerConfig.getScenario()));
        mapResources(new MountSQLResourceGenerator(storageContainerConfig.getConfigurationOption(), databaseType).generate(majorVersion, storageContainerConfig.getScenario()));
        setPrivilegedMode();
        withExposedPorts(getExposedPort());
        withStartupTimeout(Duration.of(storageContainerConfig.getConfigurationOption().getStartupTimeoutSeconds(), ChronoUnit.SECONDS));
        setWaitStrategy(new JdbcConnectionWaitStrategy(() -> DriverManager.getConnection(getURL(), StorageContainerConstants.CHECK_READY_USER, StorageContainerConstants.CHECK_READY_PASSWORD)));
    }
    
    private void setCommands() {
        String command = storageContainerConfig.getConfigurationOption().getCommand();
        if (!Strings.isNullOrEmpty(command)) {
            setCommand(command);
        }
    }
    
    private void addEnvironments() {
        storageContainerConfig.getConfigurationOption().getEnvironments().forEach(this::addEnv);
    }
    
    private void setPrivilegedMode() {
        if (storageContainerConfig.getConfigurationOption().withPrivilegedMode()) {
            withPrivilegedMode(true);
        }
    }
    
    private String getURL() {
        return storageContainerConfig.getConfigurationOption().getDefaultDatabaseName(majorVersion)
                .map(optional -> DataSourceEnvironment.getURL(databaseType, "localhost", getFirstMappedPort(), optional))
                .orElseGet(() -> DataSourceEnvironment.getURL(databaseType, "localhost", getFirstMappedPort()));
    }
    
    @Override
    protected void postStart() {
        actualDataSourceMap.putAll(createAccessDataSources(getDataSourceNames(storageContainerConfig.getActualDatabaseTypes())));
        expectedDataSourceMap.putAll(createAccessDataSources(getDataSourceNames(storageContainerConfig.getExpectedDatabaseTypes())));
    }
    
    private Collection<String> getDataSourceNames(final Map<String, DatabaseType> dataSourceNameAndTypeMap) {
        return dataSourceNameAndTypeMap.entrySet().stream().filter(entry -> entry.getValue() == databaseType).map(Entry::getKey).collect(Collectors.toList());
    }
    
    private Map<String, DataSource> createAccessDataSources(final Collection<String> dataSourceNames) {
        return dataSourceNames.stream().distinct().collect(Collectors.toMap(Function.identity(), this::createAccessDataSource));
    }
    
    /**
     * Create access data source.
     *
     * @param dataSourceName data source name
     * @return access data source
     */
    public final DataSource createAccessDataSource(final String dataSourceName) {
        return StorageContainerUtils.generateDataSource(getJdbcUrl(dataSourceName), StorageContainerConstants.OPERATION_USER, StorageContainerConstants.OPERATION_PASSWORD, 20);
    }
    
    /**
     * Get JDBC URL.
     *
     * @param dataSourceName datasource name
     * @return JDBC URL
     */
    public final String getJdbcUrl(final String dataSourceName) {
        return DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(),
                Strings.isNullOrEmpty(dataSourceName) ? storageContainerConfig.getConfigurationOption().getDefaultDatabaseName(majorVersion).orElse("") : dataSourceName);
    }
    
    /**
     * Get database container exposed port.
     *
     * @return exposed database container port
     */
    public abstract int getExposedPort();
    
    /**
     * Get database container mapped port.
     *
     * @return mapped database container port
     */
    public abstract int getMappedPort();
    
    @Override
    public final String getAbbreviation() {
        return databaseType.getType().toLowerCase();
    }
    
    @Override
    public final Map<String, String> getLinkReplacements() {
        Map<String, String> result = new HashMap<>();
        for (String each : getNetworkAliases()) {
            for (Integer exposedPort : getExposedPorts()) {
                result.put(each + ":" + exposedPort, getHost() + ":" + getMappedPort(exposedPort));
            }
        }
        return result;
    }
}
