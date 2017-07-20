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

package com.dangdang.ddframe.rdb.sharding.parsing.parser;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.Lexer;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * 解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class AbstractParser {
    
    @Getter
    private final Lexer lexer;
    
    @Getter
    @Setter
    private int parametersIndex;
    
    /**
     * 增加索引偏移量.
     * 
     * @return 增加后的索引偏移量
     */
    public int increaseParametersIndex() {
        return ++parametersIndex;
    }
    
    /**
     * 跳过小括号内所有的词法标记.
     *
     * @return 小括号内所有的词法标记
     */
    public final String skipParentheses() {
        StringBuilder result = new StringBuilder("");
        int count = 0;
        if (Symbol.LEFT_PAREN == getLexer().getCurrentToken().getType()) {
            final int beginPosition = getLexer().getCurrentToken().getEndPosition();
            result.append(Symbol.LEFT_PAREN.getLiterals());
            getLexer().nextToken();
            while (true) {
                if (equalAny(Symbol.QUESTION)) {
                    increaseParametersIndex();
                }
                if (Assist.END == getLexer().getCurrentToken().getType() || (Symbol.RIGHT_PAREN == getLexer().getCurrentToken().getType() && 0 == count)) {
                    break;
                }
                if (Symbol.LEFT_PAREN == getLexer().getCurrentToken().getType()) {
                    count++;
                } else if (Symbol.RIGHT_PAREN == getLexer().getCurrentToken().getType()) {
                    count--;
                }
                getLexer().nextToken();
            }
            result.append(getLexer().getInput().substring(beginPosition, getLexer().getCurrentToken().getEndPosition()));
            getLexer().nextToken();
        }
        return result.toString();
    }
    
    /**
     * 跳过无用的嵌套小括号.
     */
    public final void skipUselessParentheses() {
        while (skipIfEqual(Symbol.LEFT_PAREN)) { }
        while (skipIfEqual(Symbol.RIGHT_PAREN)) { }
    }
    
    /**
     * 断言当前词法标记类型与传入值相等并跳过.
     *
     * @param tokenType 待判断的词法标记类型
     */
    public final void accept(final TokenType tokenType) {
        if (lexer.getCurrentToken().getType() != tokenType) {
            throw new SQLParsingException(lexer, tokenType);
        }
        lexer.nextToken();
    }
    
    /**
     * 判断当前词法标记类型是否与其中一个传入值相等.
     *
     * @param tokenTypes 待判断的词法标记类型
     * @return 是否有相等的词法标记类型
     */
    public final boolean equalAny(final TokenType... tokenTypes) {
        for (TokenType each : tokenTypes) {
            if (each == lexer.getCurrentToken().getType()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 如果当前词法标记类型等于传入值, 则跳过.
     *
     * @param tokenTypes 待跳过的词法标记类型
     * @return 是否跳过(或可理解为是否相等)
     */
    public final boolean skipIfEqual(final TokenType... tokenTypes) {
        if (equalAny(tokenTypes)) {
            lexer.nextToken();
            return true;
        }
        return false;
    }
    
    /**
     * 跳过所有传入的词法标记类型.
     *
     * @param tokenTypes 待跳过的词法标记类型
     */
    public final void skipAll(final TokenType... tokenTypes) {
        Set<TokenType> tokenTypeSet = Sets.newHashSet(tokenTypes);
        while (tokenTypeSet.contains(lexer.getCurrentToken().getType())) {
            lexer.nextToken();
        }
    }
    
    /**
     * 直接跳转至传入的词法标记类型.
     *
     * @param tokenTypes 跳转至的词法标记类型
     */
    public final void skipUntil(final TokenType... tokenTypes) {
        Set<TokenType> tokenTypeSet = Sets.newHashSet(tokenTypes);
        tokenTypeSet.add(Assist.END);
        while (!tokenTypeSet.contains(lexer.getCurrentToken().getType())) {
            lexer.nextToken();
        }
    }
}
