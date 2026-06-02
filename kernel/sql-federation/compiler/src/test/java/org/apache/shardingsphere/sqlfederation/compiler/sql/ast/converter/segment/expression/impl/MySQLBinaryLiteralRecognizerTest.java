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

import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.util.NlsString;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLBinaryLiteralRecognizerTest {
    
    @Test
    void assertNullTextReturnsEmpty() {
        assertFalse(MySQLBinaryLiteralRecognizer.recognize(null).isPresent());
    }
    
    @Test
    void assertEmptyTextReturnsEmpty() {
        assertFalse(MySQLBinaryLiteralRecognizer.recognize("").isPresent());
    }
    
    @Test
    void assertUnrelatedTextReturnsEmpty() {
        assertFalse(MySQLBinaryLiteralRecognizer.recognize("name").isPresent());
    }
    
    @Test
    void assertHexQuotedLowercaseDecodesBytes() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("x'48656c6c6f'"), new byte[]{0x48, 0x65, 0x6c, 0x6c, 0x6f});
    }
    
    @Test
    void assertHexQuotedUppercaseDecodesBytes() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("X'64'"), new byte[]{0x64});
    }
    
    @Test
    void assertHexQuotedEmptyDecodesEmpty() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("x''"), new byte[0]);
    }
    
    @Test
    void assertHexQuotedOddDigitsReturnsEmpty() {
        assertFalse(MySQLBinaryLiteralRecognizer.recognize("x'6'").isPresent());
    }
    
    @Test
    void assertHexPrefixedDecodesBytes() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("0x6465"), new byte[]{0x64, 0x65});
    }
    
    @Test
    void assertHexPrefixedUppercaseDecodesBytes() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("0XFF"), new byte[]{(byte) 0xFF});
    }
    
    @Test
    void assertBitQuotedDecodesPaddedBytes() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("b'1010'"), new byte[]{0x0A});
    }
    
    @Test
    void assertBitQuotedFullByteDecodesBytes() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("B'11111111'"), new byte[]{(byte) 0xFF});
    }
    
    @Test
    void assertBitPrefixedDecodesBytes() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("0b00000001"), new byte[]{0x01});
    }
    
    @Test
    void assertCharsetIntroducerReturnsEmpty() {
        assertFalse(MySQLBinaryLiteralRecognizer.recognize("_latin1'foo'").isPresent());
    }
    
    @Test
    void assertWhitespaceAroundTextDoesNotBlockRecognition() {
        assertHexBytes(MySQLBinaryLiteralRecognizer.recognize("  x'64'  "), new byte[]{0x64});
    }
    
    private void assertHexBytes(final Optional<SqlNode> actual, final byte[] expected) {
        assertTrue(actual.isPresent());
        SqlLiteral literal = (SqlLiteral) actual.get();
        NlsString nlsString = (NlsString) literal.getValue();
        byte[] actualBytes = nlsString.getValue().getBytes(StandardCharsets.ISO_8859_1);
        assertThat(actualBytes.length, is(expected.length));
        for (int i = 0; i < expected.length; i++) {
            assertThat(actualBytes[i], is(expected[i]));
        }
    }
}
