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

package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.context.SelectSQLContext;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleSelectQueryBlock;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.google.common.base.Optional;

import java.util.List;

public class OracleSelectParser extends SQLSelectParser {
    
    public OracleSelectParser(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser) {
        super(shardingRule, parameters, exprParser);
    }
    
    @Override
    protected void customizedSelect(final SelectSQLContext sqlContext) {
        if (getLexer().equalToken(Token.FOR)) {
            parseForUpdate();
        }
        if (getSqlContext().getOrderByContexts().isEmpty()) {
            getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy());
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
        OracleSelectQueryBlock queryBlock = new OracleSelectQueryBlock();
        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();
            while (getLexer().equalToken(Token.HINT) || getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }
            parseDistinct();
            getLexer().skipIfEqual(Token.HINT);
            parseSelectList();
        }
        parseInto();
        parseFrom();
        parseWhere();
        skipHierarchicalQueryClause();
        parseGroupBy();
        skipModelClause();
        queryRest();
        return queryBlock;
    }
    
    private void parseInto() {
        if (getLexer().equalToken(Token.INTO)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }
    
    
    private void skipHierarchicalQueryClause() {
        skipConnect();
        skipStart();
        skipConnect();
    }
    
    private void skipStart() {
        if (getLexer().equalToken(Token.START)) {
            getLexer().nextToken();
            getLexer().accept(Token.WITH);
            getExprParser().expr();
        }
    }
    
    private void skipConnect() {
        if (getLexer().equalToken(Token.CONNECT)) {
            getLexer().nextToken();
            getLexer().accept(Token.BY);
            getLexer().skipIfEqual(Token.PRIOR);
            if (getLexer().identifierEquals("NOCYCLE")) {
                getLexer().nextToken();
                getLexer().skipIfEqual(Token.PRIOR);
            }
            getExprParser().expr();
        }
    }
    
    private void skipModelClause() {
        if (!getLexer().skipIfEqual(Token.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        getLexer().skipIfEqual(Token.RETURN);
        getLexer().skipIfEqual(Token.ALL);
        getLexer().skipIfEqual(Token.UPDATED);
        getLexer().skipIfEqual(Token.ROWS);
        while (getLexer().skipIfEqual(Token.REFERENCE)) {
            getExprParser().expr();
            getLexer().accept(Token.ON);
            getLexer().skipParentheses();
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause();
    }
    
    private void skipCellReferenceOptions() {
        if (getLexer().identifierEquals("IGNORE")) {
            getLexer().nextToken();
            getLexer().accept("NAV");
        } else if (getLexer().identifierEquals("KEEP")) {
            getLexer().nextToken();
            getLexer().accept("NAV");
        }
        if (getLexer().skipIfEqual(Token.UNIQUE)) {
            if (getLexer().identifierEquals("DIMENSION")) {
                getLexer().nextToken();
            } else {
                getLexer().accept("SINGLE");
                getLexer().accept("REFERENCE");
            }
        }
    }
    
    private void skipMainModelClause() {
        if (getLexer().identifierEquals("MAIN")) {
            getLexer().nextToken();
            getExprParser().expr();
        }
        skipQueryPartitionClause();
        getLexer().accept("DIMENSION");
        getLexer().accept(Token.BY);
        getLexer().skipParentheses();
        getLexer().accept("MEASURES");
        getLexer().skipParentheses();
        skipCellReferenceOptions();
        skipModelRulesClause();
    }

    private void skipModelRulesClause() {
        if (getLexer().identifierEquals("RULES")) {
            getLexer().nextToken();
            getLexer().skipIfEqual(Token.UPDATE);
            getLexer().skipIfEqual(Token.UPSERT);
            if (getLexer().identifierEquals("AUTOMATIC")) {
                getLexer().nextToken();
                getLexer().accept(Token.ORDER);
            } else if (getLexer().identifierEquals("SEQUENTIAL")) {
                getLexer().nextToken();
                getLexer().accept(Token.ORDER);
            }
        }
        if (getLexer().identifierEquals("ITERATE")) {
            getLexer().nextToken();
            getLexer().skipParentheses();
            if (getLexer().identifierEquals("UNTIL")) {
                getLexer().nextToken();
                getLexer().skipParentheses();
            }
        }
        getLexer().skipParentheses();
    }
    
    private void skipQueryPartitionClause() {
        if (getLexer().skipIfEqual(Token.PARTITION)) {
            getLexer().accept(Token.BY);
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                getLexer().skipParentheses();
            } else {
                throw new UnsupportedOperationException("Cannot support PARTITION BY without ()");
            }
        }
    }

    private void skipModelColumnClause() {
        throw new ParserUnsupportedException(getLexer().getToken());
    }
    
    @Override
    protected void parseGroupBy() {
        if (getLexer().equalToken(Token.GROUP)) {
            getLexer().nextToken();
            getLexer().accept(Token.BY);
            while (true) {
                if (getLexer().identifierEquals("GROUPING")) {
                    throw new UnsupportedOperationException("Cannot support GROUPING SETS");
                } 
                addGroupByItem(getExprParser().expr());
                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }
                getLexer().nextToken();
            }
            if (getLexer().equalToken(Token.HAVING)) {
                getLexer().nextToken();
                getExprParser().expr();
            }
        } else if (getLexer().equalToken(Token.HAVING)) {
            getLexer().nextToken();
            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            groupBy.setHaving(getExprParser().expr());

            if (getLexer().equalToken(Token.GROUP)) {
                getLexer().nextToken();
                getLexer().accept(Token.BY);
                while (true) {
                    if (getLexer().identifierEquals("GROUPING")) {
                        throw new UnsupportedOperationException("Cannot support GROUPING SETS");
                    }
                    addGroupByItem(getExprParser().expr());
                    if (!getLexer().equalToken(Token.COMMA)) {
                        break;
                    }
                    getLexer().nextToken();
                }
            }
        }
    }
    
    @Override
    protected Optional<String> as() {
        if (getLexer().equalToken(Token.CONNECT)) {
            return null;
        }
        return super.as();
    }
    
    @Override
    public final List<TableContext> parseTableSource() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getLexer().equalToken(Token.SELECT)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
        if (getLexer().identifierEquals("ONLY")) {
            getLexer().skipIfEqual(Token.LEFT_PAREN);
            parseQueryTableExpression();
            getLexer().skipIfEqual(Token.RIGHT_PAREN);
            parseFlashbackQueryClause();
        } else {
            parseQueryTableExpression();
            parsePivotClause();
            parseFlashbackQueryClause();
        }
        parseJoinTable();
        return getSqlContext().getTables();
    }
    
    private void parseQueryTableExpression() {
        parseTableFactor();
        parseSample();
        parsePartition();
    }
    
    private void parseSample() {
        if (getLexer().identifierEquals("SAMPLE")) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("BLOCK")) {
                getLexer().nextToken();
            }
            getLexer().skipParentheses();
            if (getLexer().identifierEquals("SEED")) {
                getLexer().skipParentheses();
            }
        }
    }
    
    private void parsePartition() {
        parsePartition(Token.PARTITION);
        parsePartition(Token.SUBPARTITION);
    }
    
    private void parsePartition(final Token token) {
        if (getLexer().skipIfEqual(token)) {
            getLexer().skipParentheses();
            if (getLexer().skipIfEqual(Token.FOR)) {
                getLexer().skipParentheses();
            }
        }
    }
    
    private void parsePivotClause() {
        if (getLexer().identifierEquals("PIVOT")) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("XML")) {
                getLexer().nextToken();
            }
            getLexer().skipParentheses();
        } else if (getLexer().identifierEquals("UNPIVOT")) {
            getLexer().nextToken();
            if (getLexer().identifierEquals("INCLUDE")) {
                getLexer().nextToken();
                getLexer().accept(Token.NULLS);
            } else if (getLexer().identifierEquals("EXCLUDE")) {
                getLexer().nextToken();
                getLexer().accept(Token.NULLS);
            }
            getLexer().skipParentheses();
        }
    }
    
    private void parseFlashbackQueryClause() {
        if (getLexer().identifierEquals("VERSIONS")) {
            getLexer().nextToken();
            if (getLexer().skipIfEqual(Token.BETWEEN)) {
                if (getLexer().identifierEquals("SCN") || getLexer().identifierEquals("TIMESTAMP")) {
                    getLexer().nextToken();
                }
                SQLBinaryOpExpr binaryExpr = (SQLBinaryOpExpr) getExprParser().expr();
                if (binaryExpr.getOperator() != SQLBinaryOperator.BooleanAnd) {
                    throw new ParserException("syntax error : " + binaryExpr.getOperator());
                }
            } else {
                throw new ParserUnsupportedException(getLexer().getToken());
            }
        } else if (getLexer().skipIfEqual(Token.AS)) {
            if (getLexer().skipIfEqual(Token.OF)) {
                if (getLexer().identifierEquals("SCN") || getLexer().identifierEquals("TIMESTAMP")) {
                    getLexer().nextToken();
                    getExprParser().expr();
                }
            }
        }
    }
    
    @Override
    protected void parseJoinTable() {
        getLexer().skipIfEqual(Token.HINT);
        if (isJoin()) {
            parseTableSource();
            if (getLexer().equalToken(Token.ON)) {
                getLexer().nextToken();
                getExprParser().expr();
            } else if (getLexer().skipIfEqual(Token.USING)) {
                getLexer().skipParentheses();
            }
            parseJoinTable();
        }
    }
    
    private void parseForUpdate() {
        getLexer().nextToken();
        getLexer().accept(Token.UPDATE);
        if (getLexer().equalToken(Token.OF)) {
            getLexer().nextToken();
            getExprParser().exprList(null);
        }
        if (getLexer().equalToken(Token.NOWAIT)) {
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.WAIT)) {
            getLexer().nextToken();
        } else if (getLexer().identifierEquals("SKIP")) {
            getLexer().nextToken();
            getLexer().accept("LOCKED");
        }
    }
}
