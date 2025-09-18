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

package org.apache.shardingsphere.test.e2e.env.container.storage.type;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.mount.MountConfigurationResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.storage.mount.MountSQLResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.util.DockerImageVersion;
import org.apache.shardingsphere.test.e2e.env.container.util.JdbcConnectCheckingWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.container.util.SQLScriptUtils;
import org.apache.shardingsphere.test.e2e.env.container.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.datasource.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
public final class DockerStorageContainer extends DockerITContainer implements StorageContainer {
    
    private final StorageContainerOption option;
    
    private final String scenario;
    
    private final int majorVersion;
    
    private final DataSourceEnvironment dataSourceEnvironment;
    
    @Getter
    private final Map<String, DataSource> actualDataSourceMap = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, DataSource> expectedDataSourceMap = new LinkedHashMap<>();
    
    public DockerStorageContainer(final String containerImage, final StorageContainerOption option, final String scenario) {
        super(option.getDatabaseType().toLowerCase(), getContainerImage(containerImage, option));
        this.option = option;
        this.scenario = scenario;
        majorVersion = new DockerImageVersion(getContainerImage(containerImage, option)).getMajorVersion();
        dataSourceEnvironment = DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, option.getType());
    }
    
    private static String getContainerImage(final String containerImage, final StorageContainerOption option) {
        Preconditions.checkNotNull(option, "Can not support database type `%s`", option.getDatabaseType());
        return Strings.isNullOrEmpty(containerImage) ? option.getDefaultImageName() : containerImage;
    }
    
    @Override
    protected void configure() {
        setCommands();
        addEnvironments();
        mountConfigurations();
        mountSQLFiles();
        setPrivilegedMode();
        withExposedPorts(getExposedPort());
        setWaitStrategy();
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
    
    private void mountConfigurations() {
        mapResources(new MountConfigurationResourceGenerator(option).generate(majorVersion, scenario));
    }
    
    private void mountSQLFiles() {
        if (option.isSupportDockerEntrypoint()) {
            mapResources(new MountSQLResourceGenerator(option).generate(majorVersion, scenario));
        }
    }
    
    private void setPrivilegedMode() {
        if (option.withPrivilegedMode()) {
            withPrivilegedMode(true);
        }
    }
    
    private void setWaitStrategy() {
        String user = option.isSupportDockerEntrypoint() ? StorageContainerConstants.CHECK_READY_USER : option.getDefaultUserWhenUnsupportedDockerEntrypoint().orElse("");
        String password = option.isSupportDockerEntrypoint() ? StorageContainerConstants.CHECK_READY_PASSWORD : option.getDefaultPasswordWhenUnsupportedDockerEntrypoint().orElse("");
        setWaitStrategy(new JdbcConnectCheckingWaitStrategy(() -> DriverManager.getConnection(getURL(), user, password)));
        withStartupTimeout(Duration.ofSeconds(option.getStartupTimeoutSeconds()));
    }
    
    private String getURL() {
        return option.getDefaultDatabaseName(majorVersion)
                .map(optional -> dataSourceEnvironment.getURL("localhost", getFirstMappedPort(), optional))
                .orElseGet(() -> dataSourceEnvironment.getURL("localhost", getFirstMappedPort()));
    }
    
    @SneakyThrows({SQLException.class, InterruptedException.class})
    @Override
    protected void containerIsStarted(final InspectContainerResponse containerInfo) {
        if (option.isSupportDockerEntrypoint()) {
            return;
        }
        Thread.sleep(10000L);
        try (
                Connection connection = DriverManager.getConnection(dataSourceEnvironment.getURL("localhost", getFirstMappedPort()),
                        option.getDefaultUserWhenUnsupportedDockerEntrypoint().orElse(""), option.getDefaultPasswordWhenUnsupportedDockerEntrypoint().orElse(""))) {
            for (String each : new MountSQLResourceGenerator(option).generate(majorVersion, scenario).keySet()) {
                SQLScriptUtils.execute(connection, each);
            }
        }
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
    public DataSource createAccessDataSource(final String dataSourceName) {
        return StorageContainerUtils.generateDataSource(getJdbcUrl(dataSourceName), StorageContainerConstants.OPERATION_USER, StorageContainerConstants.OPERATION_PASSWORD, 20);
    }
    
    /**
     * Get JDBC URL.
     *
     * @param dataSourceName datasource name
     * @return JDBC URL
     */
    public String getJdbcUrl(final String dataSourceName) {
        DataSourceEnvironment dataSourceEnvironment = DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, option.getType());
        return dataSourceEnvironment.getURL(getHost(), getMappedPort(), Strings.isNullOrEmpty(dataSourceName) ? option.getDefaultDatabaseName(majorVersion).orElse("") : dataSourceName);
    }
    
    /**
     * Get database container exposed port.
     *
     * @return exposed database container port
     */
    public int getExposedPort() {
        return option.getPort();
    }
    
    /**
     * Get database container mapped port.
     *
     * @return mapped database container port
     */
    public int getMappedPort() {
        return getMappedPort(getExposedPort());
    }
    
    @Override
    public String getAbbreviation() {
        return getName();
    }
    
    @Override
    public Map<String, String> getLinkReplacements() {
        Map<String, String> result = new HashMap<>();
        for (String each : getNetworkAliases()) {
            for (Integer exposedPort : getExposedPorts()) {
                result.put(each + ":" + exposedPort, getHost() + ":" + getMappedPort(exposedPort));
            }
        }
        return result;
    }
}
