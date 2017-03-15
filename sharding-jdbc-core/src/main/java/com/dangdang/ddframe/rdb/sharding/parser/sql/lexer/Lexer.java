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
    
    private boolean isEOF() {
        return position >= input.length();
    }
    
    protected final char currentChar() {
        return currentCharAt(0);
    }
    
    protected final char currentCharAt(final int offset) {
        return position + offset >= input.length() ? (char) CharType.EOI : input.charAt(position + offset);
    }
}
