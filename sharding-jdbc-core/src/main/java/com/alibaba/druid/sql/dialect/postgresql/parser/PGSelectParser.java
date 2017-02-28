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

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.PGLimit;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

public class PGSelectParser extends SQLSelectParser {
    
    public PGSelectParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    protected SQLExprParser createExprParser() {
        return new PGExprParser(getShardingRule(), getParameters(), getLexer());
    }
    
    @Override
    public SQLSelectQuery query() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelectQuery select = query();
            getLexer().accept(Token.RIGHT_PAREN);
            queryRest();
            return select;
        }
        PGSelectQueryBlock queryBlock = new PGSelectQueryBlock();
        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();
            getLexer().skipIfEqual(Token.COMMENT);
            parseDistinct();
            parseSelectList();
            if (getLexer().skipIfEqual(Token.INTO)) {
                getLexer().skipIfEqual(Token.TEMPORARY, Token.TEMP, Token.UNLOGGED);
                getLexer().skipIfEqual(Token.TABLE);
                createExprParser().name();
            }
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        if (getLexer().skipIfEqual(Token.WINDOW)) {
            getExprParser().expr();
            getLexer().accept(Token.AS);
            while (true) {
                createExprParser().expr();
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                } else {
                    break;
                }
            }
        }
        getSqlContext().getOrderByContexts().addAll(createExprParser().parseOrderBy());
        while (true) {
            if (getLexer().equalToken(Token.LIMIT)) {
                PGLimit limit = new PGLimit();
                getLexer().nextToken();
                if (getLexer().equalToken(Token.ALL)) {
                    limit.setRowCount(new SQLIdentifierExpr("ALL"));
                    getLexer().nextToken();
                } else {
                    limit.setRowCount(getExprParser().expr());
                }

                queryBlock.setLimit(limit);
            } else if (getLexer().equalToken(Token.OFFSET)) {
                PGLimit limit = queryBlock.getLimit();
                if (limit == null) {
                    limit = new PGLimit();
                    queryBlock.setLimit(limit);
                }
                getLexer().nextToken();
                limit.setOffset(getExprParser().expr());
                getLexer().skipIfEqual(Token.ROW, Token.ROWS);
            } else {
                break;
            }
        }
        if (getLexer().skipIfEqual(Token.FETCH)) {
            getLexer().skipIfEqual(Token.FIRST, Token.NEXT);
            getExprParser().expr();
            getLexer().skipIfEqual(Token.ROW, Token.ROWS);
            getLexer().skipIfEqual(Token.ONLY);
        }
        if (getLexer().skipIfEqual(Token.FOR)) {
            getLexer().skipIfEqual(Token.UPDATE, Token.SHARE);
            if (getLexer().equalToken(Token.OF)) {
                while (true) {
                    createExprParser().expr();
                    if (getLexer().equalToken(Token.COMMA)) {
                        getLexer().nextToken();
                    } else {
                        break;
                    }
                }
            }
            getLexer().skipIfEqual(Token.NOWAIT);
        }
        queryRest();
        return queryBlock;
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
