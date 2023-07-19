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

package org.apache.shardingsphere.data.pipeline.spi.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;

import java.util.Optional;

/**
 * Dialect pipeline SQL builder.
 */
public interface DialectPipelineSQLBuilder extends DatabaseTypedSPI {
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return built SQL
     */
    default Optional<String> buildCreateSchemaSQL(String schemaName) {
        return Optional.empty();
    }
    
    /**
     * Build on duplicate clause of insert SQL.
     *
     * @param dataRecord data record
     * @return built SQL clause
     */
    default Optional<String> buildInsertOnDuplicateClause(DataRecord dataRecord) {
        return Optional.empty();
    }
    
    /**
     * Build check empty SQL.
     *
     * @param qualifiedTableName qualified table name
     * @return built SQL
     */
    String buildCheckEmptySQL(String qualifiedTableName);
    
    /**
     * Build estimated count SQL.
     *
     * @param qualifiedTableName qualified table name
     * @return built SQL
     */
    default Optional<String> buildEstimatedCountSQL(String qualifiedTableName) {
        return Optional.empty();
    }
    
    /**
     * Build CRC32 SQL.
     *
     * @param qualifiedTableName qualified table name
     * @param columnName column name
     * @return built SQL
     */
    default Optional<String> buildCRC32SQL(String qualifiedTableName, final String columnName) {
        return Optional.empty();
    }
}
