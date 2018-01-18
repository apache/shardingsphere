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

package io.shardingjdbc.core.parsing.lexer;

import io.shardingjdbc.core.parsing.lexer.analyzer.CharType;
import io.shardingjdbc.core.parsing.lexer.analyzer.Dictionary;
import io.shardingjdbc.core.parsing.lexer.analyzer.Tokenizer;
import io.shardingjdbc.core.parsing.lexer.token.Assist;
import io.shardingjdbc.core.parsing.lexer.token.Token;
import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Lexical analysis.
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
    private Token currentToken;

    private Tokenizer tokenizer;
    
    /**
     * Analyse next token.
     */
    public final void nextToken() {
        initTokenizer();
        skipIgnoredToken();
        if (isVariableBegin()) {
            currentToken = tokenizer.scanVariable();
        } else if (isNCharBegin()) {
            ++offset;
            tokenizer.forward(1);
            currentToken = tokenizer.scanChars();
        } else if (isIdentifierBegin()) {
            currentToken = tokenizer.scanIdentifier();
        } else if (isHexDecimalBegin()) {
            currentToken = tokenizer.scanHexDecimal();
        } else if (isNumberBegin()) {
            currentToken = tokenizer.scanNumber();
        } else if (isSymbolBegin()) {
            currentToken = tokenizer.scanSymbol();
        } else if (isCharsBegin()) {
            currentToken = tokenizer.scanChars();
        } else if (isEnd()) {
            currentToken = new Token(Assist.END, "", offset);
        } else {
            throw new SQLParsingException(this, Assist.ERROR);
        }
        offset = currentToken.getEndPosition();
    }

    private void initTokenizer() {
        if (tokenizer == null) {
            tokenizer = new Tokenizer(input, dictionary);
        }
    }

    private void skipIgnoredToken() {
        offset = tokenizer.skipWhitespace();
        while (isHintBegin()) {
            offset = tokenizer.skipHint();
            offset = tokenizer.skipWhitespace();
        }
        while (isCommentBegin()) {
            offset = tokenizer.skipComment();
            offset = tokenizer.skipWhitespace();
        }
    }
    
    protected boolean isHintBegin() {
        return false;
    }
    
    protected boolean isCommentBegin() {
        char current = getCurrentChar(0);
        char next = getCurrentChar(1);
        return '/' == current && '/' == next || '-' == current && '-' == next || '/' == current && '*' == next;
    }
    
    protected boolean isVariableBegin() {
        return false;
    }
    
    protected boolean isSupportNChars() {
        return false;
    }
    
    private boolean isNCharBegin() {
        return isSupportNChars() && 'N' == getCurrentChar(0) && '\'' == getCurrentChar(1);
    }
    
    private boolean isIdentifierBegin() {
        return isIdentifierBegin(getCurrentChar(0));
    }
    
    private boolean isIdentifierBegin(final char ch) {
        return CharType.isAlphabet(ch) || '`' == ch || '_' == ch || '$' == ch;
    }
    
    private boolean isHexDecimalBegin() {
        return '0' == getCurrentChar(0) && 'x' == getCurrentChar(1);
    }
    
    private boolean isNumberBegin() {
        return CharType.isDigital(getCurrentChar(0)) || ('.' == getCurrentChar(0) && CharType.isDigital(getCurrentChar(1)) && !isIdentifierBegin(getCurrentChar(-1))
                || ('-' == getCurrentChar(0) && ('.' == getCurrentChar(1) || CharType.isDigital(getCurrentChar(1)))));
    }
    
    private boolean isSymbolBegin() {
        return CharType.isSymbol(getCurrentChar(0));
    }
    
    private boolean isCharsBegin() {
        return '\'' == getCurrentChar(0) || '\"' == getCurrentChar(0);
    }
    
    private boolean isEnd() {
        return offset >= input.length();
    }
    
    protected final char getCurrentChar(final int offset) {
        return this.offset + offset >= input.length() ? (char) CharType.EOI : input.charAt(this.offset + offset);
    }
}
