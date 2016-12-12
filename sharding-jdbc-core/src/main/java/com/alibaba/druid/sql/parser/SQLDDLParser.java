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

import com.alibaba.druid.sql.ast.statement.SQLTableConstraint;
import com.alibaba.druid.sql.lexer.Token;

public class SQLDDLParser extends SQLStatementParser {
    
    public SQLDDLParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    protected SQLTableConstraint parseConstraint() {
        if (getLexer().equalToken(Token.CONSTRAINT)) {
            getLexer().nextToken();
        }
        if (getLexer().equalToken(Token.IDENTIFIER)) {
            this.exprParser.name();
            throw new ParserException("TODO");
        }
        if (getLexer().equalToken(Token.PRIMARY)) {
            getLexer().nextToken();
            accept(Token.KEY);
            throw new ParserException("TODO");
        }
        throw new ParserException("TODO");
    }
}
