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

package org.apache.shardingsphere.infra.database.mysql;

import org.apache.shardingsphere.infra.database.core.type.TrunkDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Database type of MySQL.
 */
public final class MySQLDatabaseType implements TrunkDatabaseType {
    
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
    
    private static final Map<String, Collection<String>> SYSTEM_DATABASE_SCHEMA_MAP = new HashMap<>();
    
    static {
        SYSTEM_DATABASE_SCHEMA_MAP.put("information_schema", Collections.singletonList("information_schema"));
        SYSTEM_DATABASE_SCHEMA_MAP.put("performance_schema", Collections.singletonList("performance_schema"));
        SYSTEM_DATABASE_SCHEMA_MAP.put("mysql", Collections.singletonList("mysql"));
        SYSTEM_DATABASE_SCHEMA_MAP.put("sys", Collections.singletonList("sys"));
        SYSTEM_DATABASE_SCHEMA_MAP.put("shardingsphere", Collections.singletonList("shardingsphere"));
    }
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.BACK_QUOTE;
    }
    
    @Override
    public boolean isReservedWord(final String item) {
        return RESERVED_WORDS.contains(item.toUpperCase());
    }
    
    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Arrays.asList("jdbc:mysql:", "jdbc:mysqlx:");
    }
    
    @Override
    public MySQLDataSourceMetaData getDataSourceMetaData(final String url, final String username) {
        return new MySQLDataSourceMetaData(url);
    }
    
    @Override
    public MySQLDataSourceMetaData getDataSourceMetaData(final String url, final String username, final String catalog) {
        return new MySQLDataSourceMetaData(url, catalog);
    }
    
    @Override
    public Map<String, Collection<String>> getSystemDatabaseSchemaMap() {
        return SYSTEM_DATABASE_SCHEMA_MAP;
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return SYSTEM_DATABASE_SCHEMA_MAP.keySet();
    }
    
    @Override
    public boolean isInstanceConnectionAvailable() {
        return true;
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
