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

package com.alibaba.druid.sql.dialect.sqlserver.lexer;

import com.alibaba.druid.sql.lexer.DataType;
import com.alibaba.druid.sql.lexer.Dictionary;
import com.alibaba.druid.sql.lexer.Lexer;

public class SQLServerLexer extends Lexer {
    
    private static Dictionary dictionary = new Dictionary();
    
    static {
        dictionary.fill(SQLServerKeyword.values());
    }
    
    public SQLServerLexer(final String input) {
        super(input, dictionary);
    }
    
    @Override
    protected void scanIdentifier() {
        if (isNChar()) {
            scanNChar();
        } else {
            super.scanIdentifier();
        }
    }
    
    private boolean isNChar() {
        return 'N' == charAt(getCurrentPosition()) && '\'' == charAt(getCurrentPosition() + 1);
    }
    
    private void scanNChar() {
        increaseCurrentPosition();
        scanString();
        setToken(DataType.LITERAL_NCHARS);
        scanString();
    }
    
    @Override
    protected boolean isHint() {
        return '/' == charAt(getCurrentPosition()) && '*' == charAt(getCurrentPosition() + 1) && '!' == charAt(getCurrentPosition() + 2);
    }
}
