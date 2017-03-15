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

package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 符号标记.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum Symbol implements TokenType {
    
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),
    LEFT_BRACKET("["),
    RIGHT_BRACKET("]"),
    SEMI(";"),
    COMMA(","),
    DOT("."),
    DOUBLE_DOT(".."),
    PLUS("+"),
    SUB("-"),
    STAR("*"),
    SLASH("/"),
    QUESTION("?"),
    EQ("="),
    GT(">"),
    LT("<"),
    BANG("!"),
    TILDE("~"),
    CARET("^"),
    PERCENT("%"),
    COLON(":"),
    DOUBLE_COLON("::"),
    COLON_EQ(":="),
    LT_EQ("<="),
    GT_EQ(">="),
    LT_EQ_GT("<=>"),
    LT_GT("<>"),
    BANG_EQ("!="),
    BANG_GT("!>"),
    BANG_LT("!<"),
    AMP("&"),
    BAR("|"),
    DOUBLE_AMP("&&"),
    DOUBLE_BAR("||"),
    DOUBLE_LT("<<"),
    DOUBLE_GT(">>"),
    MONKEYS_AT("@"),
    POUND("#");
    
    private static Map<String, Symbol> symbols = new HashMap<>(128);
    
    private static Set<Character> symbolChars;
    
    static {
        for (Symbol each : Symbol.values()) {
            symbols.put(each.getLiterals(), each);
        }
        symbolChars = Sets.newHashSet('(', ')', '[', ']', '{', '}', '+', '-', '*', '/', '%', '^', '=', '>', '<', '~', '!', '?', '&', '|', '.', ':', '#', ',', ';');
    }
    
    private final String literals;
    
    /**
     * 通过字面量查找符号.
     * 
     * @param literals 字面量
     * @return 符号标记
     */
    public static Symbol literalsOf(final String literals) {
        return symbols.get(literals);
    }
    
    /**
     * 判断字符是否是符号.
     *
     * @param ch 待判断的字符
     * @return 是否是符号
     */
    public static boolean isSymbol(final char ch) {
        return symbolChars.contains(ch);
    }
}
