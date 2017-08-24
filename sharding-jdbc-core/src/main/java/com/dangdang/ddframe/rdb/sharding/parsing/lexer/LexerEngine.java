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

package com.dangdang.ddframe.rdb.sharding.parsing.lexer;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Assist;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Token;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.TokenType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 词法解析器引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class LexerEngine {
    
    private final Lexer lexer;
    
    /**
     * 获取输入的字符串.
     * 
     * @return 输入的字符串
     */
    public String getInput() {
        return lexer.getInput();
    }
    
    /**
     * 分析下一个词法标记.
     */
    public void nextToken() {
        lexer.nextToken();
    }
    
    /**
     * 获取当前词法标记.
     * 
     * @return 词法标记
     */
    public Token getCurrentToken() {
        return lexer.getCurrentToken();
    }
    
    /**
     * 跳过小括号内所有的词法标记.
     *
     * @param sqlStatement SQL语句对象
     * @return 小括号内所有的词法标记
     */
    public String skipParentheses(final SQLStatement sqlStatement) {
        StringBuilder result = new StringBuilder("");
        int count = 0;
        if (Symbol.LEFT_PAREN == lexer.getCurrentToken().getType()) {
            final int beginPosition = lexer.getCurrentToken().getEndPosition();
            result.append(Symbol.LEFT_PAREN.getLiterals());
            lexer.nextToken();
            while (true) {
                if (equalAny(Symbol.QUESTION)) {
                    sqlStatement.increaseParametersIndex();
                }
                if (Assist.END == lexer.getCurrentToken().getType() || (Symbol.RIGHT_PAREN == lexer.getCurrentToken().getType() && 0 == count)) {
                    break;
                }
                if (Symbol.LEFT_PAREN == lexer.getCurrentToken().getType()) {
                    count++;
                } else if (Symbol.RIGHT_PAREN == lexer.getCurrentToken().getType()) {
                    count--;
                }
                lexer.nextToken();
            }
            result.append(lexer.getInput().substring(beginPosition, lexer.getCurrentToken().getEndPosition()));
            lexer.nextToken();
        }
        return result.toString();
    }
    
    /**
     * 断言当前词法标记类型与传入值相等并跳过.
     *
     * @param tokenType 待判断的词法标记类型
     */
    public void accept(final TokenType tokenType) {
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
    public boolean equalAny(final TokenType... tokenTypes) {
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
    public boolean skipIfEqual(final TokenType... tokenTypes) {
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
    public void skipAll(final TokenType... tokenTypes) {
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
    public void skipUntil(final TokenType... tokenTypes) {
        Set<TokenType> tokenTypeSet = Sets.newHashSet(tokenTypes);
        tokenTypeSet.add(Assist.END);
        while (!tokenTypeSet.contains(lexer.getCurrentToken().getType())) {
            lexer.nextToken();
        }
    }
    
    /**
     * 如果当前词法标记类型等于传入值, 则抛出不支持异常.
     * 
     * @param tokenTypes 待判断的词法标记类型
     */
    public void unsupportedIfEqual(final TokenType... tokenTypes) {
        if (equalAny(tokenTypes)) {
            throw new SQLParsingUnsupportedException(lexer.getCurrentToken().getType());
        }
    }
    
    /**
     * 如果当前词法标记类型不等于传入值, 则抛出不支持异常, 否则跳过当前词法标记.
     *
     * @param tokenTypes 待判断的词法标记类型
     */
    public void unsupportedIfNotSkip(final TokenType... tokenTypes) {
        if (!skipIfEqual(tokenTypes)) {
            throw new SQLParsingUnsupportedException(lexer.getCurrentToken().getType());
        }
    }
}
