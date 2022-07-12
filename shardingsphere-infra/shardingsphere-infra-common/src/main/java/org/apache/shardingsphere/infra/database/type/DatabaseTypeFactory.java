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

package org.apache.shardingsphere.infra.database.type;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Database type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeFactory {
    
    private static final String DEFAULT_DATABASE_TYPE = "MySQL";
    
    static {
        ShardingSphereServiceLoader.register(DatabaseType.class);
    }
    
    /**
     * Get instance of database type.
     * 
     * @param name name of database type
     * @return got instance
     */
    public static DatabaseType getInstance(final String name) {
        return TypedSPIRegistry.getRegisteredService(DatabaseType.class, name);
    }
    
    /**
     * Get instances of database type.
     * 
     * @return got instances
     */
    public static Collection<DatabaseType> getInstances() {
        return ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class);
    }
    
    /**
     * Get database type.
     *
     * @param url database URL
     * @return database type
     */
    public static DatabaseType getDatabaseType(final String url) {
        return DatabaseTypeFactory.getInstances().stream().filter(each -> matchURLs(url, each)).findAny().orElseGet(() -> DatabaseTypeFactory.getInstance("SQL92"));
    }
    
    /**
     * Get database type.
     *
     * @param dataSources data sources
     * @return database type
     */
    public static DatabaseType getDatabaseType(final Collection<DataSource> dataSources) {
        DatabaseType result = null;
        for (DataSource each : dataSources) {
            DatabaseType databaseType = getDatabaseType(each);
            Preconditions.checkState(null == result || result == databaseType, "Database type inconsistent with '%s' and '%s'", result, databaseType);
            result = databaseType;
        }
        return null == result ? DatabaseTypeFactory.getInstance(DEFAULT_DATABASE_TYPE) : result;
    }
    
    private static DatabaseType getDatabaseType(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return getDatabaseType(connection.getMetaData().getURL());
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex.getMessage(), ex);
        }
    }
    
    private static boolean matchURLs(final String url, final DatabaseType databaseType) {
        return databaseType.getJdbcUrlPrefixes().stream().anyMatch(url::startsWith);
    }
}
