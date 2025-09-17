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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.mount.MountConfigurationResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.mount.MountSQLResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DockerImageVersion;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.datasource.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Docker storage container.
 */
public class DockerStorageContainer extends DockerITContainer implements StorageContainer {
    
    private final StorageContainerConfigurationOption option;
    
    private final String scenario;
    
    private final int majorVersion;
    
    @Getter
    private final Map<String, DataSource> actualDataSourceMap = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, DataSource> expectedDataSourceMap = new LinkedHashMap<>();
    
    public DockerStorageContainer(final String containerImage, final StorageContainerConfigurationOption option, final String scenario) {
        super(option.getDatabaseType().toLowerCase(), getContainerImage(containerImage, option));
        this.option = option;
        this.scenario = scenario;
        majorVersion = new DockerImageVersion(getContainerImage(containerImage, option)).getMajorVersion();
    }
    
    private static String getContainerImage(final String containerImage, final StorageContainerConfigurationOption option) {
        Preconditions.checkNotNull(option, "Can not support database type `%s`", option.getDatabaseType());
        return Strings.isNullOrEmpty(containerImage) ? option.getDefaultImageName() : containerImage;
    }
    
    @Override
    protected final void configure() {
        setCommands();
        addEnvironments();
        mapResources(new MountConfigurationResourceGenerator(option).generate(majorVersion, scenario));
        mapResources(new MountSQLResourceGenerator(option).generate(majorVersion, scenario));
        setPrivilegedMode();
        withExposedPorts(getExposedPort());
        setWaitStrategy(new JdbcConnectionWaitStrategy(() -> DriverManager.getConnection(getURL(), StorageContainerConstants.CHECK_READY_USER, StorageContainerConstants.CHECK_READY_PASSWORD)));
        withStartupTimeout(Duration.ofSeconds(option.getStartupTimeoutSeconds()));
    }
    
    private void setCommands() {
        String command = option.getCommand();
        if (!Strings.isNullOrEmpty(command)) {
            setCommand(command);
        }
    }
    
    private void addEnvironments() {
        option.getEnvironments().forEach(this::addEnv);
    }
    
    private void setPrivilegedMode() {
        if (option.withPrivilegedMode()) {
            withPrivilegedMode(true);
        }
    }
    
    private String getURL() {
        DataSourceEnvironment dataSourceEnvironment = DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, option.getType());
        return option.getDefaultDatabaseName(majorVersion)
                .map(optional -> dataSourceEnvironment.getURL("localhost", getFirstMappedPort(), optional))
                .orElseGet(() -> dataSourceEnvironment.getURL("localhost", getFirstMappedPort()));
    }
    
    @Override
    protected void postStart() {
        actualDataSourceMap.putAll(createAccessDataSources(getDataSourceNames(getDataSourceNameAndTypeMap(Type.ACTUAL))));
        expectedDataSourceMap.putAll(createAccessDataSources(getDataSourceNames(getDataSourceNameAndTypeMap(Type.EXPECTED))));
    }
    
    private Map<String, DatabaseType> getDataSourceNameAndTypeMap(final Type type) {
        return null == scenario ? Collections.emptyMap() : DatabaseEnvironmentManager.getDatabaseTypes(scenario, option.getType(), type);
    }
    
    private Collection<String> getDataSourceNames(final Map<String, DatabaseType> dataSourceNameAndTypeMap) {
        return dataSourceNameAndTypeMap.entrySet().stream().filter(entry -> entry.getValue() == option.getType()).map(Entry::getKey).collect(Collectors.toList());
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
        DataSourceEnvironment dataSourceEnvironment = DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, option.getType());
        return dataSourceEnvironment.getURL(getHost(), getMappedPort(), Strings.isNullOrEmpty(dataSourceName) ? option.getDefaultDatabaseName(majorVersion).orElse("") : dataSourceName);
    }
    
    /**
     * Get database container exposed port.
     *
     * @return exposed database container port
     */
    public final int getExposedPort() {
        return option.getPort();
    }
    
    /**
     * Get database container mapped port.
     *
     * @return mapped database container port
     */
    public final int getMappedPort() {
        return getMappedPort(getExposedPort());
    }
    
    @Override
    public final String getAbbreviation() {
        return option.getDatabaseType().toLowerCase();
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
