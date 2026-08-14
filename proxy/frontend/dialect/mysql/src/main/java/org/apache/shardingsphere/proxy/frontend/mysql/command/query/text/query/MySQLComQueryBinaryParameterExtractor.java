/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Binary parameter extractor for MySQL COM_QUERY.
 *
 * <p>Rewrite unambiguous single-quoted literals containing malformed bytes to placeholders and preserve their values as {@link SerialBlob} parameters.
 * Backslash escapes follow the current MySQL session SQL mode; literals with grammar-dependent semantics are left unchanged.</p>
 */
final class MySQLComQueryBinaryParameterExtractor {
    
    private static final byte[] BINARY_INTRODUCER = "_binary".getBytes(StandardCharsets.US_ASCII);
    
    private final byte[] sql;
    
    private final Charset charset;
    
    private final boolean noBackslashEscapes;
    
    private final CharsetDecoder decoder;
    
    private final ByteBuffer input;
    
    private final CharBuffer output;
    
    private MySQLComQueryBinaryParameterExtractor(final byte[] sql, final Charset charset, final boolean noBackslashEscapes) {
        this.sql = sql;
        this.charset = charset;
        this.noBackslashEscapes = noBackslashEscapes;
        decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
        input = ByteBuffer.wrap(sql);
        output = CharBuffer.allocate(2);
    }
    
    static ExtractionResult extract(final byte[] sql, final Charset charset, final boolean noBackslashEscapes) throws SQLException {
        return new MySQLComQueryBinaryParameterExtractor(sql, charset, noBackslashEscapes).extract();
    }
    
    private ExtractionResult extract() throws SQLException {
        Collection<QuotedLiteral> parameterLiterals = findParameterLiterals();
        if (parameterLiterals.isEmpty()) {
            return new ExtractionResult(new String(sql, charset), Collections.emptyList());
        }
        ByteArrayOutputStream sql = new ByteArrayOutputStream(this.sql.length);
        List<Object> binaryLiteralValues = new ArrayList<>(parameterLiterals.size());
        int offset = 0;
        for (QuotedLiteral each : parameterLiterals) {
            int replacementStart = findBinaryIntroducerStart(each.startIndex);
            replacementStart = 0 <= replacementStart ? replacementStart : each.startIndex;
            sql.write(this.sql, offset, replacementStart - offset);
            sql.write('?');
            binaryLiteralValues.add(new SerialBlob(unescape(each.startIndex + 1, each.endIndex)));
            offset = each.endIndex + 1;
        }
        sql.write(this.sql, offset, this.sql.length - offset);
        return new ExtractionResult(new String(sql.toByteArray(), charset), binaryLiteralValues);
    }
    
    private Collection<QuotedLiteral> findParameterLiterals() {
        Collection<QuotedLiteral> result = new LinkedList<>();
        QuotedLiteral previousLiteral = null;
        boolean previousLiteralIsParameter = false;
        for (int i = 0; i < sql.length;) {
            if ('#' == sql[i]) {
                i = skipLineComment(i + 1);
            } else if (isDashComment(i)) {
                i = skipLineComment(i + 2);
            } else if (isBlockComment(i)) {
                i = skipBlockComment(i + 2);
            } else if ('`' == sql[i] || '"' == sql[i] || '\'' == sql[i]) {
                QuotedLiteral literal = findQuotedLiteral(i, sql[i]);
                if ('\'' == sql[i] && !literal.isClosed()) {
                    return Collections.emptyList();
                }
                boolean parameterLiteral = isParameterLiteral(sql[i], literal);
                if (null != previousLiteral && (previousLiteralIsParameter || parameterLiteral) && areAdjacent(previousLiteral, literal)) {
                    return Collections.emptyList();
                }
                if (parameterLiteral) {
                    result.add(literal);
                }
                previousLiteral = literal;
                previousLiteralIsParameter = parameterLiteral;
                i = literal.isClosed() ? literal.endIndex + 1 : sql.length;
            } else {
                int characterLength = readCharacterLength(i);
                i += 0 < characterLength ? characterLength : 1;
            }
        }
        return result;
    }
    
    private boolean isParameterLiteral(final byte quote, final QuotedLiteral literal) {
        return '\'' == quote && literal.containsMalformedBytes;
    }
    
    private QuotedLiteral findQuotedLiteral(final int startIndex, final byte quote) {
        boolean containsMalformedBytes = false;
        for (int i = startIndex + 1; i < sql.length;) {
            int characterLength = readCharacterLength(i);
            if (0 > characterLength) {
                containsMalformedBytes = true;
                i++;
            } else if (1 < characterLength) {
                i += characterLength;
            } else if (!noBackslashEscapes && '\\' == sql[i]) {
                int escapedCharacterLength = i + 1 < sql.length ? readCharacterLength(i + 1) : 0;
                if (0 > escapedCharacterLength) {
                    containsMalformedBytes = true;
                }
                i += 1 + Math.max(1, escapedCharacterLength);
            } else if (quote != sql[i]) {
                i++;
            } else if (i + 1 < sql.length && quote == sql[i + 1]) {
                i += 2;
            } else {
                return new QuotedLiteral(startIndex, i, containsMalformedBytes);
            }
        }
        return new QuotedLiteral(startIndex, -1, containsMalformedBytes);
    }
    
    private boolean areAdjacent(final QuotedLiteral previousLiteral, final QuotedLiteral currentLiteral) {
        return containsOnlyTrivia(previousLiteral.endIndex + 1, findLiteralTokenStart(currentLiteral.startIndex));
    }
    
    private int findLiteralTokenStart(final int quoteIndex) {
        int prefixEnd = quoteIndex;
        while (0 < prefixEnd && isWhitespace(sql[prefixEnd - 1])) {
            prefixEnd--;
        }
        int prefixStart = prefixEnd;
        while (0 < prefixStart && isIdentifierCharacter(prefixStart - 1)) {
            prefixStart--;
        }
        boolean hasCharacterSetIntroducer = prefixStart < prefixEnd && ('_' == sql[prefixStart] || prefixEnd - prefixStart == 1 && 'n' == toLowerCase(sql[prefixStart]));
        return hasCharacterSetIntroducer ? prefixStart : quoteIndex;
    }
    
    private boolean containsOnlyTrivia(final int startIndex, final int endIndex) {
        for (int i = startIndex; i < endIndex;) {
            if (isWhitespace(sql[i])) {
                i++;
            } else if ('#' == sql[i]) {
                i = skipLineComment(i + 1);
            } else if (isDashComment(i)) {
                i = skipLineComment(i + 2);
            } else if (isBlockComment(i)) {
                i = skipBlockComment(i + 2);
            } else {
                return false;
            }
        }
        return true;
    }
    
    private int readCharacterLength(final int index) {
        if (0 <= sql[index]) {
            return 1;
        }
        decoder.reset();
        input.limit(sql.length);
        input.position(index);
        output.clear();
        for (int limit = index + 1; limit <= sql.length; limit++) {
            input.limit(limit);
            CoderResult decodeResult = decoder.decode(input, output, false);
            if (decodeResult.isError()) {
                return -1;
            }
            if (0 < output.position()) {
                return input.position() - index;
            }
        }
        CoderResult decodeResult = decoder.decode(input, output, true);
        return decodeResult.isError() || 0 == output.position() ? -1 : input.position() - index;
    }
    
    private int findBinaryIntroducerStart(final int quoteIndex) {
        int introducerEnd = quoteIndex;
        while (0 < introducerEnd && isWhitespace(sql[introducerEnd - 1])) {
            introducerEnd--;
        }
        int introducerStart = introducerEnd - BINARY_INTRODUCER.length;
        if (0 > introducerStart || isIdentifierCharacter(introducerStart - 1)) {
            return -1;
        }
        for (int i = 0; i < BINARY_INTRODUCER.length; i++) {
            if (toLowerCase(sql[introducerStart + i]) != BINARY_INTRODUCER[i]) {
                return -1;
            }
        }
        return introducerStart;
    }
    
    private boolean isIdentifierCharacter(final int index) {
        if (0 > index) {
            return false;
        }
        int value = Byte.toUnsignedInt(sql[index]);
        byte lowerCaseValue = toLowerCase(sql[index]);
        return 0x80 <= value || '_' == value || '$' == value || '0' <= value && '9' >= value || 'a' <= lowerCaseValue && 'z' >= lowerCaseValue;
    }
    
    private static byte toLowerCase(final byte value) {
        return 'A' <= value && 'Z' >= value ? (byte) (value + 'a' - 'A') : value;
    }
    
    private static boolean isWhitespace(final byte value) {
        return ' ' == value || '\t' == value || '\n' == value || '\r' == value || '\f' == value;
    }
    
    private boolean isDashComment(final int index) {
        return index + 2 < sql.length && '-' == sql[index] && '-' == sql[index + 1] && Byte.toUnsignedInt(sql[index + 2]) <= ' ';
    }
    
    private boolean isBlockComment(final int index) {
        return index + 1 < sql.length && '/' == sql[index] && '*' == sql[index + 1];
    }
    
    private int skipLineComment(final int startIndex) {
        int result = startIndex;
        while (result < sql.length && '\n' != sql[result] && '\r' != sql[result]) {
            result++;
        }
        return result;
    }
    
    private int skipBlockComment(final int startIndex) {
        for (int i = startIndex; i + 1 < sql.length; i++) {
            if ('*' == sql[i] && '/' == sql[i + 1]) {
                return i + 2;
            }
        }
        return sql.length;
    }
    
    private byte[] unescape(final int startIndex, final int endIndex) {
        ByteArrayOutputStream result = new ByteArrayOutputStream(endIndex - startIndex);
        for (int i = startIndex; i < endIndex;) {
            int characterLength = readCharacterLength(i);
            if (1 < characterLength) {
                result.write(sql, i, characterLength);
                i += characterLength;
            } else if ('\'' == sql[i] && i + 1 < endIndex && '\'' == sql[i + 1]) {
                result.write('\'');
                i += 2;
            } else if (noBackslashEscapes || '\\' != sql[i] || i + 1 >= endIndex) {
                result.write(sql[i]);
                i++;
            } else {
                int escapedCharacterLength = readCharacterLength(i + 1);
                if (1 < escapedCharacterLength) {
                    result.write(sql, i + 1, escapedCharacterLength);
                    i += escapedCharacterLength + 1;
                } else {
                    writeEscapedByte(result, sql[i + 1]);
                    i += 2;
                }
            }
        }
        return result.toByteArray();
    }
    
    private static void writeEscapedByte(final ByteArrayOutputStream output, final byte value) {
        switch (value) {
            case '0':
                output.write(0);
                break;
            case 'b':
                output.write('\b');
                break;
            case 'n':
                output.write('\n');
                break;
            case 'r':
                output.write('\r');
                break;
            case 't':
                output.write('\t');
                break;
            case 'Z':
                output.write(0x1A);
                break;
            case '%':
            case '_':
                output.write('\\');
                output.write(value);
                break;
            default:
                output.write(value);
                break;
        }
    }
    
    @RequiredArgsConstructor
    @Getter
    static final class ExtractionResult {
        
        private final String sql;
        
        private final List<Object> binaryLiteralValues;
    }
    
    @RequiredArgsConstructor
    private static final class QuotedLiteral {
        
        private final int startIndex;
        
        private final int endIndex;
        
        private final boolean containsMalformedBytes;
        
        private boolean isClosed() {
            return 0 <= endIndex;
        }
    }
}
