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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;
import org.testcontainers.containers.BindMode;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Docker storage container.
 */
@Getter
@Slf4j
public abstract class DockerStorageContainer extends DockerITContainer implements StorageContainer {
    
    private static final String READY_USER = "ready_user";
    
    private static final String READY_USER_PASSWORD = "Ready@123";
    
    private final DatabaseType databaseType;
    
    @Getter(AccessLevel.NONE)
    private final String scenario;
    
    private final Map<String, DataSource> actualDataSourceMap;
    
    private final Map<String, DataSource> expectedDataSourceMap;
    
    protected DockerStorageContainer(final DatabaseType databaseType, final String containerImage, final String scenario) {
        super(databaseType.getType().toLowerCase(), containerImage);
        this.databaseType = databaseType;
        this.scenario = scenario;
        actualDataSourceMap = new LinkedHashMap<>();
        expectedDataSourceMap = new LinkedHashMap<>();
    }
    
    @Override
    protected void configure() {
        withClasspathResourceMapping("/container/init-sql/" + databaseType.getType().toLowerCase() + "/00-init-authority.sql", "/docker-entrypoint-initdb.d/00-init-authority.sql", BindMode.READ_ONLY);
        if (Strings.isNullOrEmpty(scenario)) {
            withClasspathResourceMapping("/env/" + databaseType.getType().toLowerCase() + "/01-initdb.sql", "/docker-entrypoint-initdb.d/01-initdb.sql", BindMode.READ_ONLY);
        } else {
            withClasspathResourceMapping(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, databaseType) + "/01-actual-init.sql", "/docker-entrypoint-initdb.d/01-actual-init.sql",
                    BindMode.READ_ONLY);
            withClasspathResourceMapping(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, databaseType) + "/01-expected-init.sql",
                    "/docker-entrypoint-initdb.d/01-expected-init.sql", BindMode.READ_ONLY);
        }
        withClasspathResourceMapping("/container/init-sql/" + databaseType.getType().toLowerCase() + "/99-be-ready.sql", "/docker-entrypoint-initdb.d/99-be-ready.sql", BindMode.READ_ONLY);
        withExposedPorts(getExposedPort());
        setWaitStrategy(new JdbcConnectionWaitStrategy(
                () -> DriverManager.getConnection(getDefaultDatabaseName().isPresent()
                        ? DataSourceEnvironment.getURL(databaseType, "localhost", getFirstMappedPort(), getDefaultDatabaseName().get())
                        : DataSourceEnvironment.getURL(databaseType, "localhost", getFirstMappedPort()), READY_USER, READY_USER_PASSWORD)));
    }
    
    protected final void setCommands(final String command) {
        if (Strings.isNullOrEmpty(command)) {
            return;
        }
        setCommand(command);
    }
    
    protected final void addEnvs(final Map<String, String> envs) {
        envs.forEach(this::addEnv);
    }
    
    protected final void mapResources(final Map<String, String> resources) {
        resources.forEach((key, value) -> withClasspathResourceMapping(key, value, BindMode.READ_ONLY));
    }
    
    @Override
    @SneakyThrows({IOException.class, JAXBException.class})
    protected void postStart() {
        if (!Strings.isNullOrEmpty(scenario)) {
            DatabaseEnvironmentManager.getDatabaseNames(scenario).forEach(each -> actualDataSourceMap.put(each, createAccessDataSource(each)));
            DatabaseEnvironmentManager.getExpectedDatabaseNames(scenario).forEach(each -> expectedDataSourceMap.put(each, createAccessDataSource(each)));
        }
    }
    
    /**
     * Create access data source.
     *
     * @param dataSourceName data source name
     * @return access data source
     */
    public DataSource createAccessDataSource(final String dataSourceName) {
        return StorageContainerUtils.generateDataSource(getJdbcUrl(dataSourceName), getUsername(), getPassword(), 4);
    }
    
    /**
     * Get JDBC URL.
     *
     * @param dataSourceName datasource name
     * @return JDBC URL
     */
    public String getJdbcUrl(final String dataSourceName) {
        return DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(), dataSourceName);
    }
    
    protected abstract Optional<String> getDefaultDatabaseName();
    
    /**
     * Get username.
     *
     * @return username
     */
    public final String getUsername() {
        return StorageContainerConstants.USERNAME;
    }
    
    /**
     * Get unified database access password.
     *
     * @return unified database access password
     */
    public final String getPassword() {
        return StorageContainerConstants.PASSWORD;
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
}
