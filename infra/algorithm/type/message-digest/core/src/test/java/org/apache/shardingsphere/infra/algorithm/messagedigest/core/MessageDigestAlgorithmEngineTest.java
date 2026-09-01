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

package org.apache.shardingsphere.infra.algorithm.messagedigest.core;

import org.apache.shardingsphere.infra.algorithm.messagedigest.spi.MessageDigestAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageDigestAlgorithmEngineTest {
    
    @Test
    void assertDigestWithBase64Encoder() {
        MessageDigestAlgorithm digestAlgorithm = mock(MessageDigestAlgorithm.class);
        when(digestAlgorithm.digest("plain")).thenReturn("digest".getBytes(StandardCharsets.UTF_8));
        Object actualDigestValue = MessageDigestAlgorithmEngine.digest(digestAlgorithm, "plain", "BASE64", false);
        assertThat(actualDigestValue, is("ZGlnZXN0"));
    }
    
    @Test
    void assertDigestWithHexEncoder() {
        MessageDigestAlgorithm digestAlgorithm = mock(MessageDigestAlgorithm.class);
        when(digestAlgorithm.digest("plain")).thenReturn("digest".getBytes(StandardCharsets.UTF_8));
        Object actualDigestValue = MessageDigestAlgorithmEngine.digest(digestAlgorithm, "plain", "HEX", false);
        assertThat(actualDigestValue, is("646967657374"));
    }
    
    @Test
    void assertDigestWithPreferBinaryType() {
        MessageDigestAlgorithm digestAlgorithm = mock(MessageDigestAlgorithm.class);
        byte[] expectedDigestValue = "digest".getBytes(StandardCharsets.UTF_8);
        when(digestAlgorithm.digest("plain")).thenReturn(expectedDigestValue);
        Object actualDigestValue = MessageDigestAlgorithmEngine.digest(digestAlgorithm, "plain", "BASE64", true);
        assertThat((byte[]) actualDigestValue, is(expectedDigestValue));
    }
    
    @Test
    void assertDigestWithNullResult() {
        MessageDigestAlgorithm digestAlgorithm = mock(MessageDigestAlgorithm.class);
        when(digestAlgorithm.digest("plain")).thenReturn(null);
        assertNull(MessageDigestAlgorithmEngine.digest(digestAlgorithm, "plain", "BASE64", false));
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "base64-empty, 0, BASE64, 0",
            "base64-one-byte, 1, BASE64, 4",
            "base64-two-bytes, 2, BASE64, 4",
            "base64-three-bytes, 3, BASE64, 4",
            "base64-four-bytes, 4, BASE64, 8",
            "hex-three-bytes, 3, HEX, 6"
    })
    void assertCalculateEncodedLength(final String name, final int byteLength, final String encoder, final int expectedLength) {
        assertThat(MessageDigestAlgorithmEngine.calculateEncodedLength(byteLength, encoder), is(expectedLength));
    }
}
