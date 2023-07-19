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

package org.apache.shardingsphere.infra.database.spi;

import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Database type.
 */
@SingletonSPI
public interface DatabaseType extends TypedSPI {
    
    /**
     * Get quote character.
     *
     * @return quote character
     */
    QuoteCharacter getQuoteCharacter();
    
    /**
     * Judge whether identifier is reserved word.
     *
     * @param identifier identifier to be judged
     * @return is reserved word or not
     */
    default boolean isReservedWord(String identifier) {
        return false;
    }
    
    /**
     * Get alias of JDBC URL prefixes.
     * 
     * @return Alias of JDBC URL prefixes
     */
    Collection<String> getJdbcUrlPrefixes();
    
    /**
     * Get data source meta data.
     * 
     * @param url URL of data source
     * @param username username of data source
     * @return data source meta data
     */
    DataSourceMetaData getDataSourceMetaData(String url, String username);
    
    /**
     * Get data source meta data.
     *
     * @param url URL of data source
     * @param username username of data source
     * @param catalog catalog of data source
     * @return data source meta data
     */
    default DataSourceMetaData getDataSourceMetaData(String url, String username, String catalog) {
        return getDataSourceMetaData(url, username);
    }
    
    /**
     * Get system database schema map.
     * 
     * @return system database schema map
     */
    Map<String, Collection<String>> getSystemDatabaseSchemaMap();
    
    /**
     * Get system schemas.
     *
     * @return system schemas
     */
    Collection<String> getSystemSchemas();
    
    /**
     * Is schema feature available.
     *
     * @return true or false
     */
    default boolean isSchemaAvailable() {
        return false;
    }
    
    /**
     * Get schema.
     *
     * @param connection connection
     * @return schema
     */
    @SuppressWarnings("ReturnOfNull")
    default String getSchema(final Connection connection) {
        try {
            return connection.getSchema();
        } catch (final SQLException ignored) {
            return null;
        }
    }
    
    /**
     * Get default schema name.
     *
     * @return default schema name
     */
    default Optional<String> getDefaultSchema() {
        return Optional.empty();
    }
    
    /**
     * Format table name pattern.
     *
     * @param tableNamePattern table name pattern
     * @return formatted table name pattern
     */
    default String formatTableNamePattern(final String tableNamePattern) {
        return tableNamePattern;
    }
    
    /**
     * Handle rollback only.
     *
     * @param rollbackOnly rollback only
     * @param statement statement
     * @throws SQLException SQL exception
     */
    default void handleRollbackOnly(final boolean rollbackOnly, final SQLStatement statement) throws SQLException {
    }
    
    /**
     * Is instance connection available.
     *
     * @return true or false
     */
    default boolean isInstanceConnectionAvailable() {
        return false;
    }
    
    @Override
    String getType();
}
