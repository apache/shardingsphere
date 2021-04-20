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

package org.apache.shardingsphere.test.integration.junit.container.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.database.DatabaseEnvironmentManager;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.testcontainers.containers.BindMode;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap.Builder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The storage container was binding to the single scenario and database type.
 */
public abstract class ShardingSphereStorageContainer extends ShardingSphereContainer {
    
    @Getter
    private final Collection<String> databases;
    
    private ImmutableMap<String, DataSource> dataSourceMap;
    
    @Getter
    private final DatabaseType databaseType;
    
    @SneakyThrows
    public ShardingSphereStorageContainer(final String dockerName, final String dockerImageName, final DatabaseType databaseType,
                                          final boolean isFakeContainer, final ParameterizedArray parameterizedArray) {
        super(dockerName, dockerImageName, isFakeContainer, parameterizedArray);
        this.databaseType = databaseType;
        this.databases = DatabaseEnvironmentManager.getDatabaseNames(getParameterizedArray().getScenario());
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
    
    protected DataSource createDataSource(final String dataSourceName) {
        HikariConfig config = new HikariConfig();
        config.setUsername(getUsername());
        config.setPassword(getPassword());
        config.setDriverClassName(getDriverClassName());
        config.setJdbcUrl(getUrl(dataSourceName));
        config.setMaximumPoolSize(2);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        getConnectionInitSQL().ifPresent(config::setConnectionInitSql);
        return new HikariDataSource(config);
    }
    
    /**
     * Get DataSource Map.
     *
     * @return DatabaseName and DataSource Map
     */
    public synchronized Map<String, DataSource> getDataSourceMap() {
        if (Objects.isNull(dataSourceMap)) {
            Builder<String, DataSource> builder = ImmutableMap.builder();
            databases.forEach(e -> builder.put(e, createDataSource(e)));
            dataSourceMap = builder.build();
        }
        return dataSourceMap;
    }
    
    protected Optional<String> getConnectionInitSQL() {
        return Optional.empty();
    }
    
    protected String getDriverClassName() {
        switch (databaseType.getName()) {
            case "H2":
                return "org.h2.Driver";
            case "MySQL":
                return "com.mysql.jdbc.Driver";
            case "PostgreSQL":
                return "org.postgresql.Driver";
            case "SQLServer":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "Oracle":
                return "oracle.jdbc.driver.OracleDriver";
            default:
                throw new UnsupportedOperationException(databaseType.getName());
        }
    }
    
    protected abstract String getUrl(String dataSourceName);
    
    protected abstract int getPort();
    
    protected abstract String getUsername();
    
    protected abstract String getPassword();
}
