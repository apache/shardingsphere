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

package org.apache.shardingsphere.core.parsing.parser.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.AntlrParsingEngine;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.TokenType;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.describe.DescribeParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.set.SetParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.show.ShowParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.use.UseParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.delete.DeleteParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.update.UpdateParserFactory;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.DQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectParserFactory;
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
     * @param shardingTableMetaData sharding metadata
     * @param sql sql to parse
     * @return SQL parser
     */
    public static SQLParser newInstance(
            final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData, final String sql) {
        lexerEngine.nextToken();
        TokenType tokenType = lexerEngine.getCurrentToken().getType();
        if (DQLStatement.isDQL(tokenType)) {
            if (DatabaseType.MySQL == dbType) {
                return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
            }
            return getDQLParser(dbType, shardingRule, lexerEngine, shardingTableMetaData);
        }
        if (DMLStatement.isDML(tokenType)) {
            if (DatabaseType.MySQL == dbType) {
                return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
            }
            return getDMLParser(dbType, tokenType, shardingRule, lexerEngine, shardingTableMetaData);
        }
        if (TCLStatement.isTCL(tokenType)) {
            return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
        }
        if (DALStatement.isDAL(tokenType)) {
            return getDALParser(dbType, (Keyword) tokenType, shardingRule, lexerEngine);
        }
        lexerEngine.nextToken();
        TokenType secondaryTokenType = lexerEngine.getCurrentToken().getType();
        if (DCLStatement.isDCL(tokenType, secondaryTokenType)) {
            return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
        }
        if (DDLStatement.isDDL(tokenType, secondaryTokenType)) {
            return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
        }
        if (TCLStatement.isTCLUnsafe(dbType, tokenType, lexerEngine)) {
            return new AntlrParsingEngine(dbType, sql, shardingRule, shardingTableMetaData);
        }
        if (DefaultKeyword.SET.equals(tokenType)) {
            return SetParserFactory.newInstance();
        }
        throw new SQLParsingUnsupportedException(tokenType);
    }
    
    private static SQLParser getDQLParser(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        return SelectParserFactory.newInstance(dbType, shardingRule, lexerEngine, shardingTableMetaData);
    }
    
    private static SQLParser getDMLParser(
            final DatabaseType dbType, final TokenType tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        switch ((DefaultKeyword) tokenType) {
            case INSERT:
                return InsertParserFactory.newInstance(dbType, shardingRule, lexerEngine, shardingTableMetaData);
            case UPDATE:
                return UpdateParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            case DELETE:
                return DeleteParserFactory.newInstance(dbType, shardingRule, lexerEngine);
            default:
                throw new SQLParsingUnsupportedException(tokenType);
        }
    }
    
    private static SQLParser getDALParser(final DatabaseType dbType, final Keyword tokenType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        if (DefaultKeyword.USE == tokenType) {
            return UseParserFactory.newInstance(dbType, shardingRule, lexerEngine);
        }
        if (DefaultKeyword.DESC == tokenType || MySQLKeyword.DESCRIBE == tokenType) {
            return DescribeParserFactory.newInstance(dbType, shardingRule, lexerEngine);
        }
        if (MySQLKeyword.SHOW == tokenType) {
            return ShowParserFactory.newInstance(dbType, shardingRule, lexerEngine);
        }
        throw new SQLParsingUnsupportedException(tokenType);
    }
}
