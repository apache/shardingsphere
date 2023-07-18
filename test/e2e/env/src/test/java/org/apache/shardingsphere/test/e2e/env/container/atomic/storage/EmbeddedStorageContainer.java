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

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.container.atomic.EmbeddedITContainer;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.database.DatabaseEnvironmentManager;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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
        actualDataSourceMap = createActualDataSourceMap();
        expectedDataSourceMap = createExpectedDataSourceMap();
    }
    
    @SneakyThrows({IOException.class, JAXBException.class})
    private Map<String, DataSource> createActualDataSourceMap() {
        Collection<String> databaseNames = DatabaseEnvironmentManager.getDatabaseNames(scenario);
        Map<String, DataSource> result = new LinkedHashMap<>(databaseNames.size(), 1F);
        databaseNames.forEach(each -> result.put(each, StorageContainerUtils.generateDataSource(DataSourceEnvironment.getURL(databaseType, null, 0, scenario + each),
                "root", "Root@123")));
        return result;
    }
    
    @SneakyThrows({IOException.class, JAXBException.class})
    private Map<String, DataSource> createExpectedDataSourceMap() {
        Collection<String> databaseNames = DatabaseEnvironmentManager.getExpectedDatabaseNames(scenario);
        Map<String, DataSource> result = new LinkedHashMap<>(databaseNames.size(), 1F);
        databaseNames.forEach(each -> result.put(each, StorageContainerUtils.generateDataSource(DataSourceEnvironment.getURL(databaseType, null, 0, scenario + each),
                "root", "Root@123")));
        return result;
    }
    
    @Override
    public final String getAbbreviation() {
        return databaseType.getType().toLowerCase();
    }
}
