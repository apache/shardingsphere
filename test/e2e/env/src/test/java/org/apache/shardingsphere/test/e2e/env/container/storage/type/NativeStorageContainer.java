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

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.storage.mount.MountSQLResourceGenerator;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        return new MountSQLResourceGenerator(option.getType(), option.getCreateOption()).generate(getDefaultMajorVersion(), scenario);
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
        return StorageContainerUtils.generateDataSource(option.getConnectOption().getURL(env.getHost(), env.getPort(databaseType)),
                getInitUser(), env.getPassword(), 2);
    }
    
    private String getInitUser() {
        return env.getUser();
    }
    
    private String getInitDatabaseCacheKey() {
        return String.join(":", String.valueOf(scenario), databaseType.getType(), env.getHost(), String.valueOf(env.getPort(databaseType)), String.valueOf(getDefaultMajorVersion()));
    }
    
    private Map<String, DataSource> createDataSourceMap(final Type type) {
        return null == scenario ? Collections.emptyMap() : getDataSourceMap(DatabaseEnvironmentManager.getDatabaseNames(scenario, type));
    }
    
    private Map<String, DataSource> getDataSourceMap(final Collection<String> databaseNames) {
        Map<String, DataSource> result = new LinkedHashMap<>(databaseNames.size(), 1F);
        for (String each : databaseNames) {
            DataSource dataSource = StorageContainerUtils.generateDataSource(option.getConnectOption().getURL(env.getHost(), env.getPort(databaseType), each),
                    env.getUser(), env.getPassword(), 2);
            result.put(each, dataSource);
        }
        return result;
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
        Map<String, String> result = new HashMap<>(getNetworkAliases().size() + 2, 1F);
        for (String each : getNetworkAliases()) {
            result.put(each + ":" + getExposedPort(), env.getHost() + ":" + env.getPort(databaseType));
        }
        return result;
    }
}
