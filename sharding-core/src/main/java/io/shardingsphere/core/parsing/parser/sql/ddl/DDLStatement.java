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

package io.shardingsphere.core.parsing.parser.sql.ddl;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collection;

/**
 * DDL statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
public class DDLStatement extends AbstractSQLStatement {
    
    private static final Collection<Keyword> PRIMARY_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.CREATE, DefaultKeyword.ALTER, DefaultKeyword.DROP, DefaultKeyword.TRUNCATE);
    
    private static final Collection<Keyword> NOT_SECONDARY_STATEMENT_PREFIX = Arrays.<Keyword>asList(DefaultKeyword.LOGIN, DefaultKeyword.USER, DefaultKeyword.ROLE);
    
    public DDLStatement() {
        super(SQLType.DDL);
    }
    
    /**
     * Is DDL statement.
     *
     * @param primaryTokenType primary token type
     * @param secondaryTokenType secondary token type
     * @return is DDL or not
     */
    public static boolean isDDL(final TokenType primaryTokenType, final TokenType secondaryTokenType) {
        return PRIMARY_STATEMENT_PREFIX.contains(primaryTokenType) && !NOT_SECONDARY_STATEMENT_PREFIX.contains(secondaryTokenType);
    }
}
