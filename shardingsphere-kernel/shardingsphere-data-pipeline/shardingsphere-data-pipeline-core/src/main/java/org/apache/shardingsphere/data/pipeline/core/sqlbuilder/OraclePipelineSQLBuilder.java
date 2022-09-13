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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder;

import lombok.NonNull;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;

import java.util.Map;
import java.util.Set;

/**
 * Oracle pipeline SQL builder.
 */
public final class OraclePipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    @Override
    public String buildCreateSchemaSQL(final String schemaName) {
        throw new UnsupportedSQLOperationException("buildCreateSchemaSQL");
    }
    
    @Override
    public String getLeftIdentifierQuoteString() {
        return "\"";
    }
    
    @Override
    public String getRightIdentifierQuoteString() {
        return "\"";
    }
    
    @Override
    public String buildInventoryDumpSQL(final String schemaName, final String tableName, final String uniqueKey, final int uniqueKeyDataType, final boolean firstQuery) {
        String decoratedTableName = decorate(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        if (PipelineJdbcUtils.isIntegerColumn(uniqueKeyDataType)) {
            return "SELECT * FROM (SELECT * FROM " + decoratedTableName + " WHERE " + quotedUniqueKey + " " + (firstQuery ? ">=" : ">") + " ?"
                    + " AND " + quotedUniqueKey + " <= ? ORDER BY " + quotedUniqueKey + " ASC) WHERE ROWNUM<=?";
        } else if (PipelineJdbcUtils.isStringColumn(uniqueKeyDataType)) {
            return "SELECT * FROM (SELECT * FROM " + decoratedTableName + " WHERE " + quotedUniqueKey + " " + (firstQuery ? ">=" : ">") + " ?"
                    + " ORDER BY " + quotedUniqueKey + " ASC) WHERE ROWNUM<=?";
        } else {
            throw new IllegalArgumentException("Unknown uniqueKeyDataType: " + uniqueKeyDataType);
        }
    }
    
    @Override
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord, final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        return super.buildInsertSQL(schemaName, dataRecord, shardingColumnsMap);
        // TODO buildInsertSQL and buildConflictSQL, need 2 round parameters set
        // TODO refactor PipelineSQLBuilder to combine SQL building and parameters set
    }
    
    @Override
    public String buildChunkedQuerySQL(final String schemaName, final @NonNull String tableName, final @NonNull String uniqueKey, final boolean firstQuery) {
        if (firstQuery) {
            return "SELECT * FROM (SELECT * FROM " + decorate(schemaName, tableName) + " ORDER BY " + quote(uniqueKey) + " ASC) WHERE ROWNUM<=?";
        } else {
            return "SELECT * FROM (SELECT * FROM " + decorate(schemaName, tableName) + " WHERE " + quote(uniqueKey) + " > ? ORDER BY " + quote(uniqueKey) + " ASC) WHERE ROWNUM<=?";
        }
    }
    
    @Override
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return String.format("SELECT * FROM (SELECT * FROM %s) WHERE ROWNUM<=1", decorate(schemaName, tableName));
    }
    
    @Override
    public String buildSplitByPrimaryKeyRangeSQL(final String schemaName, final String tableName, final String primaryKey) {
        String quotedKey = quote(primaryKey);
        return String.format("SELECT MAX(%s) FROM (SELECT * FROM (SELECT %s FROM %s WHERE %s>=? ORDER BY %s) WHERE ROWNUM<=?) t",
                quotedKey, quotedKey, decorate(schemaName, tableName), quotedKey, quotedKey);
    }
    
    @Override
    public String getType() {
        return "Oracle";
    }
}
