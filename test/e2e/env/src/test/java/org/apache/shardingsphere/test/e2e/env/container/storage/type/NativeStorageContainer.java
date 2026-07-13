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

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.mount.MountSQLResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.NativeStorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.util.SQLScriptUtils;
import org.apache.shardingsphere.test.e2e.env.container.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.NativeDatabaseEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.path.ScenarioDataPath.Type;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Native storage container.
 */
public final class NativeStorageContainer implements StorageContainer {
    
    private static final Collection<String> INITIALIZED_DATABASES = new HashSet<>();
    
    private static final String SQL_RESOURCE_SUFFIX = ".sql";
    
    private final NativeDatabaseEnvironment env;
    
    @Getter
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final StorageContainerOption option;
    
    private final NativeStorageContainerOption nativeOption;
    
    @Getter
    private final Map<String, DataSource> actualDataSourceMap;
    
    @Getter
    private final Map<String, DataSource> expectedDataSourceMap;
    
    @Getter
    @Setter
    private List<String> networkAliases;
    
    public NativeStorageContainer(final DatabaseType databaseType, final String scenario) {
        env = E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment();
        this.databaseType = databaseType;
        this.scenario = scenario;
        option = DatabaseTypedSPILoader.findService(StorageContainerOption.class, databaseType).orElse(null);
        nativeOption = DatabaseTypedSPILoader.findService(NativeStorageContainerOption.class, databaseType).orElse(null);
        initDatabase();
        actualDataSourceMap = createDataSourceMap(Type.ACTUAL);
        expectedDataSourceMap = createDataSourceMap(Type.EXPECTED);
    }
    
    private void initDatabase() {
        if (null == option) {
            return;
        }
        synchronized (INITIALIZED_DATABASES) {
            String initDatabaseCacheKey = getInitDatabaseCacheKey();
            if (!INITIALIZED_DATABASES.contains(initDatabaseCacheKey)) {
                initDatabase(initDatabaseCacheKey);
            }
        }
    }
    
    private void initDatabase(final String initDatabaseCacheKey) {
        DataSource dataSource = createInitDataSource();
        Map<String, String> mountedResources = generateMountedResources();
        executeSQLScripts(dataSource, mountedResources.keySet());
        INITIALIZED_DATABASES.add(initDatabaseCacheKey);
    }
    
    private Map<String, String> generateMountedResources() {
        return new MountSQLResourceGenerator(option.getType(), option.getCreateOption()).generate(getMajorVersion(), scenario);
    }
    
    private int getMajorVersion() {
        return Optional.ofNullable(nativeOption).map(NativeStorageContainerOption::getMajorVersion).orElseGet(this::getDefaultMajorVersion);
    }
    
    private int getDefaultMajorVersion() {
        List<Integer> supportedMajorVersions = option.getCreateOption().getSupportedMajorVersions();
        return supportedMajorVersions.isEmpty() ? 0 : supportedMajorVersions.get(0);
    }
    
    private void executeSQLScripts(final DataSource dataSource, final Collection<String> mountedResources) {
        for (String each : mountedResources) {
            if (each.endsWith(SQL_RESOURCE_SUFFIX)) {
                SQLScriptUtils.execute(dataSource, each);
            }
        }
    }
    
    private DataSource createInitDataSource() {
        String jdbcUrl = Optional.ofNullable(nativeOption)
                .map(optional -> optional.getInitURL(option.getConnectOption(), env.getHost(), env.getPort(databaseType)))
                .orElseGet(() -> option.getConnectOption().getURL(env.getHost(), env.getPort(databaseType)));
        HikariDataSource result = (HikariDataSource) StorageContainerUtils.generateDataSource(jdbcUrl, getInitUser(), env.getPassword(), 2);
        Optional.ofNullable(nativeOption).ifPresent(optional -> optional.configureInitDataSource(result));
        return result;
    }
    
    private String getInitUser() {
        return Optional.ofNullable(nativeOption).map(optional -> optional.getInitUser(env.getUser())).orElseGet(env::getUser);
    }
    
    private String getInitDatabaseCacheKey() {
        return String.join(":", String.valueOf(scenario), databaseType.getType(), env.getHost(), String.valueOf(env.getPort(databaseType)), String.valueOf(getMajorVersion()));
    }
    
    private Map<String, DataSource> createDataSourceMap(final Type type) {
        return null == scenario ? Collections.emptyMap() : getDataSourceMap(DatabaseEnvironmentManager.getDatabaseNames(scenario, type));
    }
    
    private Map<String, DataSource> getDataSourceMap(final Collection<String> databaseNames) {
        Map<String, DataSource> result = new LinkedHashMap<>(databaseNames.size(), 1F);
        for (String each : databaseNames) {
            DataSource dataSource = StorageContainerUtils.generateDataSource(getAccessURL(each), env.getUser(), env.getPassword(), 2);
            initNativeDataSource(each, dataSource);
            result.put(each, dataSource);
        }
        return result;
    }
    
    private String getAccessURL(final String dataSourceName) {
        return Optional.ofNullable(nativeOption).map(optional -> optional.getAccessURL(option.getConnectOption(), env.getHost(), env.getPort(databaseType), dataSourceName))
                .orElseGet(() -> null == dataSourceName || dataSourceName.isEmpty()
                        ? option.getConnectOption().getURL(env.getHost(), env.getPort(databaseType))
                        : option.getConnectOption().getURL(env.getHost(), env.getPort(databaseType), dataSourceName));
    }
    
    private void initNativeDataSource(final String dataSourceName, final DataSource dataSource) {
        Optional.ofNullable(nativeOption).ifPresent(optional -> optional.configureAccessDataSource((HikariDataSource) dataSource, dataSourceName));
    }
    
    /**
     * Get exposed port.
     *
     * @return exposed port
     */
    public int getExposedPort() {
        return null == option ? 0 : option.getCreateOption().getPort();
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public String getAbbreviation() {
        return databaseType.getType().toLowerCase();
    }
    
    @Override
    public Map<String, String> getLinkReplacements() {
        Map<String, String> result = new LinkedHashMap<>(getNetworkAliases().size() + 2, 1F);
        for (String each : getNetworkAliases()) {
            result.putAll(Optional.ofNullable(nativeOption)
                    .map(optional -> optional.getLinkReplacements(option.getConnectOption(), each, env.getHost(), env.getPort(databaseType), getExposedPort()))
                    .orElseGet(() -> Collections.singletonMap(each + ":" + getExposedPort(), env.getHost() + ":" + env.getPort(databaseType))));
        }
        return result;
    }
}
