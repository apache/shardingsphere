/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.parser.sql.dal;

import lombok.ToString;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import org.apache.shardingsphere.core.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Keyword;
import org.apache.shardingsphere.core.parsing.lexer.token.TokenType;
import org.apache.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;

import java.util.Arrays;
import java.util.Collection;

/**
 * DAL statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
public class DALStatement extends AbstractSQLStatement {
    
    private static final Collection<Keyword> SINGLE_TOKEN_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.USE, DefaultKeyword.DESC, MySQLKeyword.DESCRIBE, MySQLKeyword.SHOW, 
            PostgreSQLKeyword.SHOW, PostgreSQLKeyword.RESET);
    
    public DALStatement() {
        super(SQLType.DAL);
    }
    
    /**
     * Is DAL statement.
     * 
     * @param tokenType token type
     * @return is DAL or not
     */
    public static boolean isDAL(final TokenType tokenType) {
        return SINGLE_TOKEN_STATEMENT_PREFIX.contains(tokenType);
    }
}
