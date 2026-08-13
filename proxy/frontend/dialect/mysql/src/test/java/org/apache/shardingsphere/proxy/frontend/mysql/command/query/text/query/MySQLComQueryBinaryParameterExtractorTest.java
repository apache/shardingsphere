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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MySQLComQueryBinaryParameterExtractorTest {
    
    @Test
    void assertExtractConnectorJBinaryLiteral() throws SQLException {
        byte[] content = {'\\', '0', '\\', '\'', '\\', '\\', (byte) 0xFF};
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(
                sql("INSERT INTO t VALUES (_binary'", content, "')"), StandardCharsets.UTF_8);
        assertThat(actual.getSql(), is("INSERT INTO t VALUES (?)"));
        assertArrayEquals(new byte[]{0, '\'', '\\', (byte) 0xFF}, getBlobBytes(actual.getBinaryLiteralValues().get(0)));
    }
    
    @Test
    void assertExtractBackslashEscapedMalformedLiteral() throws SQLException {
        byte[] content = {'\\', 'b', '\\', 'n', '\\', 'r', '\\', 't', '\\', 'Z', '\\', 'q', '\\', '%', '\\', '_', (byte) 0xFF};
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(
                sql("INSERT INTO t VALUES ('", content, "')"), StandardCharsets.UTF_8);
        assertThat(actual.getSql(), is("INSERT INTO t VALUES (?)"));
        assertArrayEquals(new byte[]{'\b', '\n', '\r', '\t', 0x1A, 'q', '\\', '%', '\\', '_', (byte) 0xFF}, getBlobBytes(actual.getBinaryLiteralValues().get(0)));
    }
    
    @Test
    void assertExtractMalformedLiteral() throws SQLException {
        byte[] content = {'f', 'o', '\'', '\'', (byte) 0xFF};
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(
                sql("INSERT INTO t VALUES ('", content, "')"), StandardCharsets.UTF_8);
        assertThat(actual.getSql(), is("INSERT INTO t VALUES (?)"));
        assertArrayEquals(new byte[]{'f', 'o', '\'', (byte) 0xFF}, getBlobBytes(actual.getBinaryLiteralValues().get(0)));
    }
    
    @Test
    void assertExtractOnlyMalformedLiteral() throws SQLException {
        byte[] sql = sql("SELECT _BiNaRy 'fo''o', '", new byte[]{(byte) 0xFF}, "'");
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(sql, StandardCharsets.UTF_8);
        assertThat(actual.getSql(), is("SELECT _BiNaRy 'fo''o', ?"));
        assertThat(actual.getBinaryLiteralValues().size(), is(1));
        assertArrayEquals(new byte[]{(byte) 0xFF}, getBlobBytes(actual.getBinaryLiteralValues().get(0)));
    }
    
    @Test
    void assertSkipValidLiteral() throws SQLException {
        String sql = "SELECT '😀', _binary'foo'";
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(sql.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        assertThat(actual.getBinaryLiteralValues(), empty());
    }
    
    @Test
    void assertIgnoreNonLiteralBinaryTokens() throws SQLException {
        String sql = "SELECT '_binary', `_binary'ignored`, \"_binary'ignored'\", 1 /* _binary'x' */ -- _binary'y'\n";
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(sql.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        assertThat(actual.getBinaryLiteralValues(), empty());
    }
    
    @Test
    void assertExtractMalformedLiteralWithMultibyteTrailBackslash() throws SQLException {
        Charset charset = Charset.forName("Shift_JIS");
        byte[] content = {(byte) 0x83, '\\', (byte) 0xFF};
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(sql("SELECT _binary'", content, "'"), charset);
        assertArrayEquals(content, getBlobBytes(actual.getBinaryLiteralValues().get(0)));
    }
    
    @Test
    void assertKeepMalformedSQLForParser() throws SQLException {
        byte[] sql = "SELECT _binary'foo".getBytes(StandardCharsets.UTF_8);
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(sql, StandardCharsets.UTF_8);
        assertThat(actual.getBinaryLiteralValues(), empty());
    }
    
    @Test
    void assertSkipDoubleQuotedLiteral() throws SQLException {
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(
                sql("SELECT _binary\"", new byte[]{(byte) 0xFF}, "\""), StandardCharsets.UTF_8);
        assertThat(actual.getBinaryLiteralValues(), empty());
    }
    
    @Test
    void assertSkipAdjacentLiteral() throws SQLException {
        MySQLComQueryBinaryParameterExtractor.ExtractionResult actual = MySQLComQueryBinaryParameterExtractor.extract(
                sql("SELECT _binary'", new byte[]{(byte) 0xFF}, "' 'foo'"), StandardCharsets.UTF_8);
        assertThat(actual.getBinaryLiteralValues(), empty());
    }
    
    private static byte[] sql(final String prefix, final byte[] content, final String suffix) {
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.US_ASCII);
        byte[] suffixBytes = suffix.getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream result = new ByteArrayOutputStream(prefixBytes.length + content.length + suffixBytes.length);
        result.write(prefixBytes, 0, prefixBytes.length);
        result.write(content, 0, content.length);
        result.write(suffixBytes, 0, suffixBytes.length);
        return result.toByteArray();
    }
    
    private static byte[] getBlobBytes(final Object value) throws SQLException {
        Blob blob = (Blob) value;
        return blob.getBytes(1, (int) blob.length());
    }
}
