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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler.DatabaseMetaDataDialectHandler;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

/**
 * Database meta data dialect handler facade.
 */
public final class DatabaseMetaDataDialectHandlerFacade {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseMetaDataDialectHandler.class);
    }
    
    /**
     * Get schema.
     *
     * @param connection connection
     * @param databaseType database type
     * @return schema
     */
    public static String getSchema(final Connection connection, final DatabaseType databaseType) {
        return findDatabaseSpecialHandler(databaseType).map(handler -> handler.getSchema(connection)).orElse(getSchema(connection));
    }
    
    private static String getSchema(final Connection connection) {
        try {
            return connection.getSchema();
        } catch (final SQLException ex) {
            return null;
        }
    }
    
    /**
     * Get table name pattern.
     *
     * @param tableNamePattern table name pattern
     * @param databaseType database type
     * @return table name pattern
     */
    public static String getTableNamePattern(final String tableNamePattern, final DatabaseType databaseType) {
        return findDatabaseSpecialHandler(databaseType).map(handler -> handler.decorate(tableNamePattern)).orElse(tableNamePattern);
    }
    
    private static Optional<DatabaseMetaDataDialectHandler> findDatabaseSpecialHandler(final DatabaseType databaseType) {
        try {
            return Optional.of(TypedSPIRegistry.getRegisteredService(DatabaseMetaDataDialectHandler.class, databaseType.getName(), new Properties()));
        } catch (final ServiceProviderNotFoundException ignored) {
            return Optional.empty();
        }
    }
}
