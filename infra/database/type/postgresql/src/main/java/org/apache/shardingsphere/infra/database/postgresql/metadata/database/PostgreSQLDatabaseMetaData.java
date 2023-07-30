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

package org.apache.shardingsphere.infra.database.postgresql.metadata.database;

import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Database meta data of PostgreSQL.
 */
public final class PostgreSQLDatabaseMetaData implements DialectDatabaseMetaData {
    
    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "ALL", "ANALYSE", "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASYMMETRIC", "AUTHORIZATION", "BETWEEN", "BIGINT", "BINARY",
            "BIT", "BOOLEAN", "BOTH", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "CONCURRENTLY", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_CATALOG",
            "CURRENT_DATE", "CURRENT_ROLE", "CURRENT_SCHEMA", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DEC", "DECIMAL", "DEFAULT", "DEFERRABLE", "DESC", "DISTINCT", "DO", "ELSE", "END",
            "EXCEPT", "EXISTS", "EXTRACT", "FALSE", "FETCH", "FLOAT", "FOR", "FOREIGN", "FREEZE", "FROM", "FULL", "GRANT", "GREATEST", "GROUP", "GROUPING", "HAVING", "ILIKE", "IN", "INITIALLY",
            "INNER", "INOUT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISNULL", "JOIN", "LATERAL", "LEADING", "LEAST", "LEFT", "LIKE", "LIMIT", "LOCALTIME", "LOCALTIMESTAMP",
            "NATIONAL", "NATURAL", "NCHAR", "NONE", "NORMALIZE", "NOT", "NOTNULL", "NULL", "NULLIF", "NUMERIC", "OFFSET", "ON", "ONLY", "OR", "ORDER", "OUT", "OUTER", "OVERLAPS", "OVERLAY", "PLACING",
            "POSITION", "PRECISION", "PRIMARY", "REAL", "REFERENCES", "RETURNING", "RIGHT", "ROW", "SELECT", "SESSION_USER", "SETOF", "SIMILAR", "SMALLINT", "SOME", "SUBSTRING", "SYMMETRIC", "TABLE",
            "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TO", "TRAILING", "TREAT", "TRIM", "TRUE", "UNION", "UNIQUE", "USER", "USING", "VALUES", "VARCHAR", "VARIADIC", "VERBOSE", "WHEN", "WHERE",
            "WINDOW", "WITH", "XMLATTRIBUTES", "XMLCONCAT", "XMLELEMENT", "XMLEXISTS", "XMLFOREST", "XMLNAMESPACES", "XMLPARSE", "XMLPI", "XMLROOT", "XMLSERIALIZE", "XMLTABLE"));
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return NullsOrderType.LAST;
    }
    
    @Override
    public boolean isReservedWord(final String identifier) {
        return RESERVED_WORDS.contains(identifier.toUpperCase());
    }
    
    @Override
    public boolean isSchemaAvailable() {
        return true;
    }
    
    @Override
    public Optional<String> getDefaultSchema() {
        return Optional.of("public");
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
