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

package com.dangdang.ddframe.rdb.sharding.parsing.lexer.analyzer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 字符类型.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CharType {
    
    /**
     * 输入结束符.
     */
    public static final byte EOI = 0x1A;
    
    /**
     * 判断是否为空格.
     * 
     * @param ch 待判断的字符
     * @return 是否为空格
     */
    public static boolean isWhitespace(final char ch) {
        return ch <= 32 && EOI != ch || 160 == ch || ch >= 0x7F && ch <= 0xA0;
    }
    
    /**
     * 判断是否输入结束.
     *
     * @param ch 待判断的字符
     * @return 是否输入结束
     */
    public static boolean isEndOfInput(final char ch) {
        return ch == 0x1A;
    }
    
    /**
     * 判断是否为字母.
     *
     * @param ch 待判断的字符
     * @return 是否为字母
     */
    public static boolean isAlphabet(final char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z';
    }
    
    /**
     * 判断是否为数字.
     *
     * @param ch 待判断的字符
     * @return 是否为数字
     */
    public static boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    /**
     * 判断是否为符号.
     *
     * @param ch 待判断的字符
     * @return 是否为符号
     */
    public static boolean isSymbol(final char ch) {
        return '(' == ch || ')' == ch || '[' == ch || ']' == ch || '{' == ch || '}' == ch || '+' == ch || '-' == ch || '*' == ch || '/' == ch || '%' == ch || '^' == ch || '=' == ch
                || '>' == ch || '<' == ch || '~' == ch || '!' == ch || '?' == ch || '&' == ch || '|' == ch || '.' == ch || ':' == ch || '#' == ch || ',' == ch || ';' == ch;
    }
}
