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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.framework.container.atomic.AtomicContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap.Builder;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Storage container.
 */
public abstract class StorageContainer extends AtomicContainer {
    
    private Map<String, DataSource> dataSourceMap;
    
    @Getter
    private final DatabaseType databaseType;
    
    @Getter
    private final String scenario;
    
    public StorageContainer(final DatabaseType databaseType, final String dockerImageName, final boolean isFakedContainer, final String scenario) {
        super(databaseType.getName().toLowerCase(), dockerImageName, isFakedContainer);
        this.databaseType = databaseType;
        this.scenario = scenario;
    }
    
    @Override
    protected void configure() {
        withClasspathResourceMapping(EnvironmentPath.getInitSQLResourcePath(databaseType, scenario), "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
    }
    
    /**
     * Get actual data source map.
     *
     * @return actual data source map
     */
    @SneakyThrows({IOException.class, JAXBException.class})
    public synchronized Map<String, DataSource> getActualDataSourceMap() {
        if (null == dataSourceMap) {
            Collection<String> dataSourceNames = DatabaseEnvironmentManager.getDatabaseNames(scenario);
            Builder<String, DataSource> builder = ImmutableMap.builder();
            dataSourceNames.forEach(each -> builder.put(each, createDataSource(each)));
            dataSourceMap = builder.build();
        }
        return dataSourceMap;
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DataSourceEnvironment.getDriverClassName(databaseType));
        config.setJdbcUrl(DataSourceEnvironment.getURL(databaseType, getHost(), getPort(), dataSourceName));
        config.setUsername(getUsername());
        config.setPassword(getPassword());
        config.setMaximumPoolSize(4);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        getConnectionInitSQL().ifPresent(config::setConnectionInitSql);
        return new HikariDataSource(config);
    }
    
    protected abstract String getUsername();
    
    protected abstract String getPassword();
    
    protected Optional<String> getConnectionInitSQL() {
        return Optional.empty();
    }
    
    protected abstract int getPort();
    
    /**
     * Get primary key column name.
     * 
     * @param dataSource data source
     * @param tableName table name
     * @return primary key column name
     * @throws SQLException SQL exception
     */
    public abstract Optional<String> getPrimaryKeyColumnName(DataSource dataSource, String tableName) throws SQLException;
}
