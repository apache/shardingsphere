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

package io.shardingsphere.core.parsing.parser.sql.dql;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.sql.AbstractSQLStatement;
import lombok.ToString;

/**
 * DQL statement.
 *
 * @author zhangliang
 */
@ToString(callSuper = true)
public class DQLStatement extends AbstractSQLStatement {
    
    public DQLStatement() {
        super(SQLType.DQL);
    }
    
    /**
     * Is DQL statement.
     *
     * @param tokenType token type
     * @return is DQL or not
     */
    public static boolean isDQL(final TokenType tokenType) {
        return DefaultKeyword.SELECT == tokenType;
    }
}
