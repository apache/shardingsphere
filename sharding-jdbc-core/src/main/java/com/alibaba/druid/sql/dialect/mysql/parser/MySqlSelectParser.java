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
package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLLiteralExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlForceIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIgnoreIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIndexHintImpl;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUseIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;

public class MySqlSelectParser extends SQLSelectParser {
    
    public MySqlSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public SQLSelectQuery query() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelectQuery select = query();
            accept(Token.RIGHT_PAREN);
            return queryRest(select);
        }

        MySqlSelectQueryBlock queryBlock = new MySqlSelectQueryBlock();

        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.HINT)) {
                queryBlock.getHints().addAll(getExprParser().parseHints());
            }

            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.DISTINCT)) {
                queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
                getLexer().nextToken();
            } else if (getLexer().identifierEquals("DISTINCTROW")) {
                queryBlock.setDistionOption(SQLSetQuantifier.DISTINCTROW);
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.ALL)) {
                queryBlock.setDistionOption(SQLSetQuantifier.ALL);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("HIGH_PRIORITY")) {
                queryBlock.setHighPriority(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("STRAIGHT_JOIN")) {
                queryBlock.setStraightJoin(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("SQL_SMALL_RESULT")) {
                queryBlock.setSmallResult(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("SQL_BIG_RESULT")) {
                queryBlock.setBigResult(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("SQL_BUFFER_RESULT")) {
                queryBlock.setBufferResult(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("SQL_CACHE")) {
                queryBlock.setCache(true);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("SQL_NO_CACHE")) {
                queryBlock.setCache(false);
                getLexer().nextToken();
            }

            if (getLexer().identifierEquals("SQL_CALC_FOUND_ROWS")) {
                queryBlock.setCalcFoundRows(true);
                getLexer().nextToken();
            }

            parseSelectList(queryBlock);
            
            parseInto(queryBlock);
        }

        parseFrom(queryBlock);

        parseWhere(queryBlock);

        parseGroupBy(queryBlock);

        queryBlock.setOrderBy(getExprParser().parseOrderBy());

        if (getLexer().equalToken(Token.LIMIT)) {
            queryBlock.setLimit(parseLimit());
        }

        if (getLexer().equalToken(Token.PROCEDURE)) {
            getLexer().nextToken();
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        parseInto(queryBlock);

        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();
            accept(Token.UPDATE);

            queryBlock.setForUpdate(true);
        }

        if (getLexer().equalToken(Token.LOCK)) {
            getLexer().nextToken();
            accept(Token.IN);
            accept("SHARE");
            accept("MODE");
            queryBlock.setLockInShareMode(true);
        }

        return queryRest(queryBlock);
    }
    
    public SQLTableSource parseTableSource() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLTableSource tableSource;
            if (getLexer().equalToken(Token.SELECT) || getLexer().equalToken(Token.WITH)) {
                SQLSelect select = select();
                accept(Token.RIGHT_PAREN);
                SQLSelectQuery query = queryRest(select.getQuery());
                if (query instanceof SQLUnionQuery) {
                    tableSource = new SQLUnionQueryTableSource((SQLUnionQuery) query);
                } else {
                    tableSource = new SQLSubqueryTableSource(select);
                }
            } else if (getLexer().equalToken(Token.LEFT_PAREN)) {
                tableSource = parseTableSource();
                accept(Token.RIGHT_PAREN);
            } else {
                tableSource = parseTableSource();
                accept(Token.RIGHT_PAREN);
            }

            return parseTableSourceRest(tableSource);
        }
        
        if(getLexer().equalToken(Token.UPDATE)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        if (getLexer().equalToken(Token.SELECT)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        SQLExprTableSource tableReference = new SQLExprTableSource();

        parseTableSourceQueryTableExpr(tableReference);

        return parseTableSourceRest(tableReference);
    }
    
    protected void parseInto(SQLSelectQueryBlock queryBlock) {
        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("OUTFILE")) {
                getLexer().nextToken();

                MySqlOutFileExpr outFile = new MySqlOutFileExpr(getExprParser().expr());
                queryBlock.setInto(outFile);
                if (getLexer().identifierEquals("FIELDS") || getLexer().identifierEquals("COLUMNS")) {
                    getLexer().nextToken();

                    if (getLexer().identifierEquals("TERMINATED")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                    }
                    outFile.setColumnsTerminatedBy((SQLLiteralExpr) getExprParser().expr());

                    if (getLexer().identifierEquals("OPTIONALLY")) {
                        getLexer().nextToken();
                        outFile.setColumnsEnclosedOptionally(true);
                    }

                    if (getLexer().identifierEquals("ENCLOSED")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setColumnsEnclosedBy((SQLLiteralExpr) getExprParser().expr());
                    }

                    if (getLexer().identifierEquals("ESCAPED")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setColumnsEscaped((SQLLiteralExpr) getExprParser().expr());
                    }
                }

                if (getLexer().identifierEquals("LINES")) {
                    getLexer().nextToken();

                    if (getLexer().identifierEquals("STARTING")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setLinesStartingBy((SQLLiteralExpr) getExprParser().expr());
                    } else {
                        getLexer().identifierEquals("TERMINATED");
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setLinesTerminatedBy((SQLLiteralExpr) getExprParser().expr());
                    }
                }
            } else {
                queryBlock.setInto(getExprParser().name());
            }
        }
    }

    protected void parseGroupBy(SQLSelectQueryBlock queryBlock) {
        SQLSelectGroupByClause groupBy = null;

        if (getLexer().equalToken(Token.GROUP)) {
            groupBy = new SQLSelectGroupByClause();

            getLexer().nextToken();
            accept(Token.BY);

            while (true) {
                groupBy.addItem(((MySqlExprParser) getExprParser()).parseSelectGroupByItem());
                if (!getLexer().equalToken((Token.COMMA))) {
                    break;
                }
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.WITH)) {
                getLexer().nextToken();
                accept("ROLLUP");

                MySqlSelectGroupBy mySqlGroupBy = new MySqlSelectGroupBy();
                for (SQLExpr sqlExpr : groupBy.getItems()) {
                    mySqlGroupBy.addItem(sqlExpr);
                }
                mySqlGroupBy.setRollUp(true);

                groupBy = mySqlGroupBy;
            }
        }

        if (getLexer().equalToken(Token.HAVING)) {
            getLexer().nextToken();

            if (groupBy == null) {
                groupBy = new SQLSelectGroupByClause();
            }
            groupBy.setHaving(getExprParser().expr());
        }

        queryBlock.setGroupBy(groupBy);
    }

    protected SQLTableSource parseTableSourceRest(SQLTableSource tableSource) {
        if (getLexer().identifierEquals("USING")) {
            return tableSource;
        }

        if (getLexer().equalToken(Token.USE)) {
            getLexer().nextToken();
            MySqlUseIndexHint hint = new MySqlUseIndexHint();
            parseIndexHint(hint);
            tableSource.getHints().add(hint);
        }

        if (getLexer().identifierEquals("IGNORE")) {
            getLexer().nextToken();
            MySqlIgnoreIndexHint hint = new MySqlIgnoreIndexHint();
            parseIndexHint(hint);
            tableSource.getHints().add(hint);
        }

        if (getLexer().identifierEquals("FORCE")) {
            getLexer().nextToken();
            MySqlForceIndexHint hint = new MySqlForceIndexHint();
            parseIndexHint(hint);
            tableSource.getHints().add(hint);
        }

        return super.parseTableSourceRest(tableSource);
    }

    private void parseIndexHint(MySqlIndexHintImpl hint) {
        if (getLexer().equalToken(Token.INDEX)) {
            getLexer().nextToken();
        } else {
            accept(Token.KEY);
        }

        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();

            if (getLexer().equalToken(Token.JOIN)) {
                getLexer().nextToken();
                hint.setOption(MySqlIndexHint.Option.JOIN);
            } else if (getLexer().equalToken(Token.ORDER)) {
                getLexer().nextToken();
                accept(Token.BY);
                hint.setOption(MySqlIndexHint.Option.ORDER_BY);
            } else {
                accept(Token.GROUP);
                accept(Token.BY);
                hint.setOption(MySqlIndexHint.Option.GROUP_BY);
            }
        }

        accept(Token.LEFT_PAREN);
        if (getLexer().equalToken(Token.PRIMARY)) {
            getLexer().nextToken();
            hint.getIndexList().add(new SQLIdentifierExpr("PRIMARY"));
        } else {
            getExprParser().names(hint.getIndexList());
        }
        accept(Token.RIGHT_PAREN);
    }
    
    @Override
    protected MySqlUnionQuery createSQLUnionQuery() {
        return new MySqlUnionQuery();
    }

    public SQLUnionQuery unionRest(SQLUnionQuery union) {
        if (getLexer().equalToken(Token.LIMIT)) {
            MySqlUnionQuery mysqlUnionQuery = (MySqlUnionQuery) union;
            mysqlUnionQuery.setLimit(parseLimit());
        }
        return super.unionRest(union);
    }

    public Limit parseLimit() {
        return ((MySqlExprParser) getExprParser()).parseLimit();
    }
}
