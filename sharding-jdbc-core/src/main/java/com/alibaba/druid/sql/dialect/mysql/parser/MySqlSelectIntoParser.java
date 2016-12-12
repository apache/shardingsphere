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
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlForceIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIgnoreIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIndexHintImpl;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUseIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlSelectIntoStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @Description: parse select into statement
 * @author zz email:455910092@qq.com
 * @date 2015-9-14
 * @version V1.0
 */
public class MySqlSelectIntoParser extends SQLSelectParser {
    private List<SQLExpr> argsList;

    public MySqlSelectIntoParser(SQLExprParser exprParser){
        super(exprParser);
    }

    public MySqlSelectIntoStatement parseSelectInto() {
        SQLSelect select=select();
        MySqlSelectIntoStatement stmt=new MySqlSelectIntoStatement();
        stmt.setSelect(select);
        stmt.setVarList(argsList);
        return stmt;
        
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
                this.exprParser.parseHints(queryBlock.getHints());
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
                queryBlock.setHignPriority(true);
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
            
            argsList=parseIntoArgs();
        }

        parseFrom(queryBlock);

        parseWhere(queryBlock);

        parseGroupBy(queryBlock);

        queryBlock.setOrderBy(this.exprParser.parseOrderBy());

        if (getLexer().equalToken(Token.LIMIT)) {
            queryBlock.setLimit(parseLimit());
        }

        if (getLexer().equalToken(Token.PROCEDURE)) {
            getLexer().nextToken();
            throw new ParserException("TODO");
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
            acceptIdentifier("SHARE");
            acceptIdentifier("MODE");
            queryBlock.setLockInShareMode(true);
        }

        return queryRest(queryBlock);
    }
    /**
     * parser the select into arguments
     * @return
     */
    protected List<SQLExpr> parseIntoArgs() {
        List<SQLExpr> args = new ArrayList<>();
        if (getLexer().equalToken(Token.INTO)) {
            accept(Token.INTO);
            while (true) {
                SQLExpr var = exprParser.primary();
                if (var instanceof SQLIdentifierExpr) {
                    var = new SQLVariantRefExpr(
                            ((SQLIdentifierExpr) var).getName());
                }
                args.add(var);
                if (getLexer().equalToken(Token.COMMA)) {
                    accept(Token.COMMA);
                    continue;
                }
                break;
            }
        }
        return args;
    }
    
    protected void parseInto(SQLSelectQueryBlock queryBlock) {
        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("OUTFILE")) {
                getLexer().nextToken();

                MySqlOutFileExpr outFile = new MySqlOutFileExpr();
                outFile.setFile(expr());

                queryBlock.setInto(outFile);

                if (getLexer().identifierEquals("FIELDS") || getLexer().identifierEquals("COLUMNS")) {
                    getLexer().nextToken();

                    if (getLexer().identifierEquals("TERMINATED")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                    }
                    outFile.setColumnsTerminatedBy((SQLLiteralExpr) expr());

                    if (getLexer().identifierEquals("OPTIONALLY")) {
                        getLexer().nextToken();
                        outFile.setColumnsEnclosedOptionally(true);
                    }

                    if (getLexer().identifierEquals("ENCLOSED")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setColumnsEnclosedBy((SQLLiteralExpr) expr());
                    }

                    if (getLexer().identifierEquals("ESCAPED")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setColumnsEscaped((SQLLiteralExpr) expr());
                    }
                }

                if (getLexer().identifierEquals("LINES")) {
                    getLexer().nextToken();

                    if (getLexer().identifierEquals("STARTING")) {
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setLinesStartingBy((SQLLiteralExpr) expr());
                    } else {
                        getLexer().identifierEquals("TERMINATED");
                        getLexer().nextToken();
                        accept(Token.BY);
                        outFile.setLinesTerminatedBy((SQLLiteralExpr) expr());
                    }
                }
            } else {
                queryBlock.setInto(this.exprParser.name());
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
                groupBy.addItem(this.getExprParser().parseSelectGroupByItem());
                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }
                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.WITH)) {
                getLexer().nextToken();
                acceptIdentifier("ROLLUP");

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
            groupBy.setHaving(this.exprParser.expr());
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
            this.exprParser.names(hint.getIndexList());
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
        return ((MySqlExprParser) this.exprParser).parseLimit();
    }
    
    public MySqlExprParser getExprParser() {
        return (MySqlExprParser) exprParser;
    }
}
