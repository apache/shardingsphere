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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.context.TableContext;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.lexer.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.SpecialLiterals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.SQLExprParser;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;

import java.util.List;

public class OracleSelectParser extends AbstractSelectParser {
    
    public OracleSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void customizedSelect() {
        if (getExprParser().getLexer().equalToken(DefaultKeyword.FOR)) {
            skipForUpdate();
        }
        if (getSqlContext().getOrderByContexts().isEmpty()) {
            getSqlContext().getOrderByContexts().addAll(getExprParser().parseOrderBy(getSqlContext()));
        }
    }
    
    @Override
    public void query() {
        if (getExprParser().getLexer().equalToken(DefaultKeyword.SELECT)) {
            getExprParser().getLexer().nextToken();
            while (getExprParser().getLexer().equalToken(SpecialLiterals.HINT) || getExprParser().getLexer().equalToken(SpecialLiterals.COMMENT)) {
                getExprParser().getLexer().nextToken();
            }
            parseDistinct();
            getExprParser().getLexer().skipIfEqual(SpecialLiterals.HINT);
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
        if (getExprParser().getLexer().equalToken(DefaultKeyword.INTO)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
    }
    
    
    private void skipHierarchicalQueryClause() {
        skipConnect();
        skipStart();
        skipConnect();
    }
    
    private void skipStart() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.START)) {
            getExprParser().getLexer().accept(DefaultKeyword.WITH);
            getExprParser().parseComparisonCondition(getSqlContext(), new ParseContext(0));
        }
    }
    
    private void skipConnect() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.CONNECT)) {
            getExprParser().getLexer().accept(DefaultKeyword.BY);
            getExprParser().getLexer().skipIfEqual(OracleKeyword.PRIOR);
            if (getExprParser().getLexer().skipIfEqual(OracleKeyword.NOCYCLE)) {
                getExprParser().getLexer().skipIfEqual(OracleKeyword.PRIOR);
            }
            getExprParser().parseComparisonCondition(getSqlContext(), new ParseContext(1));
        }
    }
    
    private void skipModelClause() {
        if (!getExprParser().getLexer().skipIfEqual(OracleKeyword.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        getExprParser().getLexer().skipIfEqual(OracleKeyword.RETURN);
        getExprParser().getLexer().skipIfEqual(DefaultKeyword.ALL);
        getExprParser().getLexer().skipIfEqual(OracleKeyword.UPDATED);
        getExprParser().getLexer().skipIfEqual(OracleKeyword.ROWS);
        while (getExprParser().getLexer().skipIfEqual(OracleKeyword.REFERENCE)) {
            getExprParser().getLexer().nextToken();
            getExprParser().getLexer().accept(DefaultKeyword.ON);
            getExprParser().getLexer().skipParentheses();
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause();
    }
    
    private void skipCellReferenceOptions() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.IGNORE)) {
            getExprParser().getLexer().accept(OracleKeyword.NAV);
        } else if (getExprParser().getLexer().skipIfEqual(OracleKeyword.KEEP)) {
            getExprParser().getLexer().accept(OracleKeyword.NAV);
        }
        if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.UNIQUE)) {
            getExprParser().getLexer().skipIfEqual(OracleKeyword.DIMENSION, OracleKeyword.SINGLE);
            getExprParser().getLexer().skipIfEqual(OracleKeyword.REFERENCE);
        }
    }
    
    private void skipMainModelClause() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.MAIN)) {
            getExprParser().getLexer().nextToken();
        }
        skipQueryPartitionClause();
        getExprParser().getLexer().accept(OracleKeyword.DIMENSION);
        getExprParser().getLexer().accept(DefaultKeyword.BY);
        getExprParser().getLexer().skipParentheses();
        getExprParser().getLexer().accept(OracleKeyword.MEASURES);
        getExprParser().getLexer().skipParentheses();
        skipCellReferenceOptions();
        skipModelRulesClause();
    }

    private void skipModelRulesClause() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.RULES)) {
            getExprParser().getLexer().skipIfEqual(DefaultKeyword.UPDATE);
            getExprParser().getLexer().skipIfEqual(OracleKeyword.UPSERT);
            if (getExprParser().getLexer().skipIfEqual(OracleKeyword.AUTOMATIC)) {
                getExprParser().getLexer().accept(DefaultKeyword.ORDER);
            } else if (getExprParser().getLexer().skipIfEqual(OracleKeyword.SEQUENTIAL)) {
                getExprParser().getLexer().accept(DefaultKeyword.ORDER);
            }
        }
        if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.ITERATE)) {
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.UNTIL)) {
                getExprParser().getLexer().skipParentheses();
            }
        }
        getExprParser().getLexer().skipParentheses();
    }
    
    private void skipQueryPartitionClause() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.PARTITION)) {
            getExprParser().getLexer().accept(DefaultKeyword.BY);
            if (getExprParser().getLexer().equalToken(Symbol.LEFT_PAREN)) {
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
//        if (getExprParser().getLexer().equalToken(DefaultKeyword.GROUP)) {
//            getExprParser().getLexer().nextToken();
//            getExprParser().getLexer().accept(DefaultKeyword.BY);
//            while (true) {
//                if (getExprParser().getLexer().identifierEquals("GROUPING")) {
//                    throw new UnsupportedOperationException("Cannot support GROUPING SETS");
//                } 
//                addGroupByItem(getExprParser().expr());
//                if (!getExprParser().getLexer().equalToken(Symbol.COMMA)) {
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
//            if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.GROUP)) {
//                getExprParser().getLexer().accept(DefaultKeyword.BY);
//                while (true) {
//                    if (getExprParser().getLexer().identifierEquals("GROUPING")) {
//                        throw new UnsupportedOperationException("Cannot support GROUPING SETS");
//                    }
//                    addGroupByItem(getExprParser().expr());
//                    if (!getExprParser().getLexer().equalToken(Symbol.COMMA)) {
//                        break;
//                    }
//                    getExprParser().getLexer().nextToken();
//                }
//            }
//        }
    }
    
    @Override
    public final List<TableContext> parseTable() {
        if (getExprParser().getLexer().equalToken(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getExprParser().getLexer().equalToken(DefaultKeyword.SELECT)) {
            throw new ParserUnsupportedException(getExprParser().getLexer().getToken());
        }
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.ONLY)) {
            getExprParser().getLexer().skipIfEqual(Symbol.LEFT_PAREN);
            parseQueryTableExpression();
            getExprParser().getLexer().skipIfEqual(Symbol.RIGHT_PAREN);
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
        skipPartition();
    }
    
    private void parseSample() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.SAMPLE)) {
            getExprParser().getLexer().skipIfEqual(OracleKeyword.BLOCK);
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().skipIfEqual(OracleKeyword.SEED)) {
                getExprParser().getLexer().skipParentheses();
            }
        }
    }
    
    private void skipPartition() {
        skipPartition(OracleKeyword.PARTITION);
        skipPartition(OracleKeyword.SUBPARTITION);
    }
    
    private void skipPartition(final OracleKeyword keyword) {
        if (getExprParser().getLexer().skipIfEqual(keyword)) {
            getExprParser().getLexer().skipParentheses();
            if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.FOR)) {
                getExprParser().getLexer().skipParentheses();
            }
        }
    }
    
    private void skipPivotClause() {
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.PIVOT)) {
            getExprParser().getLexer().skipIfEqual(OracleKeyword.XML);
            getExprParser().getLexer().skipParentheses();
        } else if (getExprParser().getLexer().skipIfEqual(OracleKeyword.UNPIVOT)) {
            if (getExprParser().getLexer().skipIfEqual(OracleKeyword.INCLUDE)) {
                getExprParser().getLexer().accept(OracleKeyword.NULLS);
            } else if (getExprParser().getLexer().skipIfEqual(OracleKeyword.EXCLUDE)) {
                getExprParser().getLexer().accept(OracleKeyword.NULLS);
            }
            getExprParser().getLexer().skipParentheses();
        }
    }
    
    private void skipFlashbackQueryClause() {
        if (getExprParser().getLexer().equalToken(OracleKeyword.VERSIONS)) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        } else if (getExprParser().getLexer().skipIfEqual(DefaultKeyword.AS)) {
            if (getExprParser().getLexer().skipIfEqual(OracleKeyword.OF)) {
                if (getExprParser().getLexer().skipIfEqual(OracleKeyword.SCN) || getExprParser().getLexer().skipIfEqual(OracleKeyword.TIMESTAMP)) {
                    throw new UnsupportedOperationException("Cannot support Flashback Query");
                }
            }
        }
    }
    
    private void skipForUpdate() {
        getExprParser().getLexer().nextToken();
        getExprParser().getLexer().accept(DefaultKeyword.UPDATE);
        if (getExprParser().getLexer().skipIfEqual(OracleKeyword.OF)) {
            do {
                getExprParser().parseExpr();
            } while (getExprParser().getLexer().skipIfEqual(Symbol.COMMA));
        }
        if (getExprParser().getLexer().equalToken(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            getExprParser().getLexer().nextToken();
        } else if (getExprParser().getLexer().skipIfEqual(OracleKeyword.SKIP)) {
            getExprParser().getLexer().accept(OracleKeyword.LOCKED);
        }
    }
}
