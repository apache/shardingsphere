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

import java.util.Map;
import java.util.Set;

/**
 * Oracle pipeline SQL builder.
 */
public final class OraclePipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    @Override
    public String buildDivisibleInventoryDumpSQL(final String schemaName, final String tableName, final String uniqueKey, final int uniqueKeyDataType, final boolean firstQuery) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        return String.format("SELECT * FROM (SELECT * FROM %s WHERE %s%s? AND %s<=? ORDER BY %s ASC) WHERE ROWNUM<=?",
                qualifiedTableName, quotedUniqueKey, firstQuery ? ">=" : ">", quotedUniqueKey, quotedUniqueKey);
    }
    
    @Override
    public String buildIndivisibleInventoryDumpSQL(final String schemaName, final String tableName, final String uniqueKey, final int uniqueKeyDataType, final boolean firstQuery) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        return String.format("SELECT * FROM (SELECT * FROM %s WHERE %s%s? ORDER BY %s ASC) WHERE ROWNUM<=?", qualifiedTableName, quotedUniqueKey, firstQuery ? ">=" : ">", quotedUniqueKey);
    }
    
    @Override
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord, final Map<LogicTableName, Set<String>> shardingColumnsMap) {
        return super.buildInsertSQL(schemaName, dataRecord, shardingColumnsMap);
        // TODO buildInsertSQL and buildConflictSQL, need 2 round parameters set
        // TODO refactor PipelineSQLBuilder to combine SQL building and parameters set
    }
    
    @Override
    public String buildChunkedQuerySQL(final String schemaName, final @NonNull String tableName, final @NonNull String uniqueKey, final boolean firstQuery) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(uniqueKey);
        return firstQuery
                ? String.format("SELECT * FROM (SELECT * FROM %s ORDER BY %s ASC) WHERE ROWNUM<=?", qualifiedTableName, quotedUniqueKey)
                : String.format("SELECT * FROM (SELECT * FROM %s WHERE %s>? ORDER BY %s ASC) WHERE ROWNUM<=?", qualifiedTableName, quotedUniqueKey, quotedUniqueKey);
    }
    
    @Override
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return String.format("SELECT * FROM (SELECT * FROM %s) WHERE ROWNUM<=1", getQualifiedTableName(schemaName, tableName));
    }
    
    @Override
    public String buildSplitByPrimaryKeyRangeSQL(final String schemaName, final String tableName, final String primaryKey) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        String quotedUniqueKey = quote(primaryKey);
        return String.format("SELECT MAX(%s), COUNT(1) FROM (SELECT * FROM (SELECT %s FROM %s WHERE %s>=? ORDER BY %s) WHERE ROWNUM<=?) t",
                quotedUniqueKey, quotedUniqueKey, qualifiedTableName, quotedUniqueKey, quotedUniqueKey);
    }
    
    @Override
    public String getType() {
        return "Oracle";
    }
}
