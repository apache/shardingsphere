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

import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleUpdateStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

import java.util.Set;
import java.util.TreeSet;

public class OracleUpdateParser extends AbstractUpdateParser {
    
    public OracleUpdateParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected OracleUpdateStatement createUpdateStatement() {
        return new OracleUpdateStatement();
    }
    
    @Override
    protected void parseCustomizedParserBetweenUpdateAndTable(final AbstractSQLUpdateStatement updateStatement) {
        ((OracleUpdateStatement) updateStatement).getHints().addAll(getExprParser().parseHints());
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenUpdateAndTable() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.ONLY.getName());
        return result;
    }
    
    @Override
    protected void parseAlias(final AbstractSQLUpdateStatement updateStatement) {
        OracleUpdateStatement oracleUpdateStatement = (OracleUpdateStatement) updateStatement; 
        if ((oracleUpdateStatement.getAlias() == null) || (oracleUpdateStatement.getAlias().length() == 0)) {
            oracleUpdateStatement.setAlias(as());
        }
    }
    
    @Override
    protected Set<String> getAppendixIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add("LOG");
        result.add(Token.RETURNING.getName());
        result.add("RETURN");
        return result;
    }
}
