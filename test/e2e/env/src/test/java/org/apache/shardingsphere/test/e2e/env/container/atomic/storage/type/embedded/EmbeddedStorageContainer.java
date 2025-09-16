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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.type.embedded;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.EmbeddedITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioDataPath.Type;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Embedded storage container.
 */
@Getter
public abstract class EmbeddedStorageContainer implements EmbeddedITContainer, StorageContainer {
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final Map<String, DataSource> actualDataSourceMap;
    
    private final Map<String, DataSource> expectedDataSourceMap;
    
    protected EmbeddedStorageContainer(final DatabaseType databaseType, final String scenario) {
        this.databaseType = databaseType;
        this.scenario = scenario;
        actualDataSourceMap = createDataSourceMap(Type.ACTUAL);
        expectedDataSourceMap = createDataSourceMap(Type.EXPECTED);
    }
    
    private Map<String, DataSource> createDataSourceMap(final Type type) {
        E2ETestEnvironment env = E2ETestEnvironment.getInstance();
        Collection<String> databaseNames = DatabaseEnvironmentManager.getDatabaseTypes(scenario, databaseType, type).entrySet().stream()
                .filter(entry -> entry.getValue().getClass().isAssignableFrom(databaseType.getClass())).map(Entry::getKey).collect(Collectors.toList());
        Map<String, DataSource> result = new LinkedHashMap<>(databaseNames.size(), 1F);
        databaseNames.forEach(each -> result.put(each, StorageContainerUtils.generateDataSource(
                DataSourceEnvironment.getURL(databaseType, env.getNativeStorageHost(), env.getNativeStoragePort(), each), env.getNativeStorageUsername(), env.getNativeStoragePassword())));
        return result;
    }
    
    @Override
    public final String getAbbreviation() {
        return databaseType.getType().toLowerCase();
    }
    
    @Override
    public Map<String, String> getLinkReplacements() {
        return Collections.emptyMap();
    }
}
