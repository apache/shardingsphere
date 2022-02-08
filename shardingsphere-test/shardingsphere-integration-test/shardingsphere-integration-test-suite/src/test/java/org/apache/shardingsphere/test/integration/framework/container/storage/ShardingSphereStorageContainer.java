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

package org.apache.shardingsphere.test.integration.framework.container.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.framework.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;
import org.testcontainers.containers.BindMode;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap.Builder;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The storage container was binding to the single scenario and database type.
 */
public abstract class ShardingSphereStorageContainer extends ShardingSphereContainer {
    
    @Getter
    private final DatabaseType databaseType;
    
    private Map<String, DataSource> dataSourceMap;
    
    public ShardingSphereStorageContainer(final String name, final String dockerImageName, 
                                          final DatabaseType databaseType, final boolean isFakedContainer, final ParameterizedArray parameterizedArray) {
        super(name, dockerImageName, isFakedContainer, parameterizedArray);
        this.databaseType = databaseType;
    }
    
    /**
     * Mount a source path into container.
     *
     * @param resourcePath resource path
     * @return self
     */
    public ShardingSphereStorageContainer withInitSQLMapping(final String resourcePath) {
        withClasspathResourceMapping(resourcePath, "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
        return this;
    }
    
    /**
     * Get data source map.
     *
     * @return database name and data source map
     */
    @SneakyThrows({IOException.class, JAXBException.class})
    public synchronized Map<String, DataSource> getDataSourceMap() {
        if (null == dataSourceMap) {
            Collection<String> dataSourceNames = DatabaseEnvironmentManager.getDatabaseNames(getParameterizedArray().getScenario());
            Builder<String, DataSource> builder = ImmutableMap.builder();
            dataSourceNames.forEach(each -> builder.put(each, createDataSource(each)));
            dataSourceMap = builder.build();
        }
        return dataSourceMap;
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(getDriverClassName());
        config.setJdbcUrl(getUrl(dataSourceName));
        config.setUsername(getUsername());
        config.setPassword(getPassword());
        config.setMaximumPoolSize(4);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        getConnectionInitSQL().ifPresent(config::setConnectionInitSql);
        return new HikariDataSource(config);
    }
    
    protected Optional<String> getConnectionInitSQL() {
        return Optional.empty();
    }
    
    protected String getDriverClassName() {
        return DataSourceEnvironment.getDriverClassName(databaseType.getName());
    }
    
    protected abstract String getUrl(String dataSourceName);
    
    protected abstract int getPort();
    
    protected abstract String getUsername();
    
    protected abstract String getPassword();
}
