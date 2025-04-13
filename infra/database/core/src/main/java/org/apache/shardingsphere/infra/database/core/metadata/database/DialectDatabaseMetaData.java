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

package org.apache.shardingsphere.infra.database.core.metadata.database;

import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.metadata.database.option.JoinOrderOption;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Dialect database meta data.
 */
@SingletonSPI
public interface DialectDatabaseMetaData extends DatabaseTypedSPI {
    
    /**
     * Get quote character.
     *
     * @return quote character
     */
    QuoteCharacter getQuoteCharacter();
    
    /**
     * Get extra data types.
     *
     * @return extra data type map
     */
    default Map<String, Integer> getExtraDataTypes() {
        return Collections.emptyMap();
    }
    
    /**
     * Find extra SQL type class.
     *
     * @param dataType data type
     * @param unsigned whether unsigned
     * @return extra SQL type class
     */
    default Optional<Class<?>> findExtraSQLTypeClass(final int dataType, final boolean unsigned) {
        return Optional.empty();
    }
    
    /**
     * Get default nulls order type.
     *
     * @return default nulls order type
     */
    // TODO Reuse java.sql.DatabaseMetaData.nullsAreSortedHigh and java.sql.DatabaseMetaData.nullsAreSortedLow
    NullsOrderType getDefaultNullsOrderType();
    
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
     * Is instance connection available.
     *
     * @return available or not
     */
    default boolean isInstanceConnectionAvailable() {
        return false;
    }
    
    /**
     * Is support three tier storage structure.
     *
     * @return support or not
     */
    default boolean isSupportThreeTierStorageStructure() {
        return false;
    }
    
    /**
     * Is support global CSN.
     *
     * @return support or not
     */
    default boolean isSupportGlobalCSN() {
        return false;
    }
    
    /**
     * Whether DDL need implicit commit.
     *
     * @return need or not
     */
    default boolean isDDLNeedImplicitCommit() {
        return false;
    }
    
    /**
     * Whether support auto commit when nested transaction.
     *
     * @return support or not
     */
    default boolean isSupportAutoCommitInNestedTransaction() {
        return false;
    }
    
    /**
     * Whether support DDL in XA transaction.
     *
     * @return support or not
     */
    default boolean isSupportDDLInXATransaction() {
        return false;
    }
    
    /**
     * Whether support meta data refresh in transaction.
     *
     * @return support or not
     */
    // TODO Investgate the reason of some databases cannot support meta data refreshed in transaction. The method should be removed finally after metadata refresh supported for all database.
    default boolean isSupportMetaDataRefreshInTransaction() {
        return true;
    }
    
    /**
     * Get join order option.
     *
     * @return join order option
     */
    default JoinOrderOption getJoinOrderOption() {
        return new JoinOrderOption(false, false);
    }
}
