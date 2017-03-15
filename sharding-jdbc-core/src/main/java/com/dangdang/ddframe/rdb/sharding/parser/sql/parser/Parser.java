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

package com.dangdang.ddframe.rdb.sharding.parser.sql.parser;

import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Assist;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.Symbol;
import com.dangdang.ddframe.rdb.sharding.parser.sql.lexer.TokenType;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class Parser {
    
    @Getter
    private final Lexer lexer;
    
    /**
     * 跳过小括号内所有的语言符号.
     *
     * @return 小括号内所有的语言符号
     */
    public final String skipParentheses() {
        StringBuilder result = new StringBuilder("");
        int count = 0;
        if (Symbol.LEFT_PAREN == getLexer().getToken().getType()) {
            int beginPosition = getLexer().getToken().getEndPosition();
            result.append(Symbol.LEFT_PAREN.getLiterals());
            getLexer().nextToken();
            while (true) {
                if (Assist.EOF == getLexer().getToken().getType() || (Symbol.RIGHT_PAREN == getLexer().getToken().getType() && 0 == count)) {
                    break;
                }
                if (Symbol.LEFT_PAREN == getLexer().getToken().getType()) {
                    count++;
                } else if (Symbol.RIGHT_PAREN == getLexer().getToken().getType()) {
                    count--;
                }
                getLexer().nextToken();
            }
            result.append(getLexer().getInput().substring(beginPosition, getLexer().getToken().getEndPosition()));
            getLexer().nextToken();
        }
        return result.toString();
    }
    
    
    /**
     * 断言当前标记类型与传入值相等并跳过.
     *
     * @param tokenType 待判断的标记类型
     */
    public final void accept(final TokenType tokenType) {
        if (lexer.getToken().getType() != tokenType) {
            throw new ParserException(lexer, tokenType);
        }
        lexer.nextToken();
    }
    
    /**
     * 判断当前语言标记是否和其中一个传入的标记相等.
     *
     * @param tokenTypes 待判断的标记类型
     * @return 是否有相等的标记类型
     */
    public final boolean equal(final TokenType... tokenTypes) {
        for (TokenType each : tokenTypes) {
            if (each == lexer.getToken().getType()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 如果当前语言符号等于传入值, 则跳过.
     *
     * @param tokenTypes 待跳过的语言符号
     * @return 是否跳过(或可理解为是否相等)
     */
    public final boolean skipIfEqual(final TokenType... tokenTypes) {
        for (TokenType each : tokenTypes) {
            if (equal(each)) {
                lexer.nextToken();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 直接跳转至传入的语言符号.
     *
     * @param tokenTypes 跳转至的语言符号
     */
    public final void skipUntil(final TokenType... tokenTypes) {
        Set<TokenType> tokenTypeSet = Sets.newHashSet(tokenTypes);
        tokenTypeSet.add(Assist.EOF);
        while (!tokenTypeSet.contains(lexer.getToken().getType())) {
            lexer.nextToken();
        }
    }
}
