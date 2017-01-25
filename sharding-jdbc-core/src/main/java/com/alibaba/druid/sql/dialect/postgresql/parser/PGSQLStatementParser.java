/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
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
 */

package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithClause;
import com.alibaba.druid.sql.dialect.postgresql.ast.PGWithQuery;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLDeleteParserFactory;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.parser.SQLUpdateParserFactory;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

public class PGSQLStatementParser extends SQLStatementParser {
    
    public PGSQLStatementParser(final ShardingRule shardingRule, final List<Object> parameters, final String sql) {
        super(shardingRule, parameters, new PGExprParser(sql));
    }
    
    @Override
    protected PGSelectParser createSQLSelectParser() {
        return new PGSelectParser(getExprParser());
    }
    
    public PGWithClause parseWithClause() {
        getLexer().nextToken();

        PGWithClause withClause = new PGWithClause();

        if (getLexer().equalToken(Token.RECURSIVE)) {
            getLexer().nextToken();
            withClause.setRecursive(true);
        }
    
        while (true) {
            PGWithQuery withQuery = withQuery();
            withClause.getWithQuery().add(withQuery);
            if (getLexer().equalToken(Token.COMMA)) {
                getLexer().nextToken();
            } else {
                break;
            }
        }
        return withClause;
    }

    private PGWithQuery withQuery() {
        PGWithQuery withQuery = new PGWithQuery();
        
        if (getLexer().equalToken(Token.LITERAL_ALIAS)) {
            withQuery.setName(new SQLIdentifierExpr("\"" + getLexer().getLiterals()
                    + "\""));
        } else {
            withQuery.setName(new SQLIdentifierExpr(getLexer().getLiterals()));
        }
        getLexer().nextToken();

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            while (true) {
                withQuery.getColumns().add(getExprParser().expr());
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                } else {
                    break;
                }
            }

            accept(Token.RIGHT_PAREN);
        }

        accept(Token.AS);

        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            SQLStatement query;
            if (getLexer().equalToken(Token.SELECT)) {
                query = parseSelect();
            } else if (getLexer().equalToken(Token.INSERT)) {
                query = new PostgreSQLInsertParser(getShardingRule(), getParameters(), getExprParser()).parse();
            } else if (getLexer().equalToken(Token.UPDATE)) {
                query = SQLUpdateParserFactory.newInstance(getShardingRule(), getParameters(), getExprParser(), JdbcConstants.POSTGRESQL).parse();
            } else if (getLexer().equalToken(Token.DELETE)) {
                query = SQLDeleteParserFactory.newInstance(getShardingRule(), getParameters(), getExprParser(), JdbcConstants.POSTGRESQL).parse();
            } else if (getLexer().equalToken(Token.VALUES)) {
                query = parseSelect();
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
            withQuery.setQuery(query);

            accept(Token.RIGHT_PAREN);
        }

        return withQuery;
    }
    
    @Override
    protected PGSelectStatement parseSelect() {
        return new PGSelectStatement(createSQLSelectParser().select());
    }
    
    @Override
    public SQLStatement parseWith() {
        PGWithClause with = parseWithClause();
        if (getLexer().equalToken(Token.INSERT)) {
//            PGInsertStatement stmt = (PGInsertStatement) new PostgreSQLInsertParser(getExprParser()).parse();
//            stmt.setWith(with);
//            return stmt;
            // TODO call AbstractDeleteParser, need sharding rule
            return null;
        }

        if (getLexer().equalToken(Token.SELECT)) {
            PGSelectStatement stmt = parseSelect();
            stmt.setWith(with);
            return stmt;
        }

        if (getLexer().equalToken(Token.DELETE)) {
            // TODO call AbstractDeleteParser, need sharding rule
            return null;
        }
        throw new ParserUnsupportedException(getLexer().getToken());
    }
}
