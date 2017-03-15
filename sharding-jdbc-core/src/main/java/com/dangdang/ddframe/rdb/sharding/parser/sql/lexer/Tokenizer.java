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

import lombok.RequiredArgsConstructor;

/**
 * 语言标记截取器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
final class Tokenizer {
    
    private final String input;
    
    private final Dictionary dictionary;
    
    private final int offset;
    
    int skipWhitespace() {
        int length = 0;
        while (CharType.isWhitespace(charAt(offset + length))) {
            length++;
        }
        return offset + length;
    }
    
    int skipComment() {
        char currentChar = charAt(offset);
        char nextChar = charAt(offset + 1);
        if (isSingleLineCommentBegin(currentChar, nextChar)) {
            return skipSingleLineComment(2);
        } else if ('#' == currentChar) {
            return skipSingleLineComment(1);
        } else if (isMultipleLineCommentBegin(currentChar, nextChar)) {
            return skipMultiLineComment();
        }
        return offset;
    }
    
    private boolean isSingleLineCommentBegin(final char ch, final char next) {
        return '/' == ch && '/' == next || '-' == ch && '-' == next;
    }
    
    private int skipSingleLineComment(final int commentFlagLength) {
        int length = commentFlagLength;
        while (!CharType.isEndOfInput(charAt(offset + length)) && '\n' != charAt(offset + length)) {
            length++;
        }
        return offset + length + 1;
    }
    
    private boolean isMultipleLineCommentBegin(final char ch, final char next) {
        return '/' == ch && '*' == next;
    }
    
    private int skipMultiLineComment() {
        return untilCommentAndHintTerminateSign(2);
    }
    
    int skipHint() {
        return untilCommentAndHintTerminateSign(3);
    }
    
    private int untilCommentAndHintTerminateSign(final int beginSignLength) {
        int length = beginSignLength;
        while (!isMultipleLineCommentEnd(charAt(offset + length), charAt(offset + length + 1))) {
            if (CharType.isEndOfInput(charAt(offset + length))) {
                throw new UnterminatedSignException("*/");
            }
            length++;
        }
        return offset + length + 2;
    }
    
    private boolean isMultipleLineCommentEnd(final char ch, final char next) {
        return '*' == ch && '/' == next;
    }
    
    Token scanUntil(final char terminatedSign, final TokenType defaultTokenType) {
        int length = 2;
        int position = offset + 1;
        while (terminatedSign != charAt(++position)) {
            if (CharType.isEndOfInput(charAt(position))) {
                throw new UnterminatedSignException(terminatedSign);
            }
            length++;
        }
        length++;
        String literals = input.substring(offset, offset + length);
        return new Token(dictionary.getToken(literals, defaultTokenType), literals, offset + length);
    }
    
    Token scanVariable() {
        int length = 1;
        if ('@' == charAt(offset + 1)) {
            length++;
        }
        while (isVariableChar(charAt(offset + length))) {
            length++;
        }
        return new Token(Literals.VARIABLE, input.substring(offset, offset + length), offset + length);
    }
    
    private boolean isVariableChar(final char ch) {
        return isIdentifierChar(ch) || '.' == ch;
    }
    
    Token scanIdentifier() {
        if ('`' == charAt(offset)) {
            return scanUntil('`', Literals.IDENTIFIER);
        }
        int length = 1;
        int position = offset;
        while (isIdentifierChar(charAt(++position))) {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        if (isAmbiguousIdentifier(literals)) {
            return new Token(processAmbiguousIdentifier(position, literals), literals, offset + length);
        }
        return new Token(dictionary.getToken(literals, Literals.IDENTIFIER), literals, offset + length);
    }
    
    private boolean isIdentifierChar(final char ch) {
        return CharType.isAlphabet(ch) || CharType.isDigital(ch) || '_' == ch || '$' == ch || '#' == ch;
    }
    
    private boolean isAmbiguousIdentifier(final String literals) {
        return DefaultKeyword.ORDER.name().equalsIgnoreCase(literals) || DefaultKeyword.GROUP.name().equalsIgnoreCase(literals);
    }
    
    private TokenType processAmbiguousIdentifier(final int position, final String literals) {
        int i = 0;
        while (CharType.isWhitespace(charAt(position + i))) {
            i++;
        }
        if (DefaultKeyword.BY.name().equalsIgnoreCase(String.valueOf(new char[] {charAt(position + i), charAt(position + i + 1)}))) {
            return dictionary.getToken(literals);
        }
        return Literals.IDENTIFIER;
    }
    
    Token scanHexDecimal() {
        int length = 3;
        int position = offset + length - 1;
        if ('-' == charAt(position)) {
            position++;
            length++;
        }
        while (isHex(charAt(++position))) {
            length++;
        }
        return new Token(Literals.HEX, input.substring(offset, offset + length), offset + length);
    }
    
    private boolean isHex(final char ch) {
        return ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f' || CharType.isDigital(ch);
    }
    
    Token scanNumber() {
        int length = 0;
        if ('-' == charAt(offset + length)) {
            length++;
        }
        length += getDigitalLength(offset + length);
        boolean isFloat = false;
        if ('.' == charAt(offset + length)) {
            // TODO 待确认 数字后面加两个点表示什么
            if ('.' == charAt(offset + length + 1)) {
                length++;
                return new Token(Literals.INT, input.substring(offset, offset + length), offset + length);
            }
            isFloat = true;
            length++;
            length += getDigitalLength(offset + length);
        }
        if (isScientificNotation(offset + length)) {
            isFloat = true;
            length++;
            if ('+' == charAt(offset + length) || '-' == charAt(offset + length)) {
                length++;
            }
            length += getDigitalLength(offset + length);
        }
        if (isBinaryNumber(offset + length)) {
            isFloat = true;
            length++;
        }
        return new Token(isFloat ? Literals.FLOAT : Literals.INT, input.substring(offset, offset + length), offset + length);
    }
    
    private int getDigitalLength(final int offset) {
        int result = 0;
        while (CharType.isDigital(charAt(offset + result))) {
            result++;
        }
        return result;
    }
    
    private boolean isScientificNotation(final int position) {
        char current = charAt(position);
        return 'e' == current || 'E' == current;
    }
    
    private boolean isBinaryNumber(final int position) {
        char current = charAt(position);
        return 'f' == current || 'F' == current || 'd' == current || 'D' == current;
    }
    
    Token scanChars() {
        return scanChars(charAt(offset));
    }
    
    private Token scanChars(final char charIdentifier) {
        int length = 1;
        while (charIdentifier != charAt(offset + length) || hasEscapeChar(charIdentifier, offset + length)) {
            if (offset + length >= input.length()) {
                throw new UnterminatedSignException(charIdentifier);
            }
            if (hasEscapeChar(charIdentifier, offset + length)) {
                length++;
            }
            length++;
        }
        length++;
        return new Token(Literals.CHARS, input.substring(offset + 1, offset + length - 1), offset + length);
    }
    
    private boolean hasEscapeChar(final char charIdentifier, final int position) {
        return charIdentifier == charAt(position) && charIdentifier == charAt(position + 1);
    }
    
    Token scanSymbol() {
        int length = 0;
        while (Symbol.isSymbol(charAt(offset + length))) {
            length++;
        }
        String literals = input.substring(offset, offset + length);
        Symbol symbol;
        while (null == (symbol = Symbol.literalsOf(literals))) {
            literals = input.substring(offset, offset + --length);
        }
        return new Token(symbol, literals, offset + length);
    }
    
    private char charAt(final int index) {
        return index >= input.length() ? (char) CharType.EOI : input.charAt(index);
    }
}
