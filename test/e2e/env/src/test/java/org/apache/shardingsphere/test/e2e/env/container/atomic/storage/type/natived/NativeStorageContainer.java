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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.mount.MountSQLResourceGenerator;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.option.StorageContainerConfigurationOption;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.SQLScriptUtils;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.datasource.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Native storage container.
 */
public final class NativeStorageContainer implements StorageContainer {
    
    private final E2ETestEnvironment env;
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final StorageContainerConfigurationOption option;
    
    @Getter
    private final Map<String, DataSource> actualDataSourceMap;
    
    @Getter
    private final Map<String, DataSource> expectedDataSourceMap;
    
    @Getter
    @Setter
    private List<String> networkAliases;
    
    public NativeStorageContainer(final DatabaseType databaseType, final String scenario) {
        env = E2ETestEnvironment.getInstance();
        this.databaseType = databaseType;
        this.scenario = scenario;
        option = DatabaseTypedSPILoader.findService(StorageContainerConfigurationOption.class, databaseType).orElse(null);
        initDatabase();
        actualDataSourceMap = createDataSourceMap(Type.ACTUAL);
        expectedDataSourceMap = createDataSourceMap(Type.EXPECTED);
    }
    
    private void initDatabase() {
        if (null != option) {
            DataSourceEnvironment dataSourceEnvironment = DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, databaseType);
            DataSource dataSource = StorageContainerUtils.generateDataSource(
                    dataSourceEnvironment.getURL(env.getNativeStorageHost(), env.getNativeStoragePort()), env.getNativeStorageUsername(), env.getNativeStoragePassword());
            new MountSQLResourceGenerator(option).generate(0, scenario).keySet().forEach(each -> SQLScriptUtils.execute(dataSource, each));
        }
    }
    
    private Map<String, DataSource> createDataSourceMap(final Type type) {
        return null == scenario
                ? Collections.emptyMap()
                : getDataSourceMap(DatabaseEnvironmentManager.getDatabaseTypes(scenario, databaseType, type).entrySet().stream()
                        .filter(entry -> entry.getValue() == databaseType).map(Entry::getKey).collect(Collectors.toList()));
    }
    
    private Map<String, DataSource> getDataSourceMap(final Collection<String> databaseNames) {
        Map<String, DataSource> result = new HashMap<>(databaseNames.size(), 1F);
        DataSourceEnvironment dataSourceEnvironment = DatabaseTypedSPILoader.getService(DataSourceEnvironment.class, databaseType);
        for (String each : databaseNames) {
            DataSource dataSource = StorageContainerUtils.generateDataSource(
                    dataSourceEnvironment.getURL(env.getNativeStorageHost(), env.getNativeStoragePort(), each), env.getNativeStorageUsername(), env.getNativeStoragePassword());
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
        return null == option ? 0 : option.getPort();
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
            result.put(each + ":" + getExposedPort(), env.getNativeStorageHost() + ":" + env.getNativeStoragePort());
        }
        result.put(StorageContainerConstants.OPERATION_USER, env.getNativeStorageUsername());
        result.put(StorageContainerConstants.OPERATION_PASSWORD, env.getNativeStoragePassword());
        return result;
    }
}
