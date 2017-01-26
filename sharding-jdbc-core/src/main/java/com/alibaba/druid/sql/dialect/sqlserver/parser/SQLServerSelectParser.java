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
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.SQLExprHint;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelect;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;

public class SQLServerSelectParser extends SQLSelectParser {
    
    public SQLServerSelectParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    public SQLSelect select() {
        SQLServerSelect result = new SQLServerSelect();
        withSubquery(result);
        result.setQuery(query());
        result.setOrderBy(getExprParser().parseOrderBy());
        if (getLexer().equalToken(Token.FOR)) {
            parseFor();
        }
        if (getLexer().identifierEquals("OFFSET")) {
            parseOffset(result);
        }
        return result;
    }
    
    @Override
    public SQLSelectQuery query() {
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();
            SQLSelectQuery select = query();
            accept(Token.RIGHT_PAREN);
            return queryRest(select);
        }
        SQLServerSelectQueryBlock queryBlock = new SQLServerSelectQueryBlock();
        if (getLexer().equalToken(Token.SELECT)) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.COMMENT)) {
                getLexer().nextToken();
            }
            if (getLexer().equalToken(Token.DISTINCT)) {
                queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
                getLexer().nextToken();
            } else if (getLexer().equalToken(Token.ALL)) {
                queryBlock.setDistionOption(SQLSetQuantifier.ALL);
                getLexer().nextToken();
            }
            if (getLexer().equalToken(Token.TOP)) {
                queryBlock.setTop(new SQLServerExprParser(getLexer()).parseTop());
            }
            parseSelectList(queryBlock);
        }
        if (getLexer().equalToken(Token.INTO)) {
            getLexer().nextToken();
            SQLTableSource into = this.parseTableSource();
            queryBlock.setInto((SQLExprTableSource) into);
        }
        parseFrom(queryBlock);
        parseWhere(queryBlock);
        parseGroupBy(queryBlock);
        return queryRest(queryBlock);
    }
    
    @Override
    protected SQLTableSource parseTableSourceRest(final SQLTableSource tableSource) {
        if (getLexer().equalToken(Token.WITH)) {
            getLexer().nextToken();
            accept(Token.LEFT_PAREN);

            while (true) {
                SQLExprHint hint = new SQLExprHint(getExprParser().expr());
                hint.setParent(tableSource);
                tableSource.getHints().add(hint);
                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }
                break;
            }
            accept(Token.RIGHT_PAREN);
        }
        return super.parseTableSourceRest(tableSource);
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
    
    private void parseOffset(final SQLServerSelect result) {
        getLexer().nextToken();
        SQLExpr offset = getExprParser().expr();
        accept("ROWS");
        result.setOffset(offset);
        
        if (getLexer().equalToken(Token.FETCH)) {
            getLexer().nextToken();
            accept("NEXT");
            
            SQLExpr rowCount = getExprParser().expr();
            accept("ROWS");
            accept("ONLY");
            result.setRowCount(rowCount);
        }
    }
}
