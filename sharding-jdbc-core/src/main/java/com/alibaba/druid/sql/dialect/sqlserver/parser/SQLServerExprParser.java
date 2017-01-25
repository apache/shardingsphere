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
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerOutput;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.sql.dialect.sqlserver.ast.expr.SQLServerObjectReferenceExpr;
import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.util.JdbcConstants;

import java.util.List;

public class SQLServerExprParser extends SQLExprParser {
    
    private static final String[] AGGREGATE_FUNCTIONS = {"MAX", "MIN", "COUNT", "SUM", "AVG", "STDDEV", "ROW_NUMBER"};
    
    public SQLServerExprParser(final String sql) {
        super(new SQLServerLexer(sql), JdbcConstants.SQL_SERVER, AGGREGATE_FUNCTIONS);
        getLexer().nextToken();
    }
    
    public SQLServerExprParser(final Lexer lexer) {
        super(lexer, JdbcConstants.SQL_SERVER, AGGREGATE_FUNCTIONS);
    }
    
    public SQLServerTop parseTop() {
        if (getLexer().equalToken(Token.TOP)) {
            SQLServerTop top = new SQLServerTop();
            getLexer().nextToken();
            
            boolean paren = false;
            if (getLexer().equalToken(Token.LEFT_PAREN)) {
                paren = true;
                getLexer().nextToken();
            }
            
            top.setExpr(primary());
            
            if (paren) {
                accept(Token.RIGHT_PAREN);
            }
            
            if (getLexer().equalToken(Token.PERCENT)) {
                getLexer().nextToken();
                top.setPercent(true);
            }
            
            return top;
        }
        
        return null;
    }
    
    
    
    
    
    
    
    public SQLExpr primary() {
        if (getLexer().equalToken(Token.LEFT_BRACKET)) {
            getLexer().nextToken();
            SQLExpr name = this.name();
            accept(Token.RIGHT_BRACKET);
            return primaryRest(name);
        }

        return super.primary();
    }

    public SQLServerSelectParser createSelectParser() {
        return new SQLServerSelectParser(this);
    }

    public SQLExpr primaryRest(SQLExpr expr) {
        if (getLexer().equalToken(Token.DOUBLE_DOT)) {
            expr = nameRest((SQLName) expr);
        }

        return super.primaryRest(expr);
    }

    protected SQLExpr dotRest(SQLExpr expr) {
        boolean backet = false;

        if (getLexer().equalToken(Token.LEFT_BRACKET)) {
            getLexer().nextToken();
            backet = true;
        }

        expr = super.dotRest(expr);

        if (backet) {
            accept(Token.RIGHT_BRACKET);
        }

        return expr;
    }

    public SQLName nameRest(SQLName expr) {
        if (getLexer().equalToken(Token.DOUBLE_DOT)) {
            getLexer().nextToken();

            boolean backet = false;
            if (getLexer().equalToken(Token.LEFT_BRACKET)) {
                getLexer().nextToken();
                backet = true;
            }
            String text = getLexer().getLiterals();
            getLexer().nextToken();

            if (backet) {
                accept(Token.RIGHT_BRACKET);
            }

            SQLServerObjectReferenceExpr owner = new SQLServerObjectReferenceExpr(expr);
            expr = new SQLPropertyExpr(owner, text);
        }

        return super.nameRest(expr);
    }

    protected SQLServerOutput parserOutput() {
        if (getLexer().equalToken(Token.OUTPUT)) {
            getLexer().nextToken();
            SQLServerOutput output = new SQLServerOutput();

            final List<SQLSelectItem> selectList = output.getSelectList();
            while (true) {
                final SQLSelectItem selectItem = parseSelectItem();
                selectList.add(selectItem);

                if (!getLexer().equalToken(Token.COMMA)) {
                    break;
                }

                getLexer().nextToken();
            }

            if (getLexer().equalToken(Token.INTO)) {
                getLexer().nextToken();
                output.setInto(new SQLExprTableSource(this.name()));
                if (getLexer().equalToken(Token.LEFT_PAREN)) {
                    getLexer().nextToken();
                    output.getColumns().addAll(exprList(output));
                    accept(Token.RIGHT_PAREN);
                }
            }
            return output;
        }
        return null;
    }

    public SQLSelectItem parseSelectItem() {
        SQLExpr expr;
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            expr = new SQLIdentifierExpr(getLexer().getLiterals());
            getLexer().nextTokenCommaOrRightParen();

            if (getLexer().equalToken(Token.COMMA)) {
                expr = this.primaryRest(expr);
                expr = this.exprRest(expr);
            }
        } else {
            expr = this.expr();
        }
        final String alias = as();
        return new SQLSelectItem(expr, alias);
    }
}
