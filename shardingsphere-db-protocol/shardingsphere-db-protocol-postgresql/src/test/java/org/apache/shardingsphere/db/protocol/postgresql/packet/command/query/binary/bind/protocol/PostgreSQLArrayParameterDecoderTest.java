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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class PostgreSQLArrayParameterDecoderTest {
    
    private static final PostgreSQLArrayParameterDecoder DECODER = new PostgreSQLArrayParameterDecoder();
    
    private static final String INT_ARRAY_STR = "{\"11\",\"12\"}";
    
    private static final String FLOAT_ARRAY_STR = "{\"11.1\",\"12.1\"}";
    
    @Test
    public void assertParseInt2ArrayNormalTextMode() {
        short[] result = DECODER.decodeInt2Array(INT_ARRAY_STR.getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(2));
        assertThat(result[0], is((short) 11));
        assertThat(result[1], is((short) 12));
    }
    
    @Test
    public void assertParseInt4ArrayNormalTextMode() {
        int[] result = DECODER.decodeInt4Array(INT_ARRAY_STR.getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(2));
        assertThat(result[0], is(11));
        assertThat(result[1], is(12));
    }
    
    @Test
    public void assertParseInt8ArrayNormalTextMode() {
        long[] result = DECODER.decodeInt8Array(INT_ARRAY_STR.getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(2));
        assertThat(result[0], is(11L));
        assertThat(result[1], is(12L));
    }
    
    @Test
    public void assertParseFloat4ArrayNormalTextMode() {
        float[] result = DECODER.decodeFloat4Array(FLOAT_ARRAY_STR.getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(2));
        assertThat(Float.compare(result[0], 11.1F), is(0));
        assertThat(Float.compare(result[1], 12.1F), is(0));
    }
    
    @Test
    public void assertParseFloat8ArrayNormalTextMode() {
        double[] result = DECODER.decodeFloat8Array(FLOAT_ARRAY_STR.getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(2));
        assertThat(Double.compare(result[0], 11.1D), is(0));
        assertThat(Double.compare(result[1], 12.1D), is(0));
    }
    
    @Test
    public void assertParseBoolArrayNormalTextMode() {
        boolean[] result = DECODER.decodeBoolArray("{\"true\",\"false\"}".getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(2));
        assertThat(result[0], is(true));
        assertThat(result[1], is(false));
    }
    
    @Test
    public void assertParseStringArrayNormalTextMode() {
        String[] result = DECODER.decodeStringArray("{\"a\",\"b\"}".getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(2));
        assertThat(result[0], is("a"));
        assertThat(result[1], is("b"));
    }
    
    @Test
    public void assertParseStringArrayWithEscapeTextMode() {
        String[] result = DECODER.decodeStringArray("{\"\\\"a\",\"\\\\b\",\"c\"}".getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(3));
        assertThat(result[0], is("\"a"));
        assertThat(result[1], is("\\b"));
        assertThat(result[2], is("c"));
    }
    
    @Test
    public void assertParseStringArrayWithNullTextMode() {
        String[] result = DECODER.decodeStringArray("{\"a\",\"b\",NULL}".getBytes(), false);
        assertNotNull(result);
        assertThat(result.length, is(3));
        assertThat(result[0], is("a"));
        assertThat(result[1], is("b"));
        assertNull(result[2]);
    }
    
}
