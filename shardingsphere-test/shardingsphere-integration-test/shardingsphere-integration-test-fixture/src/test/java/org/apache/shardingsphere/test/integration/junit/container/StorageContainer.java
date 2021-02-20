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

package org.apache.shardingsphere.test.integration.junit.container;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.junit.annotation.XmlResource;
import org.apache.shardingsphere.test.integration.junit.processor.DatabaseProcessor;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap.Builder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * The storage container was binding to the single scenario and database type.
 */
public abstract class StorageContainer extends ShardingContainer {
    
    @Getter
    @XmlResource(file = "/env/{it.scenario}/databases.xml", processor = DatabaseProcessor.class)
    private Collection<String> databases;
    
    private ImmutableMap<String, DataSource> dataSourceMap;
    
    @Getter
    private final DatabaseType databaseType;
    
    public StorageContainer(final String dockerImageName, final DatabaseType databaseType) {
        super(dockerImageName);
        this.databaseType = databaseType;
    }
    
    protected abstract String getUrl(String dataSourceName);
    
    protected abstract int getPort();
    
    protected abstract String getUsername();
    
    protected abstract String getPassword();
    
    protected abstract DataSource createDataSource(String dataSourceName);
    
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
    
    protected DataSource createHikariCP(final Properties properties) {
        HikariConfig result = new HikariConfig(properties);
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        getConnectionInitSQL().ifPresent(result::setConnectionInitSql);
        return new HikariDataSource(result);
    }
    
    protected Optional<String> getConnectionInitSQL() {
        return Optional.empty();
    }
    
}
