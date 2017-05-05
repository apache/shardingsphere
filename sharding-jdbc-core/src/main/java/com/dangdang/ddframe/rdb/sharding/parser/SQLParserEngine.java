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

package com.dangdang.ddframe.rdb.sharding.parser;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.parser.dialect.mysql.parser.MySQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.dialect.oracle.parser.OracleParser;
import com.dangdang.ddframe.rdb.sharding.parser.dialect.postgresql.parser.PostgreSQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.dialect.sqlserver.parser.SQLServerParser;
import com.dangdang.ddframe.rdb.sharding.parser.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parser.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parser.parser.delete.SQLDeleteParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.parser.insert.SQLInsertParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.parser.select.SQLSelectParserFactory;
import com.dangdang.ddframe.rdb.sharding.parser.parser.update.SQLUpdateParserFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * SQL解析引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLParserEngine {
    
    private final DatabaseType dbType;
    
    private final String sql;
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    /**
     * 解析SQL.
     * 
     * @return SQL解析对象
     */
    public SQLContext parseStatement() {
        SQLParser sqlParser = getSQLParser(sql);
        sqlParser.skipIfEqual(Symbol.SEMI);
        if (sqlParser.equalAny(DefaultKeyword.WITH)) {
            skipWith(sqlParser);
        }
        if (sqlParser.equalAny(DefaultKeyword.SELECT)) {
            return SQLSelectParserFactory.newInstance(sqlParser, dbType).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.INSERT)) {
            return SQLInsertParserFactory.newInstance(shardingRule, parameters, sqlParser, dbType).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.UPDATE)) {
            return SQLUpdateParserFactory.newInstance(sqlParser, dbType).parse();
        }
        if (sqlParser.equalAny(DefaultKeyword.DELETE)) {
            return SQLDeleteParserFactory.newInstance(sqlParser, dbType).parse();
        }
        throw new ParserUnsupportedException(sqlParser.getLexer().getCurrentToken().getType());
    }
    
    private SQLParser getSQLParser(final String sql) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLParser(sql, shardingRule, parameters);
            case Oracle:
                return new OracleParser(sql, shardingRule, parameters);
            case SQLServer:
                return new SQLServerParser(sql, shardingRule, parameters);
            case PostgreSQL:
                return new PostgreSQLParser(sql, shardingRule, parameters);
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
}
