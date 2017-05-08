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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderByContext;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SelectSQLContext;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.SQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.AbstractSelectParser;
import com.google.common.base.Optional;

public class OracleSelectParser extends AbstractSelectParser {
    
    public OracleSelectParser(final SQLParser sqlParser) {
        super(sqlParser);
    }
    
    @Override
    protected void customizedSelect() {
        if (getSqlParser().equalAny(DefaultKeyword.FOR)) {
            skipForUpdate();
        }
        if (getSqlContext().getOrderByContexts().isEmpty()) {
            getSqlContext().getOrderByContexts().addAll(parseOrderBy(getSqlContext()));
        }
    }
    
    @Override
    public void query() {
        if (getSqlParser().equalAny(DefaultKeyword.SELECT)) {
            getSqlParser().getLexer().nextToken();
            parseDistinct();
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
        if (getSqlParser().equalAny(DefaultKeyword.INTO)) {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
    }
    
    
    private void skipHierarchicalQueryClause() {
        skipConnect();
        skipStart();
        skipConnect();
    }
    
    private void skipStart() {
        if (getSqlParser().skipIfEqual(OracleKeyword.START)) {
            getSqlParser().accept(DefaultKeyword.WITH);
            getSqlParser().parseComparisonCondition(getSqlContext());
        }
    }
    
    private void skipConnect() {
        if (getSqlParser().skipIfEqual(OracleKeyword.CONNECT)) {
            getSqlParser().accept(DefaultKeyword.BY);
            getSqlParser().skipIfEqual(OracleKeyword.PRIOR);
            if (getSqlParser().skipIfEqual(OracleKeyword.NOCYCLE)) {
                getSqlParser().skipIfEqual(OracleKeyword.PRIOR);
            }
            getSqlParser().parseComparisonCondition(getSqlContext());
        }
    }
    
    private void skipModelClause() {
        if (!getSqlParser().skipIfEqual(OracleKeyword.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        getSqlParser().skipIfEqual(OracleKeyword.RETURN);
        getSqlParser().skipIfEqual(DefaultKeyword.ALL);
        getSqlParser().skipIfEqual(OracleKeyword.UPDATED);
        getSqlParser().skipIfEqual(OracleKeyword.ROWS);
        while (getSqlParser().skipIfEqual(OracleKeyword.REFERENCE)) {
            getSqlParser().getLexer().nextToken();
            getSqlParser().accept(DefaultKeyword.ON);
            getSqlParser().skipParentheses();
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause();
    }
    
    private void skipCellReferenceOptions() {
        if (getSqlParser().skipIfEqual(OracleKeyword.IGNORE)) {
            getSqlParser().accept(OracleKeyword.NAV);
        } else if (getSqlParser().skipIfEqual(OracleKeyword.KEEP)) {
            getSqlParser().accept(OracleKeyword.NAV);
        }
        if (getSqlParser().skipIfEqual(DefaultKeyword.UNIQUE)) {
            getSqlParser().skipIfEqual(OracleKeyword.DIMENSION, OracleKeyword.SINGLE);
            getSqlParser().skipIfEqual(OracleKeyword.REFERENCE);
        }
    }
    
    private void skipMainModelClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.MAIN)) {
            getSqlParser().getLexer().nextToken();
        }
        skipQueryPartitionClause();
        getSqlParser().accept(OracleKeyword.DIMENSION);
        getSqlParser().accept(DefaultKeyword.BY);
        getSqlParser().skipParentheses();
        getSqlParser().accept(OracleKeyword.MEASURES);
        getSqlParser().skipParentheses();
        skipCellReferenceOptions();
        skipModelRulesClause();
    }

    private void skipModelRulesClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.RULES)) {
            getSqlParser().skipIfEqual(DefaultKeyword.UPDATE);
            getSqlParser().skipIfEqual(OracleKeyword.UPSERT);
            if (getSqlParser().skipIfEqual(OracleKeyword.AUTOMATIC)) {
                getSqlParser().accept(DefaultKeyword.ORDER);
            } else if (getSqlParser().skipIfEqual(OracleKeyword.SEQUENTIAL)) {
                getSqlParser().accept(DefaultKeyword.ORDER);
            }
        }
        if (getSqlParser().skipIfEqual(DefaultKeyword.ITERATE)) {
            getSqlParser().skipParentheses();
            if (getSqlParser().skipIfEqual(DefaultKeyword.UNTIL)) {
                getSqlParser().skipParentheses();
            }
        }
        getSqlParser().skipParentheses();
    }
    
    private void skipQueryPartitionClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.PARTITION)) {
            getSqlParser().accept(DefaultKeyword.BY);
            if (getSqlParser().equalAny(Symbol.LEFT_PAREN)) {
                getSqlParser().skipParentheses();
            } else {
                throw new UnsupportedOperationException("Cannot support PARTITION BY without ()");
            }
        }
    }

    private void skipModelColumnClause() {
        throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
    }
    
    @Override
    protected void parseGroupBy() {
        // TODO
//        if (getSqlParser().equalAny(DefaultKeyword.GROUP)) {
//            getSqlParser().getLexer().nextToken();
//            getSqlParser().accept(DefaultKeyword.BY);
//            while (true) {
//                if (getSqlParser().getLexer().identifierEquals("GROUPING")) {
//                    throw new UnsupportedOperationException("Cannot support GROUPING SETS");
//                } 
//                addGroupByItem(getSqlParser().expr());
//                if (!getSqlParser().equalAny(Symbol.COMMA)) {
//                    break;
//                }
//                getSqlParser().getLexer().nextToken();
//            }
//            if (getSqlParser().skipIfEqual(Token.HAVING)) {
//                getSqlParser().expr();
//            }
//        } else if (getSqlParser().skipIfEqual(Token.HAVING)) {
//            SQLSelectGroupByClause groupBy = new SQLSelectGroupByClause();
//            groupBy.setHaving(getSqlParser().expr());
//            if (getSqlParser().skipIfEqual(DefaultKeyword.GROUP)) {
//                getSqlParser().accept(DefaultKeyword.BY);
//                while (true) {
//                    if (getSqlParser().getLexer().identifierEquals("GROUPING")) {
//                        throw new UnsupportedOperationException("Cannot support GROUPING SETS");
//                    }
//                    addGroupByItem(getSqlParser().expr());
//                    if (!getSqlParser().equalAny(Symbol.COMMA)) {
//                        break;
//                    }
//                    getSqlParser().getLexer().nextToken();
//                }
//            }
//        }
    }
    
    @Override
    public final void parseTable() {
        if (getSqlParser().equalAny(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support subquery");
        }
        if (getSqlParser().equalAny(DefaultKeyword.SELECT)) {
            throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
        }
        if (getSqlParser().skipIfEqual(OracleKeyword.ONLY)) {
            getSqlParser().skipIfEqual(Symbol.LEFT_PAREN);
            parseQueryTableExpression();
            getSqlParser().skipIfEqual(Symbol.RIGHT_PAREN);
            skipFlashbackQueryClause();
        } else {
            parseQueryTableExpression();
            skipPivotClause();
            skipFlashbackQueryClause();
        }
        parseJoinTable();
    }
    
    private void parseQueryTableExpression() {
        parseTableFactor();
        parseSample();
        skipPartition();
    }
    
    private void parseSample() {
        if (getSqlParser().skipIfEqual(OracleKeyword.SAMPLE)) {
            getSqlParser().skipIfEqual(OracleKeyword.BLOCK);
            getSqlParser().skipParentheses();
            if (getSqlParser().skipIfEqual(OracleKeyword.SEED)) {
                getSqlParser().skipParentheses();
            }
        }
    }
    
    private void skipPartition() {
        skipPartition(OracleKeyword.PARTITION);
        skipPartition(OracleKeyword.SUBPARTITION);
    }
    
    private void skipPartition(final OracleKeyword keyword) {
        if (getSqlParser().skipIfEqual(keyword)) {
            getSqlParser().skipParentheses();
            if (getSqlParser().skipIfEqual(DefaultKeyword.FOR)) {
                getSqlParser().skipParentheses();
            }
        }
    }
    
    private void skipPivotClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.PIVOT)) {
            getSqlParser().skipIfEqual(OracleKeyword.XML);
            getSqlParser().skipParentheses();
        } else if (getSqlParser().skipIfEqual(OracleKeyword.UNPIVOT)) {
            if (getSqlParser().skipIfEqual(OracleKeyword.INCLUDE)) {
                getSqlParser().accept(OracleKeyword.NULLS);
            } else if (getSqlParser().skipIfEqual(OracleKeyword.EXCLUDE)) {
                getSqlParser().accept(OracleKeyword.NULLS);
            }
            getSqlParser().skipParentheses();
        }
    }
    
    private void skipFlashbackQueryClause() {
        if (getSqlParser().equalAny(OracleKeyword.VERSIONS)) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        } else if (getSqlParser().skipIfEqual(DefaultKeyword.AS)) {
            if (getSqlParser().skipIfEqual(OracleKeyword.OF)) {
                if (getSqlParser().skipIfEqual(OracleKeyword.SCN) || getSqlParser().skipIfEqual(OracleKeyword.TIMESTAMP)) {
                    throw new UnsupportedOperationException("Cannot support Flashback Query");
                }
            }
        }
    }
    
    private void skipForUpdate() {
        getSqlParser().getLexer().nextToken();
        getSqlParser().accept(DefaultKeyword.UPDATE);
        if (getSqlParser().skipIfEqual(OracleKeyword.OF)) {
            do {
                getSqlParser().parseExpression();
            } while (getSqlParser().skipIfEqual(Symbol.COMMA));
        }
        if (getSqlParser().equalAny(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            getSqlParser().getLexer().nextToken();
        } else if (getSqlParser().skipIfEqual(OracleKeyword.SKIP)) {
            getSqlParser().accept(OracleKeyword.LOCKED);
        }
    }
    
    @Override
    protected Optional<OrderByContext> parseSelectOrderByItem(final SelectSQLContext sqlContext) {
        Optional<OrderByContext> result = super.parseSelectOrderByItem(sqlContext);
        skipAfterOrderByItem();
        return result;
    }
    
    private void skipAfterOrderByItem() {
        if (getSqlParser().skipIfEqual(OracleKeyword.NULLS)) {
            getSqlParser().getLexer().nextToken();
            if (!getSqlParser().skipIfEqual(OracleKeyword.FIRST, OracleKeyword.LAST)) {
                throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
            }
        }
    }
}
