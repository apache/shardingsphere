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

package org.apache.shardingsphere.infra.optimizer.util;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.optimizer.converter.SqlNodeConverter;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * Facade for SQL parser. Calcite sql parse will be used, before the converter of ShardingSphere to Calcite ast is ready.
 */
public class SqlParserFacade {
    
    private static ShardingSphereSQLParserEngine sqlStatementParserEngine = new ShardingSphereSQLParserEngine(DatabaseTypeRegistry.getTrunkDatabaseTypeName(
            new MySQLDatabaseType()));
    
    /**
     * parse sql text to calcite ast.
     * @param sql sql text
     * @return calcite ast
     */
    public static SqlNode parse(final String sql) {
        SqlNode sqlNode = null;
        // sqlNode = parseWithSs(sql);
        SqlParser parser = SqlParser.create(sql, SqlParser.config().withLex(Lex.MYSQL));
        try {
            sqlNode = parser.parseQuery();
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        }
        return sqlNode;
    }
    
    private static SqlNode parseWithSs(final String sql) {
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(sql, false);
        Optional<SqlNode> optional = SqlNodeConverter.convertSqlStatement(sqlStatement);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
