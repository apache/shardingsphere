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

package com.alibaba.druid.sql.lexer;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

/**
 * 词法解析器.
 * 
 * @author zhangliang 
 */
public class Lexer {
    
    @Getter
    private final String input;
    
    private final Map<String, Token> tokenDictionary;
    
    @Getter
    private final Term term;
    
    @Getter
    @Setter
    private int currentPosition;
    
    @Getter
    @Setter
    private Token token;
    
    @Getter
    @Setter
    private String literals;
    
    private int varIndex = -1;
    
    public Lexer(final String input, final Map<String, Token> tokenDictionary) {
        this.input = input;
        this.tokenDictionary = tokenDictionary;
        term = new Term(input, tokenDictionary);
    }
    
    /**
     * 跳至下一个语言符号.
     */
    public final void nextToken() {
        while (isWhitespace()) {
            increaseCurrentPosition();
        }
        if (isVariable()) {
            scanVariable();
            return;
        }
        if (isIdentifier()) {
            scanIdentifier();
            return;
        }
        if (isHexDecimal()) {
            scanHexDecimal();
            return;
        }
        if (isNumber()) {
            scanNumber();
            return;
        }
        if (isHint()) {
            scanHint();
            return;
        }
        if (isComment()) {
            scanComment();
            return;
        }
        if (isTernarySymbol()) {
            scanSymbol(3);
            return;
        }
        if (isBinarySymbol()) {
            scanSymbol(2);
            return;
        }
        if (isUnarySymbol()) {
            scanSymbol(1);
            return;
        }
        if (isString()) {
            scanString();
            return;
        }
        if (isAlias()) {
            scanAlias();
            return;
        }
        if (isEOF()) {
            token = Token.EOF;
        } else {
            token = Token.ERROR;
        }
        literals = "";
    }
    
    private boolean isWhitespace() {
        return CharTypes.isWhitespace(charAt(currentPosition));
    }
    
    protected boolean isVariable() {
        char currentChar = charAt(currentPosition);
        char nextChar = charAt(currentPosition + 1);
        return ('$' == currentChar && '{' == nextChar)
                || '@' == currentChar
                || '#' == currentChar
                || (':' == currentChar && '=' != nextChar && nextChar != ':');
    }
    
    protected void scanVariable() {
        char nextChar = charAt(currentPosition + 1);
        if ('{' == nextChar) {
            term.scanContentUntil(currentPosition, '}', Token.VARIANT, false);
        } else if ('`' == nextChar) {
            term.scanContentUntil(currentPosition, '`', Token.VARIANT, false);
        } else if ('"' == nextChar) {
            term.scanContentUntil(currentPosition, '"', Token.VARIANT, false);
        } else {
            term.scanVariable(currentPosition);
        }
        setTermResult();
    }
    
    private boolean isIdentifier() {
        return isFirstIdentifierChar(charAt(currentPosition));
    }
    
    protected void scanIdentifier() {
        if ('`' == charAt(currentPosition)) {
            term.scanContentUntil(currentPosition, '`', Token.IDENTIFIER, false);
        } else {
            term.scanIdentifier(currentPosition);
        }
        setTermResult();
    }
    
    private boolean isFirstIdentifierChar(final char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || '`' == ch || '_' == ch || '$' == ch;
    }
    
    private boolean isHexDecimal() {
        return '0' == charAt(currentPosition) && 'x' == charAt(currentPosition + 1);
    }
    
    private void scanHexDecimal() {
        term.scanHexDecimal(currentPosition);
        setTermResult();
    }
    
    private boolean isNumber() {
        return isDigital(charAt(currentPosition)) || ('.' == charAt(currentPosition) && isDigital(charAt(currentPosition + 1)) && !isFirstIdentifierChar(charAt(currentPosition - 1)));
    }
    
    private boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    private void scanNumber() {
        term.scanNumber(currentPosition);
        setTermResult();
    }
    
    protected boolean isHint() {
        return false;
    }
    
    private void scanHint() {
        term.scanHint(currentPosition);
        setTermResult();
    }
    
    protected boolean isComment() {
        char currentChar = charAt(currentPosition);
        char nextChar = charAt(currentPosition + 1);
        return ('-' == currentChar && '-' == nextChar) || (('/' == currentChar && '/' == nextChar) || (currentChar == '/' && nextChar == '*'));
    }
    
    private void scanComment() {
        if (('/' == charAt(currentPosition) && '/' == charAt(currentPosition + 1)) || ('-' == charAt(currentPosition) && '-' == charAt(currentPosition + 1))) {
            term.scanSingleLineComment(currentPosition, 2);
        } else if ('#' == charAt(currentPosition)) {
            term.scanSingleLineComment(currentPosition, 1);
        } else if (charAt(currentPosition) == '/' && charAt(currentPosition + 1) == '*') {
            term.scanMultiLineComment(currentPosition);
        }
        setTermResult();
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
    
    private boolean isSymbol(final String symbols) {
        for (int i = 0; i < symbols.length(); i++) {
            if (symbols.charAt(i) != charAt(currentPosition + i)) {
                return false;
            }
        }
        return true;
    }
    
    private void scanSymbol(final int symbolLength) {
        term.scanSymbol(currentPosition, symbolLength);
        setTermResult();
    }
    
    private boolean isString() {
        return '\'' == charAt(currentPosition);
    }
    
    protected void scanString() {
        term.scanString(currentPosition);
        setTermResult();
    }
    
    private boolean isAlias() {
        return '\"' == charAt(currentPosition);
    }
    
    private void scanAlias() {
        term.scanContentUntil(currentPosition, '\"', Token.LITERAL_ALIAS, true);
        setTermResult();
    }
    
    private void setTermResult() {
        literals = term.getLiterals();
        token = term.getToken();
        currentPosition = term.getCurrentPosition();
    }
    
    private boolean isEOF() {
        return currentPosition >= input.length();
    }
    
    protected final char charAt(final int index) {
        return index >= input.length() ? (char) CharTypes.EOI : input.charAt(index);
    }
    
    protected final int increaseCurrentPosition() {
        return ++currentPosition;
    }
    
    public final boolean containsToken() {
        return tokenDictionary.containsValue(token);
    }
    
    public final boolean equalToken(final Token token) {
        return this.token == token;
    }
    
    public int nextVarIndex() {
        return ++varIndex;
    }
    
    public final void nextTokenCommaOrRightParen() {
        if (' ' == charAt(currentPosition)) {
            increaseCurrentPosition();
        }
        if (',' == charAt(currentPosition)) {
            increaseCurrentPosition();
            token = Token.COMMA;
            return;
        }
        if (')' == charAt(currentPosition)) {
            increaseCurrentPosition();
            token = Token.RIGHT_PAREN;
            return;
        }
        nextToken();
    }
    
    private static final long  MULTMIN_RADIX_TEN   = Long.MIN_VALUE / 10;
    private static final long  N_MULTMAX_RADIX_TEN = -Long.MAX_VALUE / 10;

    private final static int[] digits              = new int[(int) '9' + 1];

    static {
        for (int i = '0'; i <= '9'; ++i) {
            digits[i] = i - '0';
        }
    }
    
    // QS_TODO negative number is invisible for lexer
    public Number integerValue() {
        long result = 0;
        boolean negative = false;
        int i = term.getOffset(), max = term.getOffset() + term.getLength();
        long limit;
        long multmin;
        int digit;

        if (charAt(term.getOffset()) == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            i++;
        } else {
            limit = -Long.MAX_VALUE;
        }
        multmin = negative ? MULTMIN_RADIX_TEN : N_MULTMAX_RADIX_TEN;
        if (i < max) {
            digit = digits[charAt(i++)];
            result = -digit;
        }
        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = digits[charAt(i++)];
            if (result < multmin) {
                return new BigInteger(term.getValue());
            }
            result *= 10;
            if (result < limit + digit) {
                return new BigInteger(term.getValue());
            }
            result -= digit;
        }
        if (negative) {
            if (i > term.getOffset() + 1) {
                if (result >= Integer.MIN_VALUE) {
                    return (int) result;
                }
                return result;
            } else { /* Only got "-" */
                throw new NumberFormatException(term.getValue());
            }
        } else {
            result = -result;
            if (result <= Integer.MAX_VALUE) {
                return (int) result;
            }
            return result;
        }
    }
    
    public BigDecimal decimalValue() {
        return new BigDecimal(term.getValue().toCharArray());
    }
    
    public final boolean identifierEquals(final String text) {
        return Token.IDENTIFIER == token && literals.equalsIgnoreCase(text);
    }
}
