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
import org.apache.shardingsphere.test.e2e.env.container.DockerE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.mount.MountConfigurationResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.storage.mount.MountSQLResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.util.DockerImageVersion;
import org.apache.shardingsphere.test.e2e.env.container.util.JdbcConnectCheckingWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.container.util.SQLScriptUtils;
import org.apache.shardingsphere.test.e2e.env.container.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath.Type;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Docker storage container.
 */
public final class DockerStorageContainer extends DockerE2EContainer implements StorageContainer {
    
    private final StorageContainerOption option;
    
    private final String scenario;
    
    @Getter
    private final int majorVersion;
    
    @Getter
    private final Map<String, DataSource> actualDataSourceMap = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, DataSource> expectedDataSourceMap = new LinkedHashMap<>();
    
    public DockerStorageContainer(final String containerImage, final StorageContainerOption option, final String scenario) {
        super(option.getDatabaseType().toLowerCase(), getContainerImage(containerImage, option));
        this.option = option;
        this.scenario = scenario;
        majorVersion = new DockerImageVersion(getContainerImage(containerImage, option)).getMajorVersion();
    }
    
    private static String getContainerImage(final String containerImage, final StorageContainerOption option) {
        Preconditions.checkNotNull(option, "Can not support database type `%s`", option.getDatabaseType());
        return Strings.isNullOrEmpty(containerImage) ? option.getCreateOption().getDefaultImageName() : containerImage;
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
        String command = option.getCreateOption().getCommand();
        if (!Strings.isNullOrEmpty(command)) {
            setCommand(command);
        }
    }
    
    private void addEnvironments() {
        option.getCreateOption().getEnvironments().forEach(this::addEnv);
    }
    
    private void mountConfigurations() {
        mapResources(new MountConfigurationResourceGenerator(option.getType(), option.getCreateOption()).generate(majorVersion, scenario));
    }
    
    private void mountSQLFiles() {
        if (option.getCreateOption().isSupportDockerEntrypoint()) {
            mapResources(new MountSQLResourceGenerator(option.getType(), option.getCreateOption()).generate(majorVersion, scenario));
        }
    }
    
    private void setPrivilegedMode() {
        if (option.getCreateOption().withPrivilegedMode()) {
            withPrivilegedMode(true);
        }
    }
    
    private void setWaitStrategy() {
        String user = option.getCreateOption().isSupportDockerEntrypoint()
                ? StorageContainerConstants.CHECK_READY_USER
                : option.getCreateOption().getDefaultUserWhenUnsupportedDockerEntrypoint().orElse("");
        String password = option.getCreateOption().isSupportDockerEntrypoint()
                ? StorageContainerConstants.CHECK_READY_PASSWORD
                : option.getCreateOption().getDefaultPasswordWhenUnsupportedDockerEntrypoint().orElse("");
        setWaitStrategy(new JdbcConnectCheckingWaitStrategy(() -> DriverManager.getConnection(getURL(), user, password)));
        withStartupTimeout(Duration.ofSeconds(option.getCreateOption().getStartupTimeoutSeconds()));
    }
    
    private String getURL() {
        return option.getCreateOption().getDefaultDatabaseName(majorVersion)
                .map(optional -> option.getConnectOption().getURL("localhost", getFirstMappedPort(), optional))
                .orElseGet(() -> option.getConnectOption().getURL("localhost", getFirstMappedPort()));
    }
    
    @SneakyThrows(SQLException.class)
    @Override
    protected void containerIsStarted(final InspectContainerResponse containerInfo) {
        if (option.getCreateOption().isSupportDockerEntrypoint()) {
            return;
        }
        try (
                Connection connection = DriverManager.getConnection(option.getConnectOption().getURL("localhost", getFirstMappedPort()),
                        option.getCreateOption().getDefaultUserWhenUnsupportedDockerEntrypoint().orElse(""),
                        option.getCreateOption().getDefaultPasswordWhenUnsupportedDockerEntrypoint().orElse(""))) {
            for (String each : new MountSQLResourceGenerator(option.getType(), option.getCreateOption()).generate(majorVersion, scenario).keySet()) {
                SQLScriptUtils.execute(connection, each);
            }
        }
    }
    
    @Override
    protected void postStart() {
        actualDataSourceMap.putAll(createAccessDataSources(DatabaseEnvironmentManager.getDatabaseNames(scenario, Type.ACTUAL)));
        expectedDataSourceMap.putAll(createAccessDataSources(DatabaseEnvironmentManager.getDatabaseNames(scenario, Type.EXPECTED)));
    }
    
    private Map<String, DataSource> createAccessDataSources(final Collection<String> databaseNames) {
        return databaseNames.stream().distinct().collect(Collectors.toMap(Function.identity(), this::createAccessDataSource));
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
        return option.getConnectOption().getURL(getHost(), getMappedPort(), getToBeConnectedDataSourceName(dataSourceName));
    }
    
    /**
     * Get to be connected data source name.
     *
     * @param dataSourceName datasource name
     * @return to be connected data source name
     */
    public String getToBeConnectedDataSourceName(final String dataSourceName) {
        return Strings.isNullOrEmpty(dataSourceName) ? option.getCreateOption().getDefaultDatabaseName(majorVersion).orElse("") : dataSourceName;
    }
    
    /**
     * Get database container exposed port.
     *
     * @return exposed database container port
     */
    public int getExposedPort() {
        return option.getCreateOption().getPort();
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
