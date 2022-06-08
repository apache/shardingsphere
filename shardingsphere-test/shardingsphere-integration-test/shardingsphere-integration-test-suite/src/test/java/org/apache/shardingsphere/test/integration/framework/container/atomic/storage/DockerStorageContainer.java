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
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.env.scenario.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.scenario.path.ScenarioDataPath;
import org.apache.shardingsphere.test.integration.env.scenario.path.ScenarioDataPath.Type;
import org.apache.shardingsphere.test.integration.framework.container.atomic.DockerITContainer;
import org.testcontainers.containers.BindMode;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Docker storage container.
 */
@Getter
public abstract class DockerStorageContainer extends DockerITContainer implements StorageContainer {
    
    private final DatabaseType databaseType;
    
    private final String scenario;
    
    private final Map<String, DataSource> actualDataSourceMap;
    
    private final Map<String, DataSource> expectedDataSourceMap;
    
    public DockerStorageContainer(final DatabaseType databaseType, final String dockerImageName, final String scenario) {
        super(databaseType.getType().toLowerCase(), dockerImageName);
        this.databaseType = databaseType;
        this.scenario = scenario;
        actualDataSourceMap = new LinkedHashMap<>();
        expectedDataSourceMap = new LinkedHashMap<>();
    }
    
    @Override
    protected void configure() {
        withClasspathResourceMapping(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.ACTUAL, databaseType), "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
        withClasspathResourceMapping(new ScenarioDataPath(scenario).getInitSQLResourcePath(Type.EXPECTED, databaseType), "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
    }
    
    @Override
    @SneakyThrows({IOException.class, JAXBException.class})
    protected void postStart() {
        DatabaseEnvironmentManager.getDatabaseNames(scenario).forEach(each -> actualDataSourceMap.put(each, createDataSource(each)));
        DatabaseEnvironmentManager.getExpectedDatabaseNames(scenario).forEach(each -> expectedDataSourceMap.put(each, createDataSource(each)));
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(databaseType));
        result.setJdbcUrl(DataSourceEnvironment.getURL(databaseType, getHost(), getMappedPort(getPort()), dataSourceName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(4);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    protected abstract int getPort();
}
