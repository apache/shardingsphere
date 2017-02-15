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
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;

import java.util.ArrayList;

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
                getLexer().nextToken();
            }
            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }
            parseDistinct(queryBlock);
            while (getLexer().equalToken(Token.HIGH_PRIORITY) || getLexer().equalToken(Token.STRAIGHT_JOIN) || getLexer().equalToken(Token.SQL_SMALL_RESULT)
                    || getLexer().equalToken(Token.SQL_BIG_RESULT) || getLexer().equalToken(Token.SQL_BUFFER_RESULT) || getLexer().equalToken(Token.SQL_CACHE)
                    || getLexer().equalToken(Token.SQL_NO_CACHE) || getLexer().equalToken(Token.SQL_CALC_FOUND_ROWS)) {
                getLexer().nextToken();
            }
            parseSelectList(queryBlock);
            skipToFrom();
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
        return queryRest(queryBlock);
    }
    
    private void skipToFrom() {
        if (getLexer().equalToken(Token.INTO)) {
            while (!getLexer().equalToken(Token.FROM) || !getLexer().equalToken(Token.EOF)) {
                getLexer().nextToken();
            }
        }
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
        
        if (getLexer().equalToken(Token.UPDATE)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        if (getLexer().equalToken(Token.SELECT)) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }

        SQLExprTableSource tableReference = new SQLExprTableSource();

        parseTableSourceQueryTableExpr(tableReference);

        return parseTableSourceRest(tableReference);
    }
    
    protected void parseGroupBy(final SQLSelectQueryBlock queryBlock) {
        SQLSelectGroupByClause groupBy = null;

        if (getLexer().equalToken(Token.GROUP)) {
            groupBy = new SQLSelectGroupByClause();

            getLexer().nextToken();
            accept(Token.BY);

            while (true) {
                groupBy.addItem(((MySqlExprParser) getExprParser()).parseSelectGroupByItem());
                if (!getLexer().equalToken(Token.COMMA)) {
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

    protected SQLTableSource parseTableSourceRest(final SQLTableSource tableSource) {
        if (getLexer().identifierEquals("USING")) {
            return tableSource;
        }
        if (getLexer().equalToken(Token.USE)) {
            getLexer().nextToken();
            parseIndexHint();
        }
        if (getLexer().identifierEquals("IGNORE")) {
            getLexer().nextToken();
            parseIndexHint();
        }
        if (getLexer().identifierEquals("FORCE")) {
            getLexer().nextToken();
            parseIndexHint();
        }
        return super.parseTableSourceRest(tableSource);
    }

    private void parseIndexHint() {
        if (getLexer().equalToken(Token.INDEX)) {
            getLexer().nextToken();
        } else {
            accept(Token.KEY);
        }
        if (getLexer().equalToken(Token.FOR)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.JOIN)) {
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.ORDER)) {
                getLexer().nextToken();
                accept(Token.BY);
            } else {
                accept(Token.GROUP);
                accept(Token.BY);
            }
        }
        accept(Token.LEFT_PAREN);
        if (getLexer().equalToken(Token.PRIMARY)) {
            getLexer().nextToken();
        } else {
            getExprParser().names(new ArrayList<SQLName>());
        }
        accept(Token.RIGHT_PAREN);
    }
    
    @Override
    protected MySqlUnionQuery createSQLUnionQuery() {
        return new MySqlUnionQuery();
    }

    public SQLUnionQuery unionRest(final SQLUnionQuery union) {
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
