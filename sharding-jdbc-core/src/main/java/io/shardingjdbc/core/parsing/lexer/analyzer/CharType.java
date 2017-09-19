/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.lexer.analyzer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Char type.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CharType {
    
    /**
     * End of input.
     */
    public static final byte EOI = 0x1A;
    
    /**
     * Adjust is whitespace or not.
     * 
     * @param ch to be adjusted char
     * @return is whitespace or not
     */
    public static boolean isWhitespace(final char ch) {
        return ch <= 32 && EOI != ch || 160 == ch || ch >= 0x7F && ch <= 0xA0;
    }
    
    /**
     * Adjust is end of input or not.
     *
     * @param ch to be adjusted char
     * @return is end of input or not
     */
    public static boolean isEndOfInput(final char ch) {
        return ch == 0x1A;
    }
    
    /**
     * Adjust is alphabet or not.
     *
     * @param ch to be adjusted char
     * @return is alphabet or not
     */
    public static boolean isAlphabet(final char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z';
    }
    
    /**
     * Adjust is digital or not.
     *
     * @param ch to be adjusted char
     * @return is alphabet or not
     */
    public static boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    /**
     * Adjust is symbol or not.
     *
     * @param ch to be adjusted char
     * @return is symbol or not
     */
    public static boolean isSymbol(final char ch) {
        return '(' == ch || ')' == ch || '[' == ch || ']' == ch || '{' == ch || '}' == ch || '+' == ch || '-' == ch || '*' == ch || '/' == ch || '%' == ch || '^' == ch || '=' == ch
                || '>' == ch || '<' == ch || '~' == ch || '!' == ch || '?' == ch || '&' == ch || '|' == ch || '.' == ch || ':' == ch || '#' == ch || ',' == ch || ';' == ch;
    }
}
