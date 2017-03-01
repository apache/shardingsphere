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

package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumericLiteralExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.context.LimitContext;
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

public class SQLServerSelectParser extends SQLSelectParser {
    
    public SQLServerSelectParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void customizedSelect(final SelectSQLContext sqlContext) {
        if (getLexer().equalToken(Token.FOR)) {
            parseFor();
        }
        if (getLexer().equalToken(Token.OFFSET)) {
            parseOffset(sqlContext);
        }
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
        SQLServerSelectQueryBlock queryBlock = new SQLServerSelectQueryBlock();
        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }
            parseDistinct();
            if (getLexer().equalToken(Token.TOP)) {
                queryBlock.setTop(new SQLServerExprParser(getShardingRule(), getParameters(), getLexer()).parseTop());
            }
            parseSelectList();
        }
        if (getLexer().equalToken(Token.INTO)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        parseFrom();
        parseWhere();
        parseGroupBy();
        queryRest();
        return queryBlock;
    }
    
    @Override
    protected void parseJoinTable() {
        if (getLexer().skipIfEqual(Token.WITH)) {
            getLexer().skipParentheses();
        }
        super.parseJoinTable();
    }
    
    private void parseFor() {
        getLexer().nextToken();
        if (getLexer().identifierEquals("BROWSE")) {
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("XML")) {
            getLexer().nextToken();
            while (true) {
                if (getLexer().identifierEquals("AUTO") || getLexer().identifierEquals("TYPE") || getLexer().identifierEquals("XMLSCHEMA")) {
                    getLexer().nextToken();
                } else if (getLexer().identifierEquals("ELEMENTS")) {
                    getLexer().nextToken();
                    if (getLexer().identifierEquals("XSINIL")) {
                        getLexer().nextToken();
                    }
                } else {
                    break;
                }
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                } else {
                    break;
                }
            }
        } else {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }
    
    private void parseOffset(final SelectSQLContext sqlContext) {
        getLexer().nextToken();
        SQLExpr offsetExpr = getExprParser().expr();
        int offset;
        int offsetIndex = -1;
        if (offsetExpr instanceof SQLNumericLiteralExpr) {
            offset = ((SQLNumericLiteralExpr) offsetExpr).getNumber().intValue();
        } else if (offsetExpr instanceof SQLVariantRefExpr) {
            offsetIndex = getParametersIndex();
            offset = (int) getParameters().get(offsetIndex);
            setParametersIndex(offsetIndex + 1);
        } else {
            throw new UnsupportedOperationException("Cannot support offset for: " + offsetExpr.getClass().getCanonicalName());
        }
        getLexer().nextToken();
        LimitContext limitContext;
        if (getLexer().skipIfEqual(Token.FETCH)) {
            getLexer().nextToken();
            int rowCount;
            int rowCountIndex = -1;
            SQLExpr rowCountExpr = getExprParser().expr();
            if (rowCountExpr instanceof SQLNumericLiteralExpr) {
                rowCount = ((SQLNumericLiteralExpr) rowCountExpr).getNumber().intValue();
            } else if (rowCountExpr instanceof SQLVariantRefExpr) {
                rowCountIndex = getParametersIndex();
                rowCount = (int) getParameters().get(rowCountIndex);
                setParametersIndex(rowCountIndex + 1);
            } else {
                throw new UnsupportedOperationException("Cannot support rowCount for: " + rowCountExpr.getClass().getCanonicalName());
            }
            getLexer().nextToken();
            getLexer().nextToken();
            limitContext = new LimitContext(offset, rowCount, offsetIndex, rowCountIndex);
        } else {
            limitContext = new LimitContext(offset, offsetIndex);
        }
        sqlContext.setLimitContext(limitContext);
    }
}
