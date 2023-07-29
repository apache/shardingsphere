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

package org.apache.shardingsphere.infra.database.mysql.metadata.database;

import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;

import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Database meta data of MySQL.
 */
public final class MySQLDatabaseMetaData implements DialectDatabaseMetaData {
    
    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL",
            "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT_DATE",
            "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
            "DELAYED", "DELETE", "DENSE_RANK", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EACH", "ELSE",
            "ELSEIF", "EMPTY", "ENCLOSED", "ESCAPED", "EXCEPT", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FIRST_VALUE", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT",
            "FUNCTION", "GENERATED", "GET", "GRANT", "GROUP", "GROUPING", "GROUPS", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX",
            "INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS",
            "ITERATE", "JOIN", "JSON_TABLE", "KEY", "KEYS", "KILL", "LAG", "LAST_VALUE", "LATERAL", "LEAD", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP",
            "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND", "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH", "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT",
            "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NTH_VALUE", "NTILE", "NULL", "NUMERIC", "OF", "ON", "OPTIMIZE", "OPTIMIZER_COSTS",
            "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "OVER", "PARTITION", "PERCENT_RANK", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE", "RANK", "READ", "REAL", "RECURSIVE",
            "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESIGNAL", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "ROW", "ROWS", "ROW_NUMBER", "SCHEMA", "SCHEMAS",
            "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SIGNAL", "SMALLINT", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS",
            "SQL_SMALL_RESULT", "SSL", "STARTING", "STORED", "STRAIGHT_JOIN", "SYSTEM", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO",
            "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "VIRTUAL",
            "WHEN", "WHERE", "WHILE", "WINDOW", "WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL"));
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.BACK_QUOTE;
    }
    
    @Override
    public Map<String, Integer> getExtraDataTypes() {
        Map<String, Integer> result = new HashMap<>(10, 1F);
        result.put("JSON", Types.LONGVARCHAR);
        result.put("GEOMETRY", Types.BINARY);
        result.put("GEOMETRYCOLLECTION", Types.BINARY);
        result.put("YEAR", Types.DATE);
        result.put("POINT", Types.BINARY);
        result.put("MULTIPOINT", Types.BINARY);
        result.put("POLYGON", Types.BINARY);
        result.put("MULTIPOLYGON", Types.BINARY);
        result.put("LINESTRING", Types.BINARY);
        result.put("MULTILINESTRING", Types.BINARY);
        return result;
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return NullsOrderType.FIRST;
    }
    
    @Override
    public boolean isReservedWord(final String identifier) {
        return RESERVED_WORDS.contains(identifier.toUpperCase());
    }
    
    @Override
    public boolean isInstanceConnectionAvailable() {
        return true;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
