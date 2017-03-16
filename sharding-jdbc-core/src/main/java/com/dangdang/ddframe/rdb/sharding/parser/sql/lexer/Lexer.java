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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
    
    private int offset;
    
    @Getter
    private Token token;
    
    /**
     * 分析下一个词法标记.
     */
    public final void nextToken() {
        skipIgnoredToken();
        if (isVariableBegin()) {
            token = new Tokenizer(input, dictionary, offset).scanVariable();
        } else if (isSupportNChars() && isNCharBegin()) {
            token = new Tokenizer(input, dictionary, ++offset).scanChars();
        } else if (isIdentifierBegin()) {
            token = new Tokenizer(input, dictionary, offset).scanIdentifier();
        } else if (isHexDecimalBegin()) {
            token = new Tokenizer(input, dictionary, offset).scanHexDecimal();
        } else if (isNumberBegin()) {
            token = new Tokenizer(input, dictionary, offset).scanNumber();
        } else if (isSymbolBegin()) {
            token = new Tokenizer(input, dictionary, offset).scanSymbol();
        } else if (isCharsBegin()) {
            token = new Tokenizer(input, dictionary, offset).scanChars();
        } else if (isEnd()) {
            token = new Token(Assist.END, "", offset);
        } else {
            token = new Token(Assist.ERROR, "", offset);
        }
        offset = token.getEndPosition();
    }
    
    private void skipIgnoredToken() {
        offset = new Tokenizer(input, dictionary, offset).skipWhitespace();
        while (isHintBegin()) {
            offset = new Tokenizer(input, dictionary, offset).skipHint();
            offset = new Tokenizer(input, dictionary, offset).skipWhitespace();
        }
        while (isCommentBegin()) {
            offset = new Tokenizer(input, dictionary, offset).skipComment();
            offset = new Tokenizer(input, dictionary, offset).skipWhitespace();
        }
    }
    
    protected boolean isHintBegin() {
        return false;
    }
    
    protected boolean isCommentBegin() {
        char current = currentChar();
        char next = currentCharAt(1);
        return '/' == current && '/' == next || '-' == current && '-' == next || '/' == current && '*' == next;
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
        return CharType.isAlphabet(ch) || '`' == ch || '_' == ch || '$' == ch;
    }
    
    private boolean isHexDecimalBegin() {
        return '0' == currentChar() && 'x' == currentCharAt(1);
    }
    
    private boolean isNumberBegin() {
        return CharType.isDigital(currentChar()) || ('.' == currentChar() && CharType.isDigital(currentCharAt(1)) && !isIdentifierBegin(currentCharAt(-1)));
    }
    
    private boolean isSymbolBegin() {
        return Symbol.isSymbol(currentChar());
    }
    
    private boolean isCharsBegin() {
        return '\'' == currentChar() || '\"' == currentChar();
    }
    
    private boolean isEnd() {
        return offset >= input.length();
    }
    
    protected final char currentChar() {
        return currentCharAt(0);
    }
    
    protected final char currentCharAt(final int offset) {
        return this.offset + offset >= input.length() ? (char) CharType.EOI : input.charAt(this.offset + offset);
    }
}
