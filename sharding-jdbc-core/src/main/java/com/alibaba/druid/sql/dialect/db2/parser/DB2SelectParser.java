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
package com.alibaba.druid.sql.dialect.db2.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.db2.ast.stmt.DB2SelectQueryBlock;
import com.alibaba.druid.sql.dialect.db2.ast.stmt.DB2SelectQueryBlock.Isolation;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;

public class DB2SelectParser extends SQLSelectParser {
    
    public DB2SelectParser(SQLExprParser exprParser){
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
        accept(Token.SELECT);
        if (getLexer().equalToken(Token.COMMENT)) {
            getLexer().nextToken();
        }
        DB2SelectQueryBlock queryBlock = new DB2SelectQueryBlock();
        if (getLexer().equalToken(Token.DISTINCT)) {
            queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.UNIQUE)) {
            queryBlock.setDistionOption(SQLSetQuantifier.UNIQUE);
            getLexer().nextToken();
        } else if (getLexer().equalToken(Token.ALL)) {
            queryBlock.setDistionOption(SQLSetQuantifier.ALL);
            getLexer().nextToken();
        }
        parseSelectList(queryBlock);
        parseFrom(queryBlock);
        parseWhere(queryBlock);
        parseGroupBy(queryBlock);
        while (true) {
            if (getLexer().equalToken(Token.FETCH)) {
                getLexer().nextToken();
                accept(Token.FIRST);
                SQLExpr first = getExprParser().primary();
                queryBlock.setFirst(first);
                if (getLexer().identifierEquals("ROW") || getLexer().identifierEquals("ROWS")) {
                    getLexer().nextToken();
                }
                accept(Token.ONLY);
                continue;
            }
            
            if (getLexer().equalToken(Token.WITH)) {
                getLexer().nextToken();
                if (getLexer().identifierEquals("RR")) {
                    queryBlock.setIsolation(Isolation.RR);
                } else if (getLexer().identifierEquals("RS")) {
                    queryBlock.setIsolation(Isolation.RS);
                } else if (getLexer().identifierEquals("CS")) {
                    queryBlock.setIsolation(Isolation.CS);
                } else if (getLexer().identifierEquals("UR")) {
                    queryBlock.setIsolation(Isolation.UR);
                } else {
                    throw new ParserUnsupportedException(getLexer().getToken());
                }
                getLexer().nextToken();
                continue;
            }
            
            if (getLexer().equalToken(Token.FOR)) {
                getLexer().nextToken();
                accept("READ");
                accept(Token.ONLY);
                queryBlock.setForReadOnly(true);
            }
            
            if (getLexer().equalToken(Token.OPTIMIZE)) {
                getLexer().nextToken();
                accept(Token.FOR);
                
                queryBlock.setOptimizeFor(getExprParser().expr());
                if (getLexer().identifierEquals("ROW")) {
                    getLexer().nextToken();
                } else {
                    accept("ROWS");
                }
            }
            
            break;
        }
        return queryRest(queryBlock);
    }
}
