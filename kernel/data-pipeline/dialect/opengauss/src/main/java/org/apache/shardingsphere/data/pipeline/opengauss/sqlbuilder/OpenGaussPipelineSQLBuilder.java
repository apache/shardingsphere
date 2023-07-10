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

package org.apache.shardingsphere.data.pipeline.opengauss.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.AbstractPipelineSQLBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pipeline SQL builder of openGauss.
 */
public final class OpenGaussPipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>(Arrays.asList(
            "ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHID", "AUTHORIZATION", "BETWEEN", "BIGINT",
            "BINARY", "BINARY_DOUBLE", "BINARY_INTEGER", "BIT", "BOOLEAN", "BOTH", "BUCKETCNT", "BUCKETS", "BYTEAWITHOUTORDER", "BYTEAWITHOUTORDERWITHEQUAL", "CASE", "CAST", "CHAR", "CHARACTER",
            "CHECK", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMPACT", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS", "CSN", "CURRENT_CATALOG", "CURRENT_DATE", "CURRENT_ROLE",
            "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DATE", "DEC", "DECIMAL", "DECODE", "DEFAULT", "DEFERRABLE", "DELTAMERGE", "DESC", "DISTINCT", "DO", "ELSE", "END",
            "EXCEPT", "EXCLUDED", "EXISTS", "EXTRACT", "FALSE", "FENCED", "FETCH", "FLOAT", "FOR", "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT", "GREATEST", "GROUP", "GROUPING", "GROUPPARENT",
            "HAVING", "HDFSDIRECTORY", "ILIKE", "IN", "INITIALLY", "INNER", "INOUT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "JOIN", "LEADING", "LEAST", "LEFT", "LESS", "LIKE",
            "LIMIT", "LOCALTIME", "LOCALTIMESTAMP", "MAXVALUE", "MINUS", "MODIFY", "NATIONAL", "NATURAL", "NCHAR", "NOCYCLE", "NONE", "NOT", "NOTNULL", "NULL", "NULLIF", "NUMBER", "NUMERIC",
            "NVARCHAR", "NVARCHAR2", "NVL", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUT", "OUTER", "OVERLAPS", "OVERLAY", "PERFORMANCE", "PLACING", "POSITION", "PRECISION", "PRIMARY", "PRIORER",
            "PROCEDURE", "REAL", "RECYCLEBIN", "REFERENCES", "REJECT", "RETURNING", "RIGHT", "ROW", "ROWNUM", "SELECT", "SESSION_USER", "SETOF", "SIMILAR", "SMALLDATETIME", "SMALLINT", "SOME",
            "SUBSTRING", "SYMMETRIC", "SYSDATE", "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMECAPSULE", "TIMESTAMP", "TIMESTAMPDIFF", "TINYINT", "TO", "TRAILING", "TREAT", "TRIM", "TRUE", "UNION",
            "UNIQUE", "USER", "USING", "VALUES", "VARCHAR", "VARCHAR2", "VARIADIC", "VERBOSE", "VERIFY", "WHEN", "WHERE", "WINDOW", "WITH", "XMLATTRIBUTES", "XMLCONCAT", "XMLELEMENT", "XMLEXISTS",
            "XMLFOREST", "XMLPARSE", "XMLPI", "XMLROOT", "XMLSERIALIZE"));
    
    @Override
    protected boolean isKeyword(final String item) {
        return RESERVED_KEYWORDS.contains(item.toUpperCase());
    }
    
    @Override
    protected String getLeftIdentifierQuoteString() {
        return "\"";
    }
    
    @Override
    protected String getRightIdentifierQuoteString() {
        return "\"";
    }
    
    @Override
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return Optional.of(String.format("CREATE SCHEMA %s", quote(schemaName)));
    }
    
    @Override
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord) {
        return super.buildInsertSQL(schemaName, dataRecord) + buildConflictSQL(dataRecord);
    }
    
    @Override
    public List<Column> extractUpdatedColumns(final DataRecord dataRecord) {
        return dataRecord.getColumns().stream().filter(each -> !(each.isUniqueKey())).collect(Collectors.toList());
    }
    
    private String buildConflictSQL(final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            Column column = dataRecord.getColumn(i);
            if (column.isUniqueKey()) {
                continue;
            }
            result.append(quote(column.getName())).append("=EXCLUDED.").append(quote(column.getName())).append(',');
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }
    
    @Override
    public Optional<String> buildEstimatedCountSQL(final String schemaName, final String tableName) {
        String qualifiedTableName = getQualifiedTableName(schemaName, tableName);
        return Optional.of(String.format("SELECT reltuples::integer FROM pg_class WHERE oid='%s'::regclass::oid;", qualifiedTableName));
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
