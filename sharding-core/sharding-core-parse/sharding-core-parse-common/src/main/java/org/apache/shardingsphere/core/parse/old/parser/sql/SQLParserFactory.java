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

package org.apache.shardingsphere.core.parse.old.parser.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.AntlrParsingEngine;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.DQLStatement;
import org.apache.shardingsphere.core.parse.old.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.old.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.TokenType;
import org.apache.shardingsphere.core.parse.old.parser.exception.SQLParsingUnsupportedException;
import org.apache.shardingsphere.core.parse.old.parser.sql.dml.delete.DeleteParserFactory;
import org.apache.shardingsphere.core.parse.old.parser.sql.dml.insert.InsertParserFactory;
import org.apache.shardingsphere.core.parse.old.parser.sql.dml.select.SelectParserFactory;
import org.apache.shardingsphere.core.parse.old.parser.sql.dml.update.UpdateParserFactory;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * SQL parser factory.
 *
 * @author zhangliang
 * @author panjuan
 * @author maxiaoguang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserFactory {
    
    /**
     * Create SQL parser.
     *
     * @param dbType database type
     * @param shardingRule databases and tables sharding rule
     * @param lexerEngine lexical analysis engine
     * @param shardingTableMetaData sharding table metadata
     * @param sql SQL to parse
     * @return SQL parser
     */
    public static SQLParser newInstance(
            final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData, final String sql) {
        if (DatabaseType.MySQL == dbType || DatabaseType.H2 == dbType) {
            return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
        }
        lexerEngine.nextToken();
        TokenType tokenType = lexerEngine.getCurrentToken().getType();
        if (DQLStatement.isDQL(tokenType)) {
            return getDQLParser(dbType, shardingRule, lexerEngine, shardingTableMetaData);
        }
        if (DMLStatement.isDML(tokenType)) {
            return getDMLParser(dbType, sql, tokenType, shardingRule, lexerEngine, shardingTableMetaData);
        }
        return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
    }
    
    /**
     * Create encrypt SQL parser.
     * 
     * @param dbType db type
     * @param encryptRule encrypt rule
     * @param shardingTableMetaData sharding table meta data
     * @param sql sql
     * @return sql parser
     */
    public static SQLParser newInstance(final DatabaseType dbType, final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData, final String sql) {
        if (DatabaseType.MySQL == dbType || DatabaseType.H2 == dbType) {
            return new AntlrParsingEngine(dbType, sql, encryptRule, shardingTableMetaData);
        }
        throw new SQLParsingUnsupportedException(String.format("Can not support %s", dbType)); 
    }
    
    private static SQLParser getDQLParser(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        return SelectParserFactory.newInstance(dbType, shardingRule, lexerEngine, shardingTableMetaData);
    }
    
    private static SQLParser getDMLParser(
            final DatabaseType dbType, final String sql, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        switch ((DefaultKeyword) tokenType) {
            case INSERT:
                return InsertParserFactory.newInstance(dbType, sql, shardingRule, lexerEngine, shardingTableMetaData);
            case UPDATE:
                return UpdateParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case DELETE:
                return DeleteParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            default:
                throw new SQLParsingUnsupportedException(tokenType);
        }
    }
}
