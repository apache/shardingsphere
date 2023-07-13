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

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.infra.spi.DatabaseTypedSPI;

import java.util.List;
import java.util.Optional;

/**
 * Dialect pipeline SQL builder.
 */
public interface DialectPipelineSQLBuilder extends DatabaseTypedSPI {
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return create schema SQL
     */
    default Optional<String> buildCreateSchemaSQL(String schemaName) {
        return Optional.empty();
    }
    
    /**
     * Build insert SQL on duplicate part.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @return insert SQL on duplicate part
     */
    default Optional<String> buildInsertSQLOnDuplicatePart(String schemaName, DataRecord dataRecord) {
        return Optional.empty();
    }
    
    /**
     * Extract updated columns.
     *
     * @param dataRecord data record
     * @return filtered columns
     */
    List<Column> extractUpdatedColumns(DataRecord dataRecord);
    
    /**
     * Build estimated count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return estimated count SQL
     */
    Optional<String> buildEstimatedCountSQL(String schemaName, String tableName);
    
    /**
     * Build CRC32 SQL.
     *
     * @param schemaName schema name
     * @param tableName table Name
     * @param column column
     * @return CRC32 SQL
     */
    default Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String column) {
        return Optional.empty();
    }
    
    /**
     * Judge whether keyword.
     * 
     * @param item item to be judged
     * @return is keyword or not
     */
    boolean isKeyword(String item);
}
