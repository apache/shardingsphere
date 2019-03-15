/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.lexer.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Symbol token.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum Symbol implements TokenType, Keyword {
    
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
    AT("@"),
    POUND("#");
    
    private static Map<String, Symbol> symbols = new HashMap<>(128);
    
    @Getter
    private static Symbol[] operators;
    
    static {
        for (Symbol each : Symbol.values()) {
            symbols.put(each.getLiterals(), each);
        }
        operators = new Symbol[]
            {PLUS, SUB, STAR, SLASH, EQ, GT, LT, CARET, PERCENT, LT_EQ, GT_EQ, LT_EQ_GT, LT_GT, BANG_EQ, BANG_GT, BANG_LT, AMP, BAR, DOUBLE_AMP, DOUBLE_BAR, DOUBLE_LT, DOUBLE_GT};
    }
    
    private final String literals;
    
    /**
     * 通过字面量查找词法符号.
     * 
     * @param literals 字面量
     * @return 词法符号
     */
    public static Symbol literalsOf(final String literals) {
        return symbols.get(literals);
    }
}
