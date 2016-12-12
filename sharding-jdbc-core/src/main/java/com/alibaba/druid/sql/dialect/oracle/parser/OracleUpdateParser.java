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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleUpdateStatement;
import com.alibaba.druid.sql.lexer.Lexer;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.sql.parser.SQLStatementParser;

public class OracleUpdateParser extends SQLStatementParser {

    public OracleUpdateParser(Lexer lexer){
        super(new OracleExprParser(lexer));
    }
    
    public OracleUpdateStatement parseUpdateStatement() {
        OracleUpdateStatement update = new OracleUpdateStatement();
        
        if (getLexer().equalToken(Token.UPDATE)) {
            getLexer().nextToken();

            parseHints(update);

            if (getLexer().identifierEquals("ONLY")) {
                update.setOnly(true);
            }

            SQLTableSource tableSource = this.exprParser.createSelectParser().parseTableSource();
            update.setTableSource(tableSource);

            if ((update.getAlias() == null) || (update.getAlias().length() == 0)) {
                update.setAlias(as());
            }
        }
        parseUpdateSet(update);
        parseWhere(update);
        parseReturn(update);
        parseErrorLoging();
        return update;
    }

    private void parseErrorLoging() {
        if (getLexer().identifierEquals("LOG")) {
            throw new ParserUnsupportedException(getLexer().getToken());
        }
    }

    private void parseReturn(OracleUpdateStatement update) {
        if (getLexer().identifierEquals("RETURN") || getLexer().equalToken(Token.RETURNING)) {
            getLexer().nextToken();

            while (true) {
                SQLExpr item = this.exprParser.expr();
                update.getReturning().add(item);

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }

                break;
            }

            accept(Token.INTO);

            while (true) {
                SQLExpr item = this.exprParser.expr();
                update.getReturningInto().add(item);

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    continue;
                }

                break;
            }
        }
    }

    private void parseHints(OracleUpdateStatement update) {
        this.exprParser.parseHints(update.getHints());
    }

    private void parseWhere(OracleUpdateStatement update) {
        if (getLexer().equalToken(Token.WHERE)) {
            getLexer().nextToken();
            update.setWhere(this.exprParser.expr());
        }
    }

}
