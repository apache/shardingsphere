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

package io.shardingsphere.core.parsing.lexer.dialect.sqlserver;

import io.shardingsphere.core.parsing.lexer.Lexer;
import io.shardingsphere.core.parsing.lexer.analyzer.CharType;
import io.shardingsphere.core.parsing.lexer.analyzer.Dictionary;

/**
 * SQLServer Lexical analysis.
 *
 * @author zhangliang
 */
public final class SQLServerLexer extends Lexer {
    
    private static Dictionary dictionary = new Dictionary(SQLServerKeyword.values());
    
    public SQLServerLexer(final String input) {
        super(input, dictionary);
    }
    
    @Override
    protected boolean isHintBegin() {
        return '/' == getCurrentChar(0) && '*' == getCurrentChar(1) && '!' == getCurrentChar(2);
    }
    
    @Override
    protected boolean isVariableBegin() {
        return '@' == getCurrentChar(0);
    }
    
    @Override
    protected boolean isSupportNChars() {
        return true;
    }
    
    @Override
    protected boolean isIdentifierBegin(final char ch) {
        return CharType.isAlphabet(ch) || '[' == ch || '_' == ch || '$' == ch;
    }
}
