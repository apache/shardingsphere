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

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.SQLScriptUtils;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Native storage container.
 */
@Getter
public final class NativeStorageContainer implements StorageContainer {
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final Map<String, DataSource> actualDataSourceMap;
    
    private final Map<String, DataSource> expectedDataSourceMap;
    
    private final StorageContainerConfiguration storageContainerConfiguration;
    
    @Setter
    private List<String> networkAliases;
    
    public NativeStorageContainer(final DatabaseType databaseType, final String scenario) {
        this.databaseType = databaseType;
        this.scenario = scenario;
        storageContainerConfiguration = StorageContainerConfigurationFactory.newInstance(this.databaseType, this.scenario);
        initDatabase();
        actualDataSourceMap = createActualDataSourceMap();
        expectedDataSourceMap = createExpectedDataSourceMap();
    }
    
    private void initDatabase() {
        DataSource dataSource = StorageContainerUtils.generateDataSource(
                DataSourceEnvironment.getURL(databaseType, E2ETestEnvironment.getInstance().getNativeStorageHost(), Integer.parseInt(E2ETestEnvironment.getInstance().getNativeStoragePort())),
                E2ETestEnvironment.getInstance().getNativeStorageUsername(), E2ETestEnvironment.getInstance().getNativeStoragePassword());
        storageContainerConfiguration.getMountedResources().keySet().stream().filter(each -> each.toLowerCase().endsWith(".sql")).forEach(each -> SQLScriptUtils.execute(dataSource, each));
    }
    
    private Map<String, DataSource> createActualDataSourceMap() {
        Collection<String> databaseNames =
                storageContainerConfiguration.getDatabaseTypes().entrySet().stream().filter(entry -> entry.getValue() == databaseType).map(Entry::getKey).collect(Collectors.toList());
        return getDataSourceMap(databaseNames);
    }
    
    private Map<String, DataSource> createExpectedDataSourceMap() {
        Collection<String> databaseNames =
                storageContainerConfiguration.getExpectedDatabaseTypes().entrySet().stream().filter(entry -> entry.getValue() == databaseType).map(Entry::getKey).collect(Collectors.toList());
        return getDataSourceMap(databaseNames);
    }
    
    private Map<String, DataSource> getDataSourceMap(final Collection<String> databaseNames) {
        Map<String, DataSource> result = new HashMap<>();
        for (String each : databaseNames) {
            DataSource dataSource = StorageContainerUtils.generateDataSource(DataSourceEnvironment.getURL(databaseType, E2ETestEnvironment.getInstance().getNativeStorageHost(),
                    Integer.parseInt(E2ETestEnvironment.getInstance().getNativeStoragePort()), each),
                    E2ETestEnvironment.getInstance().getNativeStorageUsername(), E2ETestEnvironment.getInstance().getNativeStoragePassword());
            result.put(each, dataSource);
        }
        return result;
    }
    
    @Override
    public String getAbbreviation() {
        return databaseType.getType().toLowerCase();
    }
    
    @Override
    public Map<String, String> getLinkReplacements() {
        Map<String, String> result = new HashMap<>();
        for (String each : getNetworkAliases()) {
            result.put(each + ":" + getExposedPort(), E2ETestEnvironment.getInstance().getNativeStorageHost() + ":" + E2ETestEnvironment.getInstance().getNativeStoragePort());
        }
        result.put(StorageContainerConstants.USERNAME, E2ETestEnvironment.getInstance().getNativeStorageUsername());
        result.put(StorageContainerConstants.PASSWORD, E2ETestEnvironment.getInstance().getNativeStoragePassword());
        return result;
    }
    
    /**
     * Get exposed port.
     *
     * @return exposed port
     * @throws UnsupportedOperationException unsupported operation exception
     */
    public int getExposedPort() {
        if ("MySQL".equalsIgnoreCase(databaseType.getType())) {
            return 3306;
        } else if ("PostgreSQL".equalsIgnoreCase(databaseType.getType())) {
            return 5432;
        } else if ("openGauss".equalsIgnoreCase(databaseType.getType())) {
            return 5432;
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported database type: %s.", databaseType.getType()));
        }
    }
    
    @Override
    public void start() {
    }
}
