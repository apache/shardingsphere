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

package org.apache.shardingsphere.infra.algorithm.core.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AlgorithmValueUtilsTest {
    
    @Test
    void assertConvertToBytesWithBytes() {
        byte[] value = "test".getBytes(StandardCharsets.UTF_8);
        assertThat(AlgorithmValueUtils.convertToBytes(value, StandardCharsets.UTF_8), is(value));
    }
    
    @Test
    void assertConvertToBytesWithCharset() {
        assertThat(AlgorithmValueUtils.convertToBytes("test", StandardCharsets.UTF_8), is("test".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertConvertToBytesWithDefaultCharset() {
        assertThat(AlgorithmValueUtils.convertToBytes("test", null), is("test".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertConvertToUTF8TextWithBytes() {
        assertThat(AlgorithmValueUtils.convertToUTF8Text("张三".getBytes(StandardCharsets.UTF_8)), is("张三"));
    }
    
    @Test
    void assertConvertToUTF8TextWithText() {
        assertThat(AlgorithmValueUtils.convertToUTF8Text("test"), is("test"));
    }
    
    @Test
    void assertConvertToTextWithCharset() {
        assertThat(AlgorithmValueUtils.convertToText("张三".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), is("张三"));
    }
    
    @Test
    void assertConvertToTextWithText() {
        assertThat(AlgorithmValueUtils.convertToText("test", StandardCharsets.UTF_8), is("test"));
    }
    
    @Test
    void assertConvertToTextWithDefaultCharset() {
        assertThat(AlgorithmValueUtils.convertToText("test".getBytes(StandardCharsets.UTF_8), null), is("test"));
    }
    
    @Test
    void assertConvertToPlainValueWithText() {
        byte[] value = "test".getBytes(StandardCharsets.UTF_16BE);
        assertThat(AlgorithmValueUtils.convertToPlainValue(value, StandardCharsets.UTF_16BE, false), is("test"));
    }
    
    @Test
    void assertConvertToPlainValueWithBytes() {
        byte[] value = "test".getBytes(StandardCharsets.UTF_8);
        assertThat((byte[]) AlgorithmValueUtils.convertToPlainValue(value, StandardCharsets.UTF_8, true), is(value));
    }
}
