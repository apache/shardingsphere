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

package com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.sqlserver.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Literals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Dictionary;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.AbstractLexer;

public final class SQLServerLexer extends AbstractLexer {
    
    private static Dictionary dictionary = new Dictionary();
    
    static {
        dictionary.fill(SQLServerKeyword.values());
    }
    
    public SQLServerLexer(final String input) {
        super(input, dictionary);
    }
    
    @Override
    protected boolean isVariableBegin() {
        return '@' == charAt(getPosition());
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
        return 'N' == charAt(getPosition()) && '\'' == charAt(getPosition() + 1);
    }
    
    private void scanNChar() {
        increaseCurrentPosition();
        scanChars();
        setToken(Literals.NCHARS);
        scanChars();
    }
    
    @Override
    protected boolean isHintBegin() {
        return '/' == charAt(getPosition()) && '*' == charAt(getPosition() + 1) && '!' == charAt(getPosition() + 2);
    }
}
