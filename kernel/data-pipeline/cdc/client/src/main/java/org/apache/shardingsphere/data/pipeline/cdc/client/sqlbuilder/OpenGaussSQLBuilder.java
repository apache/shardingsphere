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

/**
 * Pipeline SQL builder of openGauss.
 */
public final class OpenGaussSQLBuilder extends AbstractSQLBuilder {
    
    private static final List<String> RESERVED_KEYWORDS = Arrays.asList("ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHID", "AUTHORIZATION", "BETWEEN", "BIGINT",
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
            "XMLFOREST", "XMLPARSE", "XMLPI", "XMLROOT", "XMLSERIALIZE");
    
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
        if (!record.getTableMetaData().getUniqueKeyNamesList().isEmpty()) {
            return insertSql + " ON DUPLICATE KEY UPDATE NOTHING";
        }
        return insertSql;
    }
}
