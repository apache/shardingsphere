/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.alter.AlterParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.create.CreateParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.drop.DropParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.ddl.truncate.TruncateParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.delete.DeleteParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.insert.InsertParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dml.update.UpdateParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectParserFactory;
import lombok.RequiredArgsConstructor;

/**
 * SQL解析引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLParsingEngine {
    
    private final DatabaseType dbType;
    
    private final String sql;
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    public SQLParsingEngine(final DatabaseType dbType, final String sql, final ShardingRule shardingRule) {
        this.dbType = dbType;
        this.sql = sql;
        this.shardingRule = shardingRule;
        lexerEngine = getLexerEngine();
    }
    
    private LexerEngine getLexerEngine() {
        switch (dbType) {
            case H2:
            case MySQL:
                return new LexerEngine(new MySQLLexer(sql));
            case Oracle:
                return new LexerEngine(new OracleLexer(sql));
            case SQLServer:
                return new LexerEngine(new SQLServerLexer(sql));
            case PostgreSQL:
                return new LexerEngine(new PostgreSQLLexer(sql));
            default:
                throw new UnsupportedOperationException(dbType.name());
        }
    }
    
    /**
     * 解析SQL.
     * 
     * @return SQL语句对象
     */
    public SQLStatement parse() {
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(DefaultKeyword.SELECT)) {
            return SelectParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        if (lexerEngine.equalAny(DefaultKeyword.INSERT)) {
            return InsertParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        if (lexerEngine.equalAny(DefaultKeyword.UPDATE)) {
            return UpdateParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        if (lexerEngine.equalAny(DefaultKeyword.DELETE)) {
            return DeleteParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        if (lexerEngine.equalAny(DefaultKeyword.CREATE)) {
            return CreateParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        if (lexerEngine.equalAny(DefaultKeyword.ALTER)) {
            return AlterParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        if (lexerEngine.equalAny(DefaultKeyword.DROP)) {
            return DropParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        if (lexerEngine.equalAny(DefaultKeyword.TRUNCATE)) {
            return TruncateParserFactory.newInstance(dbType, shardingRule, lexerEngine).parse();
        }
        throw new SQLParsingUnsupportedException(lexerEngine.getCurrentToken().getType());
    }
}
