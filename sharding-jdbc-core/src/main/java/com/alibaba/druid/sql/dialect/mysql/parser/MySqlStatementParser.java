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

import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;

import java.util.Set;
import java.util.TreeSet;

public class MySqlStatementParser extends SQLStatementParser {
    
    public MySqlStatementParser(final String sql) {
        super(new MySqlExprParser(sql));
    }
    
    @Override
    protected SQLSelectStatement parseSelect() {
        return new SQLSelectStatement(new MySqlSelectParser(getExprParser()).select(), JdbcConstants.MYSQL);
    }
    
    @Override
    protected SQLSelectParser createSQLSelectParser() {
        return new MySqlSelectParser(getExprParser());
    }
    
    @Override
    protected MySqlDeleteStatement createSQLDeleteStatement() {
        return new MySqlDeleteStatement();
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenDeleteAndFrom() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(MySqlKeyword.LOW_PRIORITY);
        result.add("QUICK");
        result.add(MySqlKeyword.IGNORE);
        return result;
    }
    
    @Override
    protected void parseCustomizedParserBetweenTableAndNextIdentifier(final SQLDeleteStatement deleteStatement) {
        ((MySqlDeleteStatement) deleteStatement).getPartitionNames().addAll(((MySqlExprParser) getExprParser()).parsePartition());
    }
    
    @Override
    protected void parseCustomizedParserAfterWhere(final SQLDeleteStatement deleteStatement) {
        if (getLexer().equalToken(Token.ORDER)) {
            ((MySqlDeleteStatement) deleteStatement).setOrderBy(getExprParser().parseOrderBy());
        }
        ((MySqlDeleteStatement) deleteStatement).setLimit(((MySqlExprParser) getExprParser()).parseLimit());
    }
}
