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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLArrayParameterDecoderTest {
    
    private static final PostgreSQLArrayParameterDecoder DECODER = new PostgreSQLArrayParameterDecoder();
    
    private static final String INT_ARRAY_STR = "{\"11\",\"12\"}";
    
    private static final String FLOAT_ARRAY_STR = "{\"11.1\",\"12.1\"}";
    
    @Test
    void assertParseInt2ArrayNormalTextMode() {
        short[] actual = DECODER.decodeInt2Array(INT_ARRAY_STR.getBytes(), false);
        assertThat(actual.length, is(2));
        assertThat(actual[0], is((short) 11));
        assertThat(actual[1], is((short) 12));
    }
    
    @Test
    void assertParseInt4ArrayNormalTextMode() {
        int[] actual = DECODER.decodeInt4Array(INT_ARRAY_STR.getBytes(), false);
        assertThat(actual.length, is(2));
        assertThat(actual[0], is(11));
        assertThat(actual[1], is(12));
    }
    
    @Test
    void assertParseInt8ArrayNormalTextMode() {
        long[] actual = DECODER.decodeInt8Array(INT_ARRAY_STR.getBytes(), false);
        assertThat(actual.length, is(2));
        assertThat(actual[0], is(11L));
        assertThat(actual[1], is(12L));
    }
    
    @Test
    void assertParseFloat4ArrayNormalTextMode() {
        float[] actual = DECODER.decodeFloat4Array(FLOAT_ARRAY_STR.getBytes(), false);
        assertThat(actual.length, is(2));
        assertThat(Float.compare(actual[0], 11.1F), is(0));
        assertThat(Float.compare(actual[1], 12.1F), is(0));
    }
    
    @Test
    void assertParseFloat8ArrayNormalTextMode() {
        double[] actual = DECODER.decodeFloat8Array(FLOAT_ARRAY_STR.getBytes(), false);
        assertThat(actual.length, is(2));
        assertThat(Double.compare(actual[0], 11.1D), is(0));
        assertThat(Double.compare(actual[1], 12.1D), is(0));
    }
    
    @Test
    void assertParseBoolArrayNormalTextMode() {
        boolean[] actual = DECODER.decodeBoolArray("{\"true\",\"false\"}".getBytes(), false);
        assertThat(actual.length, is(2));
        assertTrue(actual[0]);
        assertFalse(actual[1]);
    }
    
    @Test
    void assertParseStringArrayNormalTextMode() {
        String[] actual = DECODER.decodeStringArray("{\"a\",\"b\"}".getBytes(), false);
        assertThat(actual.length, is(2));
        assertThat(actual[0], is("a"));
        assertThat(actual[1], is("b"));
    }
    
    @Test
    void assertParseStringArrayWithEscapeTextMode() {
        String[] actual = DECODER.decodeStringArray("{\"\\\"a\",\"\\\\b\",\"c\"}".getBytes(), false);
        assertThat(actual.length, is(3));
        assertThat(actual[0], is("\"a"));
        assertThat(actual[1], is("\\b"));
        assertThat(actual[2], is("c"));
    }
    
    @Test
    void assertParseStringArrayWithNullTextMode() {
        String[] actual = DECODER.decodeStringArray("{\"a\",\"b\",NULL}".getBytes(), false);
        assertThat(actual.length, is(3));
        assertThat(actual[0], is("a"));
        assertThat(actual[1], is("b"));
        assertNull(actual[2]);
    }
}
