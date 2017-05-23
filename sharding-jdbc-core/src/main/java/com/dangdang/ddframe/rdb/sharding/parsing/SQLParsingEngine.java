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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.analyzer.Dictionary;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.DeleteSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.InsertSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.UpdateSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.PostgreSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.delete.SQLDeleteParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.insert.SQLInsertParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SQLSelectParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.update.SQLUpdateParserFactory;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.DerivedColumnUtils;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.GeneratedKeysUtils;
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
    
    /**
     * 解析SQL.
     * 
     * @return SQL解析上下文
     */
    public SQLContext parse() {
        SQLParser sqlParser = getSQLParser();
        sqlParser.skipIfEqual(Symbol.SEMI);
        if (sqlParser.equalAny(DefaultKeyword.WITH)) {
            skipWith(sqlParser);
        }
        if (sqlParser.equalAny(DefaultKeyword.SELECT)) {
            SelectSQLContext result = SQLSelectParserFactory.newInstance(sqlParser).parse();
            DerivedColumnUtils.appendDerivedColumns(result);
            return result;
        }
        if (sqlParser.equalAny(DefaultKeyword.INSERT)) {
            InsertSQLContext result = SQLInsertParserFactory.newInstance(shardingRule, sqlParser).parse();
            GeneratedKeysUtils.appendGenerateKeys(shardingRule, result);
            return result;
        }
        if (sqlParser.equalAny(DefaultKeyword.UPDATE)) {
            return SQLUpdateParserFactory.newInstance(sqlParser).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.DELETE)) {
            return SQLDeleteParserFactory.newInstance(sqlParser).parse();
        }
        throw new SQLParsingUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
    }
    
    private SQLParser getSQLParser() {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLParser(sql, shardingRule);
            case Oracle:
                return new OracleParser(sql, shardingRule);
            case SQLServer:
                return new SQLServerParser(sql, shardingRule);
            case PostgreSQL:
                return new PostgreSQLParser(sql, shardingRule);
            default:
                throw new UnsupportedOperationException(dbType.name());
        }
    }
    
    private void skipWith(final SQLParser sqlParser) {
        sqlParser.getLexer().nextToken();
        do {
            sqlParser.skipUntil(DefaultKeyword.AS);
            sqlParser.accept(DefaultKeyword.AS);
            sqlParser.skipParentheses();
        } while (sqlParser.skipIfEqual(Symbol.COMMA));
    }
    
    /**
     * 预解析SQL.
     * 仅获取SQL类型.
     *
     * @return SQL解析上下文
     */
    public SQLContext prepareParse() {
        Lexer lexer = new Lexer(sql, new Dictionary());
        lexer.nextToken();
        while (true) {
            TokenType tokenType = lexer.getCurrentToken().getType();
            if (tokenType instanceof Keyword) {
                if (tokenType.equals(DefaultKeyword.SELECT)) {
                    return new SelectSQLContext();
                } else if (tokenType.equals(DefaultKeyword.UPDATE)) {
                    return new UpdateSQLContext();
                } else if (tokenType.equals(DefaultKeyword.INSERT)) {
                    return new InsertSQLContext();
                } else if (tokenType.equals(DefaultKeyword.DELETE)) {
                    return new DeleteSQLContext();
                }
            }
            if (tokenType instanceof Assist && tokenType.equals(Assist.END)) {
                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
            }
            lexer.nextToken();
        }
    }
}
