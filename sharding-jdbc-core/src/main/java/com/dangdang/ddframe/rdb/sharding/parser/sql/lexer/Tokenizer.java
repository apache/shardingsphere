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

import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.oracle.lexer.OracleLiterals;
import com.dangdang.ddframe.rdb.sharding.parser.sql.dialect.sqlserver.lexer.SQLServerLiterals;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 处理词.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class Tokenizer {
    
    private final String input;
    
    private final Dictionary dictionary;
    
    private final int offset;
    
    private int length;
    
    @Getter(AccessLevel.PACKAGE)
    private String literals;
    
    @Getter(AccessLevel.PACKAGE)
    private TokenType tokenType;
    
    int getCurrentPosition() {
        return offset + length;
    }
    
    void scanContentUntil(final char terminatedSign, final TokenType defaultTokenType) {
        length = 2;
        int position = offset + 1;
        while (terminatedSign != charAt(++position)) {
            if (CharTypes.EOI == charAt(position)) {
                throw new UnterminatedSignException(terminatedSign);
            }
            length++;
        }
        length++;
        literals = input.substring(offset, offset + length);
        tokenType = dictionary.getToken(literals, defaultTokenType);
    }
    
    void scanVariable() {
        length = 1;
        int position = offset;
        if ('@' == charAt(position + 1)) {
            position++;
            length++;
        }
        while (isVariableChar(charAt(++position))) {
            length++;
        }
        literals = input.substring(offset, offset + length);
        tokenType = GeneralLiterals.VARIANT;
    }
    
    private boolean isVariableChar(final char ch) {
        return isIdentifierChar(ch) || '.' == ch;
    }
    
    void scanIdentifier() {
        length = 1;
        int position = offset;
        while (isIdentifierChar(charAt(++position))) {
            length++;
        }
        literals = input.substring(offset, offset + length);
        String upperCaseLiterals = literals.toUpperCase();
        if (DefaultKeyword.ORDER.toString().equals(upperCaseLiterals) || DefaultKeyword.GROUP.toString().equals(upperCaseLiterals)) {
            int i = 0;
            while (CharTypes.isWhitespace(charAt(position + i))) {
                i++;
            }
            if (DefaultKeyword.BY.toString().equalsIgnoreCase(String.valueOf(new char[] {charAt(position + i), charAt(position + i + 1)}))) {
                tokenType = dictionary.getToken(literals);
            } else {
                tokenType = GeneralLiterals.IDENTIFIER;
            }
        } else {
            tokenType = dictionary.getToken(literals, GeneralLiterals.IDENTIFIER);
        }
    }
    
    private boolean isIdentifierChar(final char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || '_' == ch || '$' == ch || '#' == ch;
    }
    
    void scanHexDecimal() {
        length = 3;
        int position = offset + length - 1;
        if ('-' == charAt(position)) {
            position++;
            length++;
        }
        while (isHex(charAt(++position))) {
            length++;
        }
        literals = input.substring(offset, offset + length);
        tokenType = GeneralLiterals.HEX;
    }
    
    private boolean isHex(final char ch) {
        return (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f') || (ch >= '0' && ch <= '9');
    }
    
    void scanNumber() {
        length = 0;
        int position = offset;
        if ('-' == charAt(position)) {
            position++;
            length++;
        }
        while (isDigital(charAt(position))) {
            length++;
            position++;
        }
        boolean isFloat = false;
        if ('.' == charAt(position)) {
            // TODO 待确认 数字后面加两个点表示什么
            if ('.' == charAt(position + 1)) {
                length++;
                literals = input.substring(offset, offset + length);
                tokenType = GeneralLiterals.INT;
                return;
            }
            isFloat = true;
            position++;
            length++;
            while (isDigital(charAt(position))) {
                position++;
                length++;
            }
        }
        if ('e' == charAt(position) || 'E' == charAt(position)) {
            isFloat = true;
            position++;
            length++;
            if ('+' == charAt(position)) {
                position++;
                length++;
            }
            if ('-' == charAt(position)) {
                position++;
                length++;
            }
            while (isDigital(charAt(position))) {
                position++;
                length++;
            }
        }
        // TODO ORACLE拆分
        if ('f' == charAt(position) || 'F' == charAt(position)) {
            length++;
            literals = input.substring(offset, offset + length);
            tokenType = OracleLiterals.BINARY_FLOAT;
            return;
        }
        if ('d' == charAt(position) || 'D' == charAt(position)) {
            length++;
            literals = input.substring(offset, offset + length);
            tokenType = OracleLiterals.BINARY_DOUBLE;
            return;
        }
        literals = input.substring(offset, offset + length);
        tokenType = isFloat ? GeneralLiterals.FLOAT : GeneralLiterals.INT;
    }
    
    private boolean isDigital(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    void scanChars() {
        length = 1;
        int position = offset + length;
        while ('\'' != charAt(position) || hasEscapeChar(position)) {
            if (position >= input.length()) {
                throw new UnterminatedSignException('\'');
            }
            if (hasEscapeChar(position)) {
                length++;
                position++;
            }
            length++;
            position++;
        }
        length++;
        literals = input.substring(offset + 1, offset + length - 1);
        tokenType = GeneralLiterals.CHARS;
    }
    
    void scanNChars() {
        length = 2;
        scanChars();
        tokenType = SQLServerLiterals.NCHARS;
    }
    
    private boolean hasEscapeChar(final int position) {
        return '\'' == charAt(position) && '\'' == charAt(position + 1);
    }
    
    void scanHint() {
        length = 4;
        int position = offset + length;
        while (!('*' == charAt(position) && '/' == charAt(position + 1))) {
            if (CharTypes.EOI == charAt(position)) {
                throw new UnterminatedSignException("*/");
            }
            position++;
            length++;
        }
        length += 2;
        literals = input.substring(offset + 4, offset + length - 2);
        tokenType = GeneralLiterals.HINT;
    }
    
    void scanSingleLineComment(final int commentFlagLength) {
        int position = offset + commentFlagLength + 1;
        length = commentFlagLength + 1;
        while (CharTypes.EOI != charAt(position) && '\n' != charAt(position)) {
            position++;
            length++;
        }
        literals = input.substring(offset, offset + length);
        tokenType = GeneralLiterals.COMMENT;
    }
    
    void scanMultiLineComment() {
        length = 3;
        int position = offset + length;
        while (!('*' == charAt(position) && '/' == charAt(position + 1))) {
            if (CharTypes.EOI == charAt(position)) {
                throw new UnterminatedSignException("*/");
            }
            position++;
            length++;
        }
        length += 2;
        literals = input.substring(offset, offset + length);
        tokenType = GeneralLiterals.COMMENT;
    }
    
    void scanSymbol(final int charLength) {
        int position = offset;
        length = 0;
        char[] symbolChars = new char[charLength];
        for (int i = 0; i < charLength; i++) {
            symbolChars[i] = charAt(position++);
            length++;
        }
        literals = String.valueOf(symbolChars);
        tokenType = Symbol.literalsOf(literals);
    }
    
    private char charAt(final int index) {
        return index >= input.length() ? (char) CharTypes.EOI : input.charAt(index);
    }
}
