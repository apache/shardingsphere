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

package org.apache.shardingsphere.infra.database.core.metadata.database.metadata;

import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.column.DialectColumnOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.datatype.DefaultDataTypeOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.join.DialectJoinOrderOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.table.DefaultSystemTableOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.table.DialectSystemTableOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.table.TableNamePatternType;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

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
     * Get schema option.
     *
     * @return schema option
     */
    default DialectSchemaOption getSchemaOption() {
        return new DefaultSchemaOption(false, null);
    }
    
    /**
     * Get system table option.
     *
     * @return system table option
     */
    default DialectSystemTableOption getSystemTableOption() {
        return new DefaultSystemTableOption();
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
     * Get table name pattern type.
     *
     * @return table name pattern type
     */
    default TableNamePatternType getTableNamePatternType() {
        return TableNamePatternType.KEEP_ORIGIN;
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
        return new DialectTransactionOption(false, false, false, false, true);
    }
    
    /**
     * Get join order option.
     *
     * @return join order option
     */
    default DialectJoinOrderOption getJoinOrderOption() {
        return new DialectJoinOrderOption(false, false);
    }
}
