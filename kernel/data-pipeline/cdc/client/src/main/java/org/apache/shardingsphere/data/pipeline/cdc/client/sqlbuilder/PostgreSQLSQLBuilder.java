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

package org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PostgreSQL SQL builder.
 */
public final class PostgreSQLSQLBuilder extends AbstractSQLBuilder {
    
    private static final List<String> RESERVED_KEYWORDS = Arrays.asList("ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHORIZATION", "BETWEEN", "BIGINT", "BINARY",
            "BIT", "BOOLEAN", "BOTH", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_CATALOG",
            "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEC", "DECIMAL", "DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END",
            "EXCEPT", "EXISTS", "EXTRACT", "FALSE", "FETCH", "FLOAT", "FOR", "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT", "GREATEST", "GROUP", "GROUPING", "HAVING", "ILIKE", "IN", "INITIALLY",
            "INNER", "INOUT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISNULL", "JOIN", "LATERAL", "LEADING", "LEAST", "LEFT", "LIKE", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP",
            "NATIONAL", "NATURAL", "NCHAR", "NONE", "NORMALIZE", "NOT", "NOTNULL", "NULL", "NULLIF", "NUMERIC", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUT", "OUTER", "OVERLAPS", "OVERLAY", "PLACING",
            "POSITION", "PRECISION", "PRIMARY", "REAL", "REFERENCES", "RETURNING", "RIGHT", "ROW", "SELECT", "SESSION_USER", "SETOF", "SIMILAR", "SMALLINT", "SOME", "SUBSTRING", "SYMMETRIC", "TABLE",
            "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TO", "TRAILING", "TREAT", "TRIM", "TRUE", "UNION", "UNIQUE", "USER", "USING", "VALUES", "VARCHAR", "VARIADIC", "VERBOSE", "WHEN", "WHERE",
            "WINDOW", "WITH", "XMLATTRIBUTES", "XMLCONCAT", "XMLELEMENT", "XMLEXISTS", "XMLFOREST", "XMLNAMESPACES", "XMLPARSE", "XMLPI", "XMLROOT", "XMLSERIALIZE", "XMLTABLE");
    
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
    public String buildInsertSQL(final Record record) {
        String insertSql = super.buildInsertSQL(record);
        List<String> uniqueKeyNamesList = record.getTableMetaData().getUniqueKeyNamesList();
        if (uniqueKeyNamesList.isEmpty()) {
            return insertSql;
        }
        StringBuilder updateValue = new StringBuilder();
        for (String each : record.getAfterMap().keySet()) {
            if (uniqueKeyNamesList.contains(each)) {
                continue;
            }
            updateValue.append(quote(each)).append("=EXCLUDED.").append(quote(each)).append(",");
        }
        updateValue.setLength(updateValue.length() - 1);
        String uniqueKeyNames = uniqueKeyNamesList.stream().map(this::quote).collect(Collectors.joining(","));
        return insertSql + String.format(" ON CONFLICT (%s) DO UPDATE SET %s", uniqueKeyNames, updateValue);
    }
}
