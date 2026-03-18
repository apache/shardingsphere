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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect;

import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Dialect pipeline SQL builder.
 */
@SingletonSPI
public interface DialectPipelineSQLBuilder extends DatabaseTypedSPI {
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return built SQL
     */
    default Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return Optional.empty();
    }
    
    /**
     * Build on duplicate clause of insert SQL.
     *
     * @param dataRecord data record
     * @return built SQL clause
     */
    default Optional<String> buildInsertOnDuplicateClause(final DataRecord dataRecord) {
        return Optional.empty();
    }
    
    /**
     * Build check empty table SQL.
     *
     * @param qualifiedTableName qualified table name
     * @return built SQL
     */
    String buildCheckEmptyTableSQL(String qualifiedTableName);
    
    /**
     * Build estimated count SQL.
     *
     * @param catalogName catalog name
     * @param qualifiedTableName qualified table name
     * @return built SQL
     */
    default Optional<String> buildEstimatedCountSQL(final String catalogName, final String qualifiedTableName) {
        return Optional.empty();
    }
    
    /**
     * Build CRC32 SQL.
     *
     * @param qualifiedTableName qualified table name
     * @param columnName column name
     * @return built SQL
     */
    default Optional<String> buildCRC32SQL(final String qualifiedTableName, final String columnName) {
        return Optional.empty();
    }
    
    /**
     * Build split by unique key subquery clause.
     *
     * @param qualifiedTableName qualified table name
     * @param uniqueKey unique key
     * @param hasLowerBound has lower bound
     * @return built SQL
     */
    String buildSplitByUniqueKeyRangedSubqueryClause(String qualifiedTableName, String uniqueKey, boolean hasLowerBound);
    
    /**
     * Build create table SQLs.
     *
     * @param dataSource dataSource
     * @param schemaName schema name
     * @param tableName table name
     * @return built SQLs
     * @throws SQLException SQL exception
     */
    Collection<String> buildCreateTableSQLs(DataSource dataSource, String schemaName, String tableName) throws SQLException;
    
    /**
     * Build query current position SQL.
     *
     * @return built SQL
     */
    default Optional<String> buildQueryCurrentPositionSQL() {
        return Optional.empty();
    }
    
    /**
     * Wrap with page query.
     *
     * @param sql SQL
     * @return wrapped SQL
     */
    String wrapWithPageQuery(String sql);
}
