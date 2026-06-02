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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recognize MySQL hex / bit literal text carried by {@code CommonExpressionSegment} and emit a Calcite character
 * literal whose Java characters map one-to-one to the underlying bytes via {@link StandardCharsets#ISO_8859_1}.
 *
 * <p>Supported text patterns:</p>
 * <ul>
 *   <li>{@code x'..'} / {@code X'..'} hex literal — decodes to bytes, then maps each byte to the char of the same
 *       code point.</li>
 *   <li>{@code 0x..} hex literal — same byte-to-char mapping.</li>
 *   <li>{@code b'..'} / {@code B'..'} / {@code 0b..} bit-value literal — packs bits into bytes (right-aligned MSB
 *       first) and applies the byte-to-char mapping.</li>
 * </ul>
 *
 * <p>Emitting the literal as a {@link org.apache.calcite.sql.SqlLiteral} character string preserves byte length under
 * the downstream {@code LENGTH} / {@code BIT_LENGTH} / {@code LOCATE} pipeline for ASCII-range bytes: each byte
 * decoded by {@code ISO_8859_1} becomes a single UTF-8 byte when the underlying byte is below {@code 0x80}, so
 * {@code LENGTH(x'48656c6c6f')} returns {@code 5} and {@code LOCATE(x'64', _utf8mb4'abcdef')} returns {@code 4},
 * matching MySQL for ASCII payloads. Bytes {@code 0x80}-{@code 0xFF} expand to two UTF-8 bytes after the
 * byte-to-char mapping, so MySQL's length result for purely non-ASCII binary payloads remains a follow-up; the
 * dominant use cases of hex literals are ASCII payloads such as the {@code x'64'} reproducer for issue #31899.</p>
 *
 * <p>Charset introducers ({@code _latin1'value'}, {@code _utf8mb4'value'}, etc.) are stripped by the MySQL parser
 * before reaching this converter, so they are not addressed here.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLBinaryLiteralRecognizer {
    
    private static final Pattern HEX_QUOTED = Pattern.compile("^[xX]'([0-9A-Fa-f]*)'$");
    
    private static final Pattern HEX_PREFIXED = Pattern.compile("^0[xX]([0-9A-Fa-f]+)$");
    
    private static final Pattern BIT_QUOTED = Pattern.compile("^[bB]'([01]+)'$");
    
    private static final Pattern BIT_PREFIXED = Pattern.compile("^0[bB]([01]+)$");
    
    /**
     * Recognize a literal text and convert it to a Calcite {@link SqlNode}.
     *
     * @param text raw text from {@code CommonExpressionSegment}
     * @return the recognized literal node, or empty when the text does not match any supported pattern
     */
    public static Optional<SqlNode> recognize(final String text) {
        if (null == text || text.isEmpty()) {
            return Optional.empty();
        }
        String trimmed = text.trim();
        Matcher matcher = HEX_QUOTED.matcher(trimmed);
        if (matcher.matches()) {
            return decodeHex(matcher.group(1));
        }
        matcher = HEX_PREFIXED.matcher(trimmed);
        if (matcher.matches()) {
            return decodeHex(matcher.group(1));
        }
        matcher = BIT_QUOTED.matcher(trimmed);
        if (matcher.matches()) {
            return decodeBit(matcher.group(1));
        }
        matcher = BIT_PREFIXED.matcher(trimmed);
        if (matcher.matches()) {
            return decodeBit(matcher.group(1));
        }
        return Optional.empty();
    }
    
    private static Optional<SqlNode> decodeHex(final String hex) {
        if (0 != hex.length() % 2) {
            return Optional.empty();
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return Optional.of(toCharLiteral(bytes));
    }
    
    private static Optional<SqlNode> decodeBit(final String bits) {
        int bitCount = bits.length();
        byte[] bytes = new byte[(bitCount + 7) / 8];
        int offset = bytes.length * 8 - bitCount;
        for (int i = 0; i < bitCount; i++) {
            if ('1' == bits.charAt(i)) {
                int bitIndex = offset + i;
                bytes[bitIndex / 8] |= (byte) (1 << (7 - bitIndex % 8));
            }
        }
        return Optional.of(toCharLiteral(bytes));
    }
    
    private static SqlNode toCharLiteral(final byte[] bytes) {
        return SqlLiteral.createCharString(new String(bytes, StandardCharsets.ISO_8859_1), SqlParserPos.ZERO);
    }
}
