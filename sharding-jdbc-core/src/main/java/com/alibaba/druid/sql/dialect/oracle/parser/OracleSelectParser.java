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

import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractSelectParser;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;

import java.util.List;

public class OracleSelectParser extends AbstractSelectParser {
    
    public OracleSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void customizedSelect() {
        if (getExprParser().getLexer().equalToken(Token.FOR)) {
            skipForUpdate();
        }
        if (getSqlContext().getOrderByContexts().isEmpty()) {
            getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy());
        }
    }
    
    @Override
    public void query() {
        if (getExprParser().getLexer().equalToken(Token.SELECT)) {
            getExprParser().getLexer().nextToken();
            while (getExprParser().getLexer().equalToken(Token.HINT) || getExprParser().getLexer().equalToken(Token.COMMENT)) {
                getExprParser().getLexer().nextToken();
            }
            parseDistinct();
            getExprParser().getLexer().skipIfEqual(Token.HINT);
            parseSelectList();
        }
        skipInto();
        parseFrom();
        parseWhere();
        skipHierarchicalQueryClause();
        parseGroupBy();
        skipModelClause();
        queryRest();
    }
    
    private void skipInto() {
        if (getExprParser().getLexer().equalToken(Token.INTO)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
    }
    
    
    private void skipHierarchicalQueryClause() {
        skipConnect();
        skipStart();
        skipConnect();
    }
    
    private void skipStart() {
        if (getExprParser().getLexer().equalToken(Token.START)) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept(Token.WITH);
            getExprParser().expr();
        }
    }
    
    private void skipConnect() {
        if (getExprParser().getLexer().equalToken(Token.CONNECT)) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept(Token.BY);
            getExprParser().getLexer().skipIfEqual(Token.PRIOR);
            if (getExprParser().getLexer().identifierEquals("NOCYCLE")) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().skipIfEqual(Token.PRIOR);
            }
            getExprParser().expr();
        }
    }
    
    private void skipModelClause() {
        if (!getExprParser().getLexer().skipIfEqual(Token.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        getExprParser().getLexer().skipIfEqual(Token.RETURN);
        getExprParser().getLexer().skipIfEqual(Token.ALL);
        getExprParser().getLexer().skipIfEqual(Token.UPDATED);
        getExprParser().getLexer().skipIfEqual(Token.ROWS);
        while (getExprParser().getLexer().skipIfEqual(Token.REFERENCE)) {
            getExprParser().expr();
            getExprParser().getLexer().accept(Token.ON);
            getExprParser().getLexer().skipParentheses();
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause();
    }
    
    private void skipCellReferenceOptions() {
        if (getExprParser().getLexer().identifierEquals("IGNORE")) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept("NAV");
        } else if (getExprParser().getLexer().identifierEquals("KEEP")) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept("NAV");
        }
        if (getExprParser().getLexer().skipIfEqual(Token.UNIQUE)) {
            if (getExprParser().getLexer().identifierEquals("DIMENSION")) {
                getExprParser().getLexer().nextToken();
            } else {
                getExprParser().getLexer().accept("SINGLE");
                getExprParser().getLexer().accept("REFERENCE");
            }
        }
    }
    
    private void skipMainModelClause() {
        if (getExprParser().getLexer().identifierEquals("MAIN")) {
            getExprParser().getLexer().nextToken();
            getExprParser().expr();
        }
        skipQueryPartitionClause();
        getExprParser().getLexer().accept("DIMENSION");
        getExprParser().getLexer().accept(Token.BY);
        getExprParser().getLexer().skipParentheses();
        getExprParser().getLexer().accept("MEASURES");
        getExprParser().getLexer().skipParentheses();
        skipCellReferenceOptions();
        skipModelRulesClause();
    }

    private void skipModelRulesClause() {
        if (getExprParser().getLexer().identifierEquals("RULES")) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().skipIfEqual(Token.UPDATE);
            getExprParser().getLexer().skipIfEqual(Token.UPSERT);
            if (getExprParser().getLexer().identifierEquals("AUTOMATIC")) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().accept(Token.ORDER);
            } else if (getExprParser().getLexer().identifierEquals("SEQUENTIAL")) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().accept(Token.ORDER);
            }
        }
        if (getExprParser().getLexer().identifierEquals("ITERATE")) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().identifierEquals("UNTIL")) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().skipParentheses();
            }
        }
        getExprParser().getLexer().skipParentheses();
    }
    
    private void skipQueryPartitionClause() {
        if (getExprParser().getLexer().skipIfEqual(Token.PARTITION)) {
            getExprParser().getLexer().accept(Token.BY);
            if (getExprParser().getLexer().equalToken(Token.LEFT_PAREN)) {
                getExprParser().getLexer().skipParentheses();
            } else {
                throw new UnsupportedOperationException("Cannot support PARTITION BY without ()");
            }
        }
    }

    private void skipModelColumnClause() {
        throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
    }
    
    @Override
    protected void parseGroupBy() {
        if (getExprParser().getLexer().equalToken(Token.GROUP)) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept(Token.BY);
            while (true) {
                if (getExprParser().getLexer().identifierEquals("GROUPING")) {
                    throw new UnsupportedOperationException("Cannot support GROUPING SETS");
                } 
                addGroupByItem(getExprParser().expr());
                if (!getExprParser().getLexer().equalToken(Token.COMMA)) {
                    break;
                }
                getExprParser().getLexer().nextToken();
            }
            if (getExprParser().getLexer().skipIfEqual(Token.HAVING)) {
                getExprParser().expr();
            }
        } else if (getExprParser().getLexer().skipIfEqual(Token.HAVING)) {
            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
            groupBy.setHaving(getExprParser().expr());
            if (getExprParser().getLexer().skipIfEqual(Token.GROUP)) {
                getExprParser().getLexer().accept(Token.BY);
                while (true) {
                    if (getExprParser().getLexer().identifierEquals("GROUPING")) {
                        throw new UnsupportedOperationException("Cannot support GROUPING SETS");
                    }
                    addGroupByItem(getExprParser().expr());
                    if (!getExprParser().getLexer().equalToken(Token.COMMA)) {
                        break;
                    }
                    getExprParser().getLexer().nextToken();
                }
            }
        }
    }
    
    @Override
    public final List<TableContext> parseTableSource() {
        if (getExprParser().getLexer().equalToken(Token.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getExprParser().getLexer().equalToken(Token.SELECT)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
        if (getExprParser().getLexer().identifierEquals("ONLY")) {
            getExprParser().getLexer().skipIfEqual(Token.LEFT_PAREN);
            parseQueryTableExpression();
            getExprParser().getLexer().skipIfEqual(Token.RIGHT_PAREN);
            skipFlashbackQueryClause();
        } else {
            parseQueryTableExpression();
            skipPivotClause();
            skipFlashbackQueryClause();
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
        if (getExprParser().getLexer().identifierEquals("SAMPLE")) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().identifierEquals("BLOCK")) {
                getExprParser().getLexer().nextToken();
            }
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().identifierEquals("SEED")) {
                getExprParser().getLexer().skipParentheses();
            }
        }
    }
    
    private void parsePartition() {
        parsePartition(Token.PARTITION);
        parsePartition(Token.SUBPARTITION);
    }
    
    private void parsePartition(final Token token) {
        if (getExprParser().getLexer().skipIfEqual(token)) {
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().skipIfEqual(Token.FOR)) {
                getExprParser().getLexer().skipParentheses();
            }
        }
    }
    
    private void skipPivotClause() {
        if (getExprParser().getLexer().identifierEquals("PIVOT")) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().identifierEquals("XML")) {
                getExprParser().getLexer().nextToken();
            }
            getExprParser().getLexer().skipParentheses();
        } else if (getExprParser().getLexer().identifierEquals("UNPIVOT")) {
            getExprParser().getLexer().nextToken();
            if (getExprParser().getLexer().identifierEquals("INCLUDE")) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().accept(Token.NULLS);
            } else if (getExprParser().getLexer().identifierEquals("EXCLUDE")) {
                getExprParser().getLexer().nextToken();
                getExprParser().getLexer().accept(Token.NULLS);
            }
            getExprParser().getLexer().skipParentheses();
        }
    }
    
    private void skipFlashbackQueryClause() {
        if (getExprParser().getLexer().identifierEquals("VERSIONS")) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        } else if (getExprParser().getLexer().skipIfEqual(Token.AS)) {
            if (getExprParser().getLexer().skipIfEqual(Token.OF)) {
                if (getExprParser().getLexer().identifierEquals("SCN") || getExprParser().getLexer().identifierEquals("TIMESTAMP")) {
                    throw new UnsupportedOperationException("Cannot support Flashback Query");
                }
            }
        }
    }
    
    @Override
    protected void parseJoinTable() {
        getExprParser().getLexer().skipIfEqual(Token.HINT);
        if (getExprParser().isJoin()) {
            parseTableSource();
            if (getExprParser().getLexer().equalToken(Token.ON)) {
                getExprParser().getLexer().nextToken();
                getExprParser().expr();
            } else if (getExprParser().getLexer().skipIfEqual(Token.USING)) {
                getExprParser().getLexer().skipParentheses();
            }
            parseJoinTable();
        }
    }
    
    private void skipForUpdate() {
        getExprParser().getLexer().nextToken();
        getExprParser().getLexer().accept(Token.UPDATE);
        if (getExprParser().getLexer().equalToken(Token.OF)) {
            getExprParser().getLexer().nextToken();
            getExprParser().exprList(null);
        }
        if (getExprParser().getLexer().equalToken(Token.NOWAIT)) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().getLexer().equalToken(Token.WAIT)) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().getLexer().identifierEquals("SKIP")) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept("LOCKED");
        }
    }
}
