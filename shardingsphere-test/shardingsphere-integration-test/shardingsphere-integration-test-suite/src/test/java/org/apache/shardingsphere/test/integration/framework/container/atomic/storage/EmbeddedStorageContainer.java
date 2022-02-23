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

package org.apache.shardingsphere.test.integration.framework.container.atomic.storage;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.env.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.framework.container.atomic.EmbeddedITContainer;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Embedded storage container.
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public abstract class EmbeddedStorageContainer implements EmbeddedITContainer, StorageContainer {
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private Map<String, DataSource> actualDataSourceMap;
    
    private DataSource verificationDataSource;
    
    @Override
    @SneakyThrows({IOException.class, JAXBException.class})
    public Map<String, DataSource> getActualDataSourceMap() {
        if (null != actualDataSourceMap) {
            return actualDataSourceMap;
        }
        synchronized (this) {
            if (null != actualDataSourceMap) {
                return actualDataSourceMap;
            } 
            Collection<String> dataSourceNames = DatabaseEnvironmentManager.getDatabaseNames(scenario);
            actualDataSourceMap = new LinkedHashMap<>(dataSourceNames.size(), 1);
            dataSourceNames.forEach(each -> actualDataSourceMap.put(each, createDataSource(each)));
            return actualDataSourceMap;
        }
    }
    
    @Override
    public final DataSource getVerificationDataSource() {
        if (null != verificationDataSource) {
            return verificationDataSource;
        }
        synchronized (this) {
            if (null != verificationDataSource) {
                return verificationDataSource;
            }
            verificationDataSource = createDataSource(scenario + "_verification_dataset");
            return verificationDataSource;
        }
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(databaseType));
        result.setJdbcUrl(DataSourceEnvironment.getURL(databaseType, null, 0, dataSourceName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(4);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
}
