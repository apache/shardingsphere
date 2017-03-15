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
        skipWhitespace();
        skipComment();
        skipHint();
        if (isVariableBegin()) {
            token = scanVariable();
        } else if (isSupportNChars() && isNCharBegin()) {
            position++;
            token = scanChars();
        } else if (isIdentifierBegin()) {
            token = scanIdentifier();
        } else if (isHexDecimalBegin()) {
            token = scanHexDecimal();
        } else if (isNumberBegin()) {
            token = scanNumber();
        } else if (isTernarySymbol()) {
            token = scanSymbol(3);
        } else if (isBinarySymbol()) {
            token = scanSymbol(2);
        } else if (isUnarySymbol()) {
            token = scanSymbol(1);
        } else if (isCharsBegin()) {
            token = scanChars();
        } else if (isAliasBegin()) {
            token = scanAlias();
        } else if (isEOF()) {
            token = new Token(Assist.EOF, "", position);
        } else {
            token = new Token(Assist.ERROR, "", position);
        }
        position = token.getEndPosition();
    }
    
    private void skipWhitespace() {
        while (CharTypes.isWhitespace(currentChar())) {
            position++;
        }
    }
    
    private void skipComment() {
        while (isCommentBegin()) {
            position = scanComment().getEndPosition();
            skipWhitespace();
        }
    }
    
    private void skipHint() {
        while (isHintBegin()) {
            position = scanHint().getEndPosition();
            skipWhitespace();
        }
    }
    
    protected boolean isVariableBegin() {
        return false;
    }
    
    private Token scanVariable() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position);
        tokenizer.scanVariable();
        return new Token(tokenizer);
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
    
    private Token scanIdentifier() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position);
        if ('`' == currentChar()) {
            tokenizer.scanUntil('`', Literals.IDENTIFIER);
        } else {
            tokenizer.scanIdentifier();
        }
        return new Token(tokenizer);
    }
    
    private boolean isHexDecimalBegin() {
        return '0' == currentChar() && 'x' == currentCharAt(1);
    }
    
    private Token scanHexDecimal() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position);
        tokenizer.scanHexDecimal();
        return new Token(tokenizer);
    }
    
    private boolean isNumberBegin() {
        return isDigital(currentChar()) || ('.' == currentChar() && isDigital(currentCharAt(1)) && !isIdentifierBegin(currentCharAt(-1)));
    }
    
    private boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    private Token scanNumber() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position);
        tokenizer.scanNumber();
        return new Token(tokenizer);
    }
    
    protected boolean isHintBegin() {
        return false;
    }
    
    private Token scanHint() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position - 1);
        tokenizer.scanHint();
        return new Token(tokenizer);
    }
    
    protected boolean isCommentBegin() {
        char currentChar = currentChar();
        char nextChar = currentCharAt(1);
        return ('-' == currentChar && '-' == nextChar) || (('/' == currentChar && '/' == nextChar) || (currentChar == '/' && nextChar == '*'));
    }
    
    private Token scanComment() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position - 1);
        if (('/' == currentChar() && '/' == currentCharAt(1)) || ('-' == currentChar() && '-' == currentCharAt(1))) {
            tokenizer.scanSingleLineComment(2);
        } else if ('#' == currentChar()) {
            tokenizer.scanSingleLineComment(1);
        } else if ('/' == currentChar() && '*' == currentCharAt(1)) {
            tokenizer.scanMultiLineComment();
        }
        return new Token(tokenizer);
    }
    
    private boolean isTernarySymbol() {
        return isSymbol(":=:") || isSymbol("<=>");
    }
    
    private boolean isBinarySymbol() {
        return isSymbol(":=") || isSymbol("..") || isSymbol("&&") || isSymbol("||")
                || isSymbol(">=") || isSymbol("<=") || isSymbol("<>") || isSymbol(">>") || isSymbol("<<")
                || isSymbol("!=") || isSymbol("!>") || isSymbol("!<");
    }
    
    private boolean isUnarySymbol() {
        return isSymbol("(") || isSymbol(")") || isSymbol("[") || isSymbol("]") || isSymbol("{") || isSymbol("}")
                || isSymbol("+") || isSymbol("-") || isSymbol("*") || isSymbol("/") || isSymbol("%") || isSymbol("^")
                || isSymbol("=") || isSymbol(">") || isSymbol("<") || isSymbol("~") || isSymbol("!") || isSymbol("?")
                || isSymbol("&") || isSymbol("|") || isSymbol(".") || isSymbol(":") || isSymbol("#") || isSymbol(",") || isSymbol(";");
    }
    
    private boolean isSymbol(final String symbol) {
        for (int i = 0; i < symbol.length(); i++) {
            if (symbol.charAt(i) != currentCharAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    private Token scanSymbol(final int symbolLength) {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position);
        tokenizer.scanSymbol(symbolLength);
        return new Token(tokenizer);
    }
    
    private boolean isCharsBegin() {
        return '\'' == currentChar();
    }
    
    private Token scanChars() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position);
        tokenizer.scanChars();
        return new Token(tokenizer);
    }
    
    private boolean isAliasBegin() {
        return '\"' == currentChar();
    }
    
    private Token scanAlias() {
        Tokenizer tokenizer = new Tokenizer(input, dictionary, position);
        tokenizer.scanUntil('\"', Literals.ALIAS);
        return new Token(tokenizer);
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
    
    /**
     * 跳过小括号内所有的语言符号.
     *
     * @return 小括号内所有的语言符号
     */
    public final String skipParentheses() {
        StringBuilder result = new StringBuilder("");
        int count = 0;
        if (Symbol.LEFT_PAREN == token.getType()) {
            int beginPosition = position;
            result.append(Symbol.LEFT_PAREN.getLiterals());
            nextToken();
            while (true) {
                if (Assist.EOF == token.getType() || (Symbol.RIGHT_PAREN == token.getType() && 0 == count)) {
                    break;
                }
                if (Symbol.LEFT_PAREN == token.getType()) {
                    count++;
                } else if (Symbol.RIGHT_PAREN == token.getType()) {
                    count--;
                }
                nextToken();
            }
            result.append(input.substring(beginPosition, position));
            nextToken();
        }
        return result.toString();
    }
}
