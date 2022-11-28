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

package org.apache.shardingsphere.data.pipeline.mysql.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.AbstractPipelineSQLBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * MySQL pipeline SQL builder.
 */
public final class MySQLPipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    private static final List<String> RESERVED_KEYWORDS = Arrays.asList("ADD", "ANALYZE", "ASC", "BERKELEYDB", "BINARY", "BTREE", "CASE", "CHARACTER", "COLUMN", "CREATE", "CURRENT_TIME", "DATABASES",
            "DAY_SECOND", "DEFAULT", "DESC", "DISTINCTROW", "DROP", "ERRORS", "EXPLAIN", "FLOAT", "FOREIGN", "FUNCTION", "GROUP", "HELP", "HOUR_SECOND", "IN", "INNER", "INT", "INTO", "KEY",
            "LEADING", "LIMIT", "LOCALTIME", "LONG", "LOW_PRIORITY", "MEDIUMBLOB", "MIDDLEINT", "MRG_MYISAM", "NULL", "OPTIMIZE", "OR", "OUTFILE", "PRIVILEGES", "READ", "REGEXP", "REQUIRE", "REVOKE",
            "RTREE", "SHOW", "SONAME", "SQL_CALC_FOUND_ROWS", "STARTING", "TABLE", "THEN", "TINYTEXT", "TRUE", "UNIQUE", "UPDATE", "USER_RESOURCES", "VARBINARY", "VARYING", "WHERE", "XOR", "ALL",
            "AND", "AUTO_INCREMENT", "BETWEEN", "BLOB", "BY", "CHANGE", "CHECK", "COLUMNS", "CROSS", "CURRENT_TIMESTAMP", "DAY_HOUR", "DEC", "DELAYED", "DESCRIBE", "DIV", "ELSE", "ESCAPED", "FALSE",
            "FOR", "FROM", "GEOMETRY", "HASH", "HIGH_PRIORITY", "IF", "INDEX", "INNODB", "INTEGER", "IS", "KEYS", "LEFT", "LINES", "LOCALTIMESTAMP", "LONGBLOB", "MASTER_SERVER_ID", "MEDIUMINT",
            "MINUTE_SECOND", "NATURAL", "NUMERIC", "OPTION", "ORDER", "PRECISION", "PROCEDURE", "REAL", "RENAME", "RESTRICT", "RIGHT", "SELECT", "SMALLINT", "SPATIAL", "SQL_SMALL_RESULT",
            "STRAIGHT_JOIN", "TABLES", "TINYBLOB", "TO", "TYPES", "UNLOCK", "USAGE", "USING", "VARCHAR", "WARNINGS", "WITH", "YEAR_MONTH", "ALTER", "AS", "BDB", "BIGINT", "BOTH", "CASCADE", "CHAR",
            "COLLATE", "CONSTRAINT", "CURRENT_DATE", "DATABASE", "DAY_MINUTE", "DECIMAL", "DELETE", "DISTINCT", "DOUBLE", "ENCLOSED", "EXISTS", "FIELDS", "FORCE", "FULLTEXT", "GRANT", "HAVING",
            "HOUR_MINUTE", "IGNORE", "INFILE", "INSERT", "INTERVAL", "JOIN", "KILL", "LIKE", "LOAD", "LOCK", "LONGTEXT", "MATCH", "MEDIUMTEXT", "MOD", "NOT", "ON", "OPTIONALLY", "OUTER", "PRIMARY",
            "PURGE", "REFERENCES", "REPLACE", "RETURNS", "RLIKE", "SET", "SOME", "SQL_BIG_RESULT", "SSL", "STRIPED", "TERMINATED", "TINYINT", "TRAILING", "UNION", "UNSIGNED", "USE", "VALUES",
            "VARCHARACTER", "WHEN", "WRITE", "ZEROFILL", "_FILENAME");
    
    @Override
    protected boolean isKeyword(final String item) {
        return RESERVED_KEYWORDS.contains(item.toUpperCase());
    }
    
    @Override
    public String getLeftIdentifierQuoteString() {
        return "`";
    }
    
    @Override
    public String getRightIdentifierQuoteString() {
        return "`";
    }
    
    @Override
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord) {
        return super.buildInsertSQL(schemaName, dataRecord) + buildDuplicateUpdateSQL(dataRecord);
    }
    
    private String buildDuplicateUpdateSQL(final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            Column column = dataRecord.getColumn(i);
            if (!column.isUpdated()) {
                continue;
            }
            // TOOD not skip unique key
            if (column.isUniqueKey()) {
                continue;
            }
            result.append(quote(column.getName())).append("=VALUES(").append(quote(column.getName())).append("),");
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }
    
    @Override
    public Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String column) {
        return Optional.of(String.format("SELECT BIT_XOR(CAST(CRC32(%s) AS UNSIGNED)) AS checksum, COUNT(1) AS cnt FROM %s", quote(column), quote(tableName)));
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
