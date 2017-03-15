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

package com.dangdang.ddframe.rdb.sharding.parser.sql.lexer;

import com.dangdang.ddframe.rdb.sharding.parser.sql.parser.ParserException;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 词法解析器.
 * 
 * @author zhangliang 
 */
@RequiredArgsConstructor
public class Lexer {
    
    @Getter
    private final String input;
    
    private final Dictionary dictionary;
    
    private int position;
    
    @Getter
    private Token token;
    
    /**
     * 跳至下一个语言符号.
     */
    public final void nextToken() {
        skipIgnoredToken();
        if (isVariableBegin()) {
            token = new Tokenizer(input, dictionary, position).scanVariable();
        } else if (isSupportNChars() && isNCharBegin()) {
            position++;
            token = new Tokenizer(input, dictionary, position).scanChars();
        } else if (isIdentifierBegin()) {
            token = new Tokenizer(input, dictionary, position).scanIdentifier();
        } else if (isHexDecimalBegin()) {
            token = new Tokenizer(input, dictionary, position).scanHexDecimal();
        } else if (isNumberBegin()) {
            token = new Tokenizer(input, dictionary, position).scanNumber();
        } else if (isSymbolBegin()) {
            token = new Tokenizer(input, dictionary, position).scanSymbol();
        } else if (isCharsBegin()) {
            token = new Tokenizer(input, dictionary, position).scanChars();
        } else if (isAliasBegin()) {
            token = new Tokenizer(input, dictionary, position).scanUntil('\"', Literals.ALIAS);
        } else if (isEOF()) {
            token = new Token(Assist.EOF, "", position);
        } else {
            token = new Token(Assist.ERROR, "", position);
        }
        position = token.getEndPosition();
    }
    
    private void skipIgnoredToken() {
        position = new Tokenizer(input, dictionary, position).skipWhitespace();
        while (isHintBegin()) {
            position = new Tokenizer(input, dictionary, position).skipHint();
            position = new Tokenizer(input, dictionary, position).skipWhitespace();
        }
        while (isCommentBegin()) {
            position = new Tokenizer(input, dictionary, position).skipComment();
            position = new Tokenizer(input, dictionary, position).skipWhitespace();
        }
    }
    
    protected boolean isHintBegin() {
        return false;
    }
    
    protected boolean isCommentBegin() {
        String chars = String.valueOf(new char[] {currentChar(), currentCharAt(1)});
        return "--".equals(chars) || "//".equals(chars) || "/*".equals(chars);
    }
    
    protected boolean isVariableBegin() {
        return false;
    }
    
    protected boolean isSupportNChars() {
        return false;
    }
    
    private boolean isNCharBegin() {
        return 'N' == currentChar() && '\'' == currentCharAt(1);
    }
    
    private boolean isIdentifierBegin() {
        return isIdentifierBegin(currentChar());
    }
    
    private boolean isIdentifierBegin(final char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || '`' == ch || '_' == ch || '$' == ch;
    }
    
    private boolean isHexDecimalBegin() {
        return '0' == currentChar() && 'x' == currentCharAt(1);
    }
    
    private boolean isNumberBegin() {
        return isDigital(currentChar()) || ('.' == currentChar() && isDigital(currentCharAt(1)) && !isIdentifierBegin(currentCharAt(-1)));
    }
    
    private boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    private boolean isSymbolBegin() {
        return Symbol.isSymbol(currentChar());
    }
    
    private boolean isCharsBegin() {
        return '\'' == currentChar();
    }
    
    private boolean isAliasBegin() {
        return '\"' == currentChar();
    }
    
    private boolean isEOF() {
        return position >= input.length();
    }
    
    protected final char currentChar() {
        return currentCharAt(0);
    }
    
    protected final char currentCharAt(final int offset) {
        return position + offset >= input.length() ? (char) CharTypes.EOI : input.charAt(position + offset);
    }
    
    /**
     * 断言当前标记类型与传入值相等并跳过.
     * 
     * @param tokenType 待判断的标记类型
     */
    public final void accept(final TokenType tokenType) {
        if (token.getType() != tokenType) {
            throw new ParserException(this, tokenType);
        }
        nextToken();
    }
    
    /**
     * 判断当前语言标记是否和其中一个传入的标记相等.
     * 
     * @param tokenTypes 待判断的标记类型
     * @return 是否有相等的标记类型
     */
    public final boolean equal(final TokenType... tokenTypes) {
        for (TokenType each : tokenTypes) {
            if (each == token.getType()) {
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
                nextToken();
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
        while (!tokenTypeSet.contains(token.getType())) {
            nextToken();
        }
    }
}
