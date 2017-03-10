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

import com.alibaba.druid.sql.context.TableContext;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractSelectParser;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;

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
            getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy(getSqlContext()));
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
        if (getExprParser().getLexer().skipIfEqual(Token.START)) {
            getExprParser().getLexer().accept(Token.WITH);
            getExprParser().parseComparisonCondition(getSqlContext(), new ParseContext(0));
        }
    }
    
    private void skipConnect() {
        if (getExprParser().getLexer().skipIfEqual(Token.CONNECT)) {
            getExprParser().getLexer().accept(Token.BY);
            getExprParser().getLexer().skipIfEqual(Token.PRIOR);
            if (getExprParser().getLexer().skipIfEqual(Token.NOCYCLE)) {
                getExprParser().getLexer().skipIfEqual(Token.PRIOR);
            }
            getExprParser().parseComparisonCondition(getSqlContext(), new ParseContext(1));
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
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept(Token.ON);
            getExprParser().getLexer().skipParentheses();
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause();
    }
    
    private void skipCellReferenceOptions() {
        if (getExprParser().getLexer().skipIfEqual(Token.IGNORE)) {
            getExprParser().getLexer().accept(Token.NAV);
        } else if (getExprParser().getLexer().skipIfEqual(Token.KEEP)) {
            getExprParser().getLexer().accept(Token.NAV);
        }
        if (getExprParser().getLexer().skipIfEqual(Token.UNIQUE)) {
            getExprParser().getLexer().skipIfEqual(Token.DIMENSION, Token.SINGLE);
            getExprParser().getLexer().skipIfEqual(Token.REFERENCE);
        }
    }
    
    private void skipMainModelClause() {
        if (getExprParser().getLexer().skipIfEqual(Token.MAIN)) {
            getExprParser().getLexer().nextToken();
        }
        skipQueryPartitionClause();
        getExprParser().getLexer().accept(Token.DIMENSION);
        getExprParser().getLexer().accept(Token.BY);
        getExprParser().getLexer().skipParentheses();
        getExprParser().getLexer().accept(Token.MEASURES);
        getExprParser().getLexer().skipParentheses();
        skipCellReferenceOptions();
        skipModelRulesClause();
    }

    private void skipModelRulesClause() {
        if (getExprParser().getLexer().skipIfEqual(Token.RULES)) {
            getExprParser().getLexer().skipIfEqual(Token.UPDATE);
            getExprParser().getLexer().skipIfEqual(Token.UPSERT);
            if (getExprParser().getLexer().skipIfEqual(Token.AUTOMATIC)) {
                getExprParser().getLexer().accept(Token.ORDER);
            } else if (getExprParser().getLexer().skipIfEqual(Token.SEQUENTIAL)) {
                getExprParser().getLexer().accept(Token.ORDER);
            }
        }
        if (getExprParser().getLexer().skipIfEqual(Token.ITERATE)) {
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().skipIfEqual(Token.UNTIL)) {
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
        // TODO
//        if (getExprParser().getLexer().equalToken(Token.GROUP)) {
//            getExprParser().getLexer().nextToken();
//            getExprParser().getLexer().accept(Token.BY);
//            while (true) {
//                if (getExprParser().getLexer().identifierEquals("GROUPING")) {
//                    throw new UnsupportedOperationException("Cannot support GROUPING SETS");
//                } 
//                addGroupByItem(getExprParser().expr());
//                if (!getExprParser().getLexer().equalToken(Token.COMMA)) {
//                    break;
//                }
//                getExprParser().getLexer().nextToken();
//            }
//            if (getExprParser().getLexer().skipIfEqual(Token.HAVING)) {
//                getExprParser().expr();
//            }
//        } else if (getExprParser().getLexer().skipIfEqual(Token.HAVING)) {
//            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
//            groupBy.setHaving(getExprParser().expr());
//            if (getExprParser().getLexer().skipIfEqual(Token.GROUP)) {
//                getExprParser().getLexer().accept(Token.BY);
//                while (true) {
//                    if (getExprParser().getLexer().identifierEquals("GROUPING")) {
//                        throw new UnsupportedOperationException("Cannot support GROUPING SETS");
//                    }
//                    addGroupByItem(getExprParser().expr());
//                    if (!getExprParser().getLexer().equalToken(Token.COMMA)) {
//                        break;
//                    }
//                    getExprParser().getLexer().nextToken();
//                }
//            }
//        }
    }
    
    @Override
    public final List<TableContext> parseTable() {
        if (getExprParser().getLexer().equalToken(Token.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getExprParser().getLexer().equalToken(Token.SELECT)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
        if (getExprParser().getLexer().skipIfEqual(Token.ONLY)) {
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
        if (getExprParser().getLexer().skipIfEqual(Token.SAMPLE)) {
            getExprParser().getLexer().skipIfEqual(Token.BLOCK);
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().skipIfEqual(Token.SEED)) {
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
        if (getExprParser().getLexer().skipIfEqual(Token.PIVOT)) {
            getExprParser().getLexer().skipIfEqual(Token.XML);
            getExprParser().getLexer().skipParentheses();
        } else if (getExprParser().getLexer().skipIfEqual(Token.UNPIVOT)) {
            if (getExprParser().getLexer().skipIfEqual(Token.INCLUDE)) {
                getExprParser().getLexer().accept(Token.NULLS);
            } else if (getExprParser().getLexer().skipIfEqual(Token.EXCLUDE)) {
                getExprParser().getLexer().accept(Token.NULLS);
            }
            getExprParser().getLexer().skipParentheses();
        }
    }
    
    private void skipFlashbackQueryClause() {
        if (getExprParser().getLexer().equalToken(Token.VERSIONS)) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        } else if (getExprParser().getLexer().skipIfEqual(Token.AS)) {
            if (getExprParser().getLexer().skipIfEqual(Token.OF)) {
                if (getExprParser().getLexer().skipIfEqual(Token.SCN) || getExprParser().getLexer().skipIfEqual(Token.TIMESTAMP)) {
                    throw new UnsupportedOperationException("Cannot support Flashback Query");
                }
            }
        }
    }
    
    private void skipForUpdate() {
        getExprParser().getLexer().nextToken();
        getExprParser().getLexer().accept(Token.UPDATE);
        if (getExprParser().getLexer().skipIfEqual(Token.OF)) {
            do {
                getExprParser().parseExpr();
            } while (getExprParser().getLexer().skipIfEqual(Token.COMMA));
        }
        if (getExprParser().getLexer().equalToken(Token.NOWAIT, Token.WAIT)) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().getLexer().skipIfEqual(Token.SKIP)) {
            getExprParser().getLexer().accept(Token.LOCKED);
        }
    }
}
