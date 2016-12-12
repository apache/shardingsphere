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
package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLConstraint;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.lexer.Token;

public class SQLCreateTableParser extends SQLDDLParser {
    
    public SQLCreateTableParser(SQLExprParser exprParser){
        super(exprParser);
    }
    
    public SQLCreateTableStatement parseCrateTable(boolean acceptCreate) {
        if (acceptCreate) {
            accept(Token.CREATE);
        }
        SQLCreateTableStatement createTable = newCreateStatement();
        if (getLexer().identifierEquals("GLOBAL")) {
            getLexer().nextToken();

            if (getLexer().identifierEquals("TEMPORARY")) {
                getLexer().nextToken();
                createTable.setType(SQLCreateTableStatement.Type.GLOBAL_TEMPORARY);
            } else {
                throw new ParserException(getLexer());
            }
        } else if (getLexer().equalToken(Token.IDENTIFIER) && getLexer().getLiterals().equalsIgnoreCase("LOCAL")) {
            getLexer().nextToken();
            if (getLexer().equalToken(Token.IDENTIFIER) && getLexer().getLiterals().equalsIgnoreCase("TEMPORAY")) {
                getLexer().nextToken();
                createTable.setType(SQLCreateTableStatement.Type.LOCAL_TEMPORARY);
            } else {
                throw new ParserException(getLexer());
            }
        }
        accept(Token.TABLE);
        createTable.setName(this.exprParser.name());
        if (getLexer().equalToken(Token.LEFT_PAREN)) {
            getLexer().nextToken();

            while (true) {
                if (getLexer().equalToken(Token.IDENTIFIER)
                    || getLexer().equalToken(Token.LITERAL_ALIAS)) {
                    SQLColumnDefinition column = this.exprParser.parseColumn();
                    createTable.getTableElementList().add(column);
                } else if (getLexer().equalToken(Token.PRIMARY) //
                           || getLexer().equalToken(Token.UNIQUE) //
                           || getLexer().equalToken(Token.CHECK) //
                           || getLexer().equalToken(Token.CONSTRAINT)) {
                    SQLConstraint constraint = this.exprParser.parseConstraint();
                    constraint.setParent(createTable);
                    createTable.getTableElementList().add((SQLTableElement) constraint);
                } else if (getLexer().equalToken(Token.TABLESPACE)) {
                    throw new ParserUnsupportedException(getLexer().getToken());
                } else {
                    SQLColumnDefinition column = this.exprParser.parseColumn();
                    createTable.getTableElementList().add(column);
                }

                if (getLexer().equalToken(Token.COMMA)) {
                    getLexer().nextToken();
                    
                    if (getLexer().equalToken(Token.RIGHT_PAREN)) { // compatible for sql server
                        break;
                    }
                    continue;
                }

                break;
            }
            accept(Token.RIGHT_PAREN);
            if (getLexer().identifierEquals("INHERITS")) {
                getLexer().nextToken();
                accept(Token.LEFT_PAREN);
                SQLName inherits = this.exprParser.name();
                createTable.setInherits(new SQLExprTableSource(inherits));
                accept(Token.RIGHT_PAREN);
            }
        }
        return createTable;
    }

    protected SQLCreateTableStatement newCreateStatement() {
        return new SQLCreateTableStatement(getDbType());
    }
}
