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

package com.alibaba.druid.sql.dialect.mysql.lexer;

import com.alibaba.druid.sql.lexer.Dictionary;
import com.alibaba.druid.sql.lexer.Lexer;

public final class MySql1Lexer extends Lexer {
    
    private static Dictionary dictionary = new Dictionary();
    
    static {
        dictionary.fill(MySQLKeyword.values());
    }
    
    public MySql1Lexer(final String input) {
        super(input, dictionary);
    }
    
    @Override
    protected boolean isVariable() {
        char currentChar = charAt(getCurrentPosition());
        char nextChar = charAt(getCurrentPosition() + 1);
        return ('$' == currentChar && '{' == nextChar)
                || '@' == currentChar
                || (':' == currentChar && '=' != nextChar && ':' != nextChar);
    }
    
    @Override
    protected boolean isHint() {
        return '/' == charAt(getCurrentPosition()) && '*' == charAt(getCurrentPosition() + 1) && '!' == charAt(getCurrentPosition() + 2);
    }
    
    @Override
    protected boolean isComment() {
        return '#' == charAt(getCurrentPosition()) || super.isComment();
    }
}
