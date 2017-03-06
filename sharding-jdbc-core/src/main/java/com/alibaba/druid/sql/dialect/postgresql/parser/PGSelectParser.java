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
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGSelectQueryBlock.PGLimit;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

public class PGSelectParser extends AbstractSelectParser {
    
    public PGSelectParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    protected SQLExprParser createExprParser() {
        return new PGExprParser(getShardingRule(), getParameters(), getExprParser().getLexer());
    }
    
    @Override
    public SQLSelectQuery query(final SelectSQLContext sqlContext) {
        if (getExprParser().getLexer().equalToken(Token.LEFT_PAREN)) {
            getExprParser().getLexer().nextToken();
            SQLSelectQuery select = query(sqlContext);
            getExprParser().getLexer().accept(Token.RIGHT_PAREN);
            queryRest();
            return select;
        }
        PGSelectQueryBlock queryBlock = new PGSelectQueryBlock();
        if (getExprParser().getLexer().equalToken(Token.SELECT)) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().skipIfEqual(Token.COMMENT);
            parseDistinct(sqlContext);
            parseSelectList(sqlContext);
            if (getExprParser().getLexer().skipIfEqual(Token.INTO)) {
                getExprParser().getLexer().skipIfEqual(Token.TEMPORARY, Token.TEMP, Token.UNLOGGED);
                getExprParser().getLexer().skipIfEqual(Token.TABLE);
                createExprParser().name();
            }
        }
        parseFrom(sqlContext);
        parseWhere(sqlContext);
        parseGroupBy(sqlContext);
        if (getExprParser().getLexer().skipIfEqual(Token.WINDOW)) {
            getExprParser().expr();
            getExprParser().getLexer().accept(Token.AS);
            while (true) {
                createExprParser().expr();
                if (getExprParser().getLexer().equalToken(Token.COMMA)) {
                    getExprParser().getLexer().nextToken();
                } else {
                    break;
                }
            }
        }
        sqlContext.getOrderByContexts().addAll(createExprParser().parseOrderBy());
        while (true) {
            if (getExprParser().getLexer().equalToken(Token.LIMIT)) {
                PGLimit limit = new PGLimit();
                getExprParser().getLexer().nextToken();
                if (getExprParser().getLexer().equalToken(Token.ALL)) {
                    limit.setRowCount(new SQLIdentifierExpr("ALL"));
                    getExprParser().getLexer().nextToken();
                } else {
                    limit.setRowCount(getExprParser().expr());
                }

                queryBlock.setLimit(limit);
            } else if (getExprParser().getLexer().equalToken(Token.OFFSET)) {
                PGLimit limit = queryBlock.getLimit();
                if (limit == null) {
                    limit = new PGLimit();
                    queryBlock.setLimit(limit);
                }
                getExprParser().getLexer().nextToken();
                limit.setOffset(getExprParser().expr());
                getExprParser().getLexer().skipIfEqual(Token.ROW, Token.ROWS);
            } else {
                break;
            }
        }
        if (getExprParser().getLexer().skipIfEqual(Token.FETCH)) {
            getExprParser().getLexer().skipIfEqual(Token.FIRST, Token.NEXT);
            getExprParser().expr();
            getExprParser().getLexer().skipIfEqual(Token.ROW, Token.ROWS);
            getExprParser().getLexer().skipIfEqual(Token.ONLY);
        }
        if (getExprParser().getLexer().skipIfEqual(Token.FOR)) {
            getExprParser().getLexer().skipIfEqual(Token.UPDATE, Token.SHARE);
            if (getExprParser().getLexer().equalToken(Token.OF)) {
                while (true) {
                    createExprParser().expr();
                    if (getExprParser().getLexer().equalToken(Token.COMMA)) {
                        getExprParser().getLexer().nextToken();
                    } else {
                        break;
                    }
                }
            }
            getExprParser().getLexer().skipIfEqual(Token.NOWAIT);
        }
        queryRest();
        return queryBlock;
    }
    
    protected boolean hasDistinctOn() {
        return true;
    }
}
