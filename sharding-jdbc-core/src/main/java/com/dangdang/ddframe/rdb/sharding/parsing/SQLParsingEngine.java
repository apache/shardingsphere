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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.postgresql.PostgreSQLLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerLexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
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
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;
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
     * @return SQL语句对象
     */
    public SQLStatement parse() {
        CommonParser commonParser = getCommonParser();
        skipToSQLBegin(commonParser);
        if (commonParser.equalAny(DefaultKeyword.SELECT)) {
            return SelectParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        if (commonParser.equalAny(DefaultKeyword.INSERT)) {
            return InsertParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        if (commonParser.equalAny(DefaultKeyword.UPDATE)) {
            return UpdateParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        if (commonParser.equalAny(DefaultKeyword.DELETE)) {
            return DeleteParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        if (commonParser.equalAny(DefaultKeyword.CREATE)) {
            return CreateParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        if (commonParser.equalAny(DefaultKeyword.ALTER)) {
            return AlterParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        if (commonParser.equalAny(DefaultKeyword.DROP)) {
            return DropParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        if (commonParser.equalAny(DefaultKeyword.TRUNCATE)) {
            return TruncateParserFactory.newInstance(shardingRule, commonParser).parse();
        }
        throw new SQLParsingUnsupportedException(commonParser.getLexer().getCurrentToken().getType());
    }
    
    private CommonParser getCommonParser() {
        switch (dbType) {
            case H2:
            case MySQL:
                return new CommonParser(new MySQLLexer(sql));
            case Oracle:
                return new CommonParser(new OracleLexer(sql));
            case SQLServer:
                return new CommonParser(new SQLServerLexer(sql));
            case PostgreSQL:
                return new CommonParser(new PostgreSQLLexer(sql));
            default:
                throw new UnsupportedOperationException(dbType.name());
        }
    }
    
    private void skipToSQLBegin(final CommonParser parser) {
        parser.getLexer().nextToken();
        parser.skipIfEqual(Symbol.SEMI);
        if (parser.equalAny(DefaultKeyword.WITH)) {
            skipWith(parser);
        }
    }
    
    private void skipWith(final CommonParser parser) {
        parser.getLexer().nextToken();
        do {
            parser.skipUntil(DefaultKeyword.AS);
            parser.accept(DefaultKeyword.AS);
            // TODO with 中包含 ? 无法获取
            parser.skipParentheses(new SelectStatement());
        } while (parser.skipIfEqual(Symbol.COMMA));
    }
}
