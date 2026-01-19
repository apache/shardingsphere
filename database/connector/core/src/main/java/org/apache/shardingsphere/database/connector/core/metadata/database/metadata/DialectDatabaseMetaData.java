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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.altertable.DialectAlterTableOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.column.DialectColumnOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DefaultDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sqlbatch.DialectSQLBatchOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.version.DialectProtocolVersionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.sql.Connection;
import java.util.Collections;
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
     * Get identifier pattern type.
     *
     * @return identifier pattern type
     */
    IdentifierPatternType getIdentifierPatternType();
    
    /**
     * Whether identifier is case-sensitive.
     *
     * @return is case-sensitive or insensitive
     */
    default boolean isCaseSensitive() {
        return false;
    }
    
    /**
     * Get default nulls order type.
     *
     * @return default nulls order type
     */
    // TODO Reuse java.sql.DatabaseMetaData.nullsAreSortedHigh and java.sql.DatabaseMetaData.nullsAreSortedLow
    NullsOrderType getDefaultNullsOrderType();
    
    /**
     * Get data type option.
     *
     * @return data type option
     */
    default DialectDataTypeOption getDataTypeOption() {
        return new DefaultDataTypeOption();
    }
    
    /**
     * Get driver query system catalog option.
     *
     * @return driver query system catalog option
     */
    default Optional<DialectDriverQuerySystemCatalogOption> getDriverQuerySystemCatalogOption() {
        return Optional.empty();
    }
    
    /**
     * Get schema option.
     *
     * @return schema option
     */
    default DialectSchemaOption getSchemaOption() {
        return new DefaultSchemaOption(false, null);
    }
    
    /**
     * Get column option.
     *
     * @return column option
     */
    default DialectColumnOption getColumnOption() {
        return new DialectColumnOption(true);
    }
    
    /**
     * Get index option.
     *
     * @return index option
     */
    default DialectIndexOption getIndexOption() {
        return new DialectIndexOption(false);
    }
    
    /**
     * Get connection option.
     *
     * @return connection option
     */
    default DialectConnectionOption getConnectionOption() {
        return new DialectConnectionOption(false, false);
    }
    
    /**
     * Get transaction option.
     *
     * @return transaction option
     */
    default DialectTransactionOption getTransactionOption() {
        return new DialectTransactionOption(false, false, false, false, true, Connection.TRANSACTION_READ_COMMITTED, false, false, Collections.emptyList());
    }
    
    /**
     * Get join option.
     *
     * @return join option
     */
    default DialectJoinOption getJoinOption() {
        return new DialectJoinOption(false, false);
    }
    
    /**
     * Get pagination option.
     *
     * @return pagination option
     */
    default DialectPaginationOption getPaginationOption() {
        return new DialectPaginationOption(false, "", false);
    }
    
    /**
     * Get generated key option.
     *
     * @return generated key option
     */
    default Optional<DialectGeneratedKeyOption> getGeneratedKeyOption() {
        return Optional.empty();
    }
    
    /**
     * Get alter table option.
     *
     * @return alter table option
     */
    default Optional<DialectAlterTableOption> getAlterTableOption() {
        return Optional.empty();
    }
    
    /**
     * Get SQL batch option.
     *
     * @return SQL batch option
     */
    default DialectSQLBatchOption getSQLBatchOption() {
        return new DialectSQLBatchOption(true);
    }
    
    /**
     * Get protocol version option.
     *
     * @return protocol version option
     */
    default DialectProtocolVersionOption getProtocolVersionOption() {
        return new DialectProtocolVersionOption("");
    }
}
