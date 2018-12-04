/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.sql.dal;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingsphere.core.parsing.lexer.dialect.postgresql.PostgreSQLKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * DAL statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
public class DALStatement extends AbstractSQLStatement {
    
    private static final Collection<Keyword> SINGLE_TOKEN_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.USE, DefaultKeyword.DESC, MySQLKeyword.DESCRIBE, MySQLKeyword.SHOW, 
            PostgreSQLKeyword.SHOW, PostgreSQLKeyword.RESET);

    private static final Collection<Keyword> DUAL_TOKEN_PRIMARY_STATEMENT_PREFIX = Collections.<Keyword>singletonList(DefaultKeyword.SET);

    private static final Collection<Keyword> DUAL_TOKEN_NOT_SECONDARY_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.ROLE, DefaultKeyword.TRANSACTION, PostgreSQLKeyword.CONSTRAINTS);
    
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

    /**
     * Is DAL statement.
     *
     * @param primaryTokenType primary token type
     * @param secondaryTokenType secondary token type
     * @return is DAL or not
     */
    public static boolean isDAL(final TokenType primaryTokenType, final TokenType secondaryTokenType) {
        return DUAL_TOKEN_PRIMARY_STATEMENT_PREFIX.contains(primaryTokenType) && !DUAL_TOKEN_NOT_SECONDARY_STATEMENT_PREFIX.contains(secondaryTokenType);
    }
}
