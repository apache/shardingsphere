/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.mysql.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Dictionary;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Lexer;

public final class MySQLLexer extends Lexer {
    
    private static Dictionary dictionary = new Dictionary();
    
    static {
        dictionary.fill(MySQLKeyword.values());
    }
    
    public MySQLLexer(final String input) {
        super(input, dictionary);
    }
    
    @Override
    protected boolean isVariable() {
        char currentChar = charAt(getPosition());
        char nextChar = charAt(getPosition() + 1);
        return ('$' == currentChar && '{' == nextChar)
                || '@' == currentChar
                || (':' == currentChar && '=' != nextChar && ':' != nextChar);
    }
    
    @Override
    protected boolean isHint() {
        return '/' == charAt(getPosition()) && '*' == charAt(getPosition() + 1) && '!' == charAt(getPosition() + 2);
    }
    
    @Override
    protected boolean isComment() {
        return '#' == charAt(getPosition()) || super.isComment();
    }
}
