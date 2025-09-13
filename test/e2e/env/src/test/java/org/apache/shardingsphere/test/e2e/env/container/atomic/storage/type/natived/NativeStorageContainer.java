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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.natived;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.mount.MountSQLResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.StorageContainerConfigurationOptionFactory;
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
public final class NativeStorageContainer implements StorageContainer {
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final StorageContainerConfiguration storageContainerConfig;
    
    @Getter
    private final Map<String, DataSource> actualDataSourceMap;
    
    @Getter
    private final Map<String, DataSource> expectedDataSourceMap;
    
    @Getter
    @Setter
    private List<String> networkAliases;
    
    public NativeStorageContainer(final DatabaseType databaseType, final String scenario) {
        this.databaseType = databaseType;
        this.scenario = scenario;
        storageContainerConfig = StorageContainerConfigurationFactory.newInstance(StorageContainerConfigurationOptionFactory.newInstance(databaseType), databaseType, scenario);
        initDatabase();
        actualDataSourceMap = createActualDataSourceMap();
        expectedDataSourceMap = createExpectedDataSourceMap();
    }
    
    private void initDatabase() {
        DataSource dataSource = StorageContainerUtils.generateDataSource(
                DataSourceEnvironment.getURL(databaseType, E2ETestEnvironment.getInstance().getNativeStorageHost(), Integer.parseInt(E2ETestEnvironment.getInstance().getNativeStoragePort())),
                E2ETestEnvironment.getInstance().getNativeStorageUsername(), E2ETestEnvironment.getInstance().getNativeStoragePassword());
        new MountSQLResourceGenerator(storageContainerConfig.getConfigurationOption(), databaseType).generate(0, scenario).keySet().forEach(each -> SQLScriptUtils.execute(dataSource, each));
    }
    
    private Map<String, DataSource> createActualDataSourceMap() {
        Collection<String> databaseNames =
                storageContainerConfig.getActualDatabaseTypes().entrySet().stream().filter(entry -> entry.getValue() == databaseType).map(Entry::getKey).collect(Collectors.toList());
        return getDataSourceMap(databaseNames);
    }
    
    private Map<String, DataSource> createExpectedDataSourceMap() {
        Collection<String> databaseNames =
                storageContainerConfig.getExpectedDatabaseTypes().entrySet().stream().filter(entry -> entry.getValue() == databaseType).map(Entry::getKey).collect(Collectors.toList());
        return getDataSourceMap(databaseNames);
    }
    
    private Map<String, DataSource> getDataSourceMap(final Collection<String> databaseNames) {
        Map<String, DataSource> result = new HashMap<>(databaseNames.size(), 1F);
        for (String each : databaseNames) {
            DataSource dataSource = StorageContainerUtils.generateDataSource(DataSourceEnvironment.getURL(databaseType, E2ETestEnvironment.getInstance().getNativeStorageHost(),
                    Integer.parseInt(E2ETestEnvironment.getInstance().getNativeStoragePort()), each),
                    E2ETestEnvironment.getInstance().getNativeStorageUsername(), E2ETestEnvironment.getInstance().getNativeStoragePassword());
            result.put(each, dataSource);
        }
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
        }
        if ("PostgreSQL".equalsIgnoreCase(databaseType.getType())) {
            return 5432;
        }
        if ("openGauss".equalsIgnoreCase(databaseType.getType())) {
            return 5432;
        }
        if ("Hive".equalsIgnoreCase(databaseType.getType())) {
            return 10000;
        }
        throw new UnsupportedOperationException(String.format("Unsupported database type: %s.", databaseType.getType()));
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
            result.put(each + ":" + getExposedPort(), E2ETestEnvironment.getInstance().getNativeStorageHost() + ":" + E2ETestEnvironment.getInstance().getNativeStoragePort());
        }
        result.put(StorageContainerConstants.OPERATION_USER, E2ETestEnvironment.getInstance().getNativeStorageUsername());
        result.put(StorageContainerConstants.OPERATION_PASSWORD, E2ETestEnvironment.getInstance().getNativeStoragePassword());
        return result;
    }
}
