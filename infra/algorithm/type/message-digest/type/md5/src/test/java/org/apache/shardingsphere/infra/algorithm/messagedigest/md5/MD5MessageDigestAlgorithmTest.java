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

package org.apache.shardingsphere.infra.algorithm.messagedigest.md5;

import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.infra.algorithm.messagedigest.spi.MessageDigestAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class MD5MessageDigestAlgorithmTest {
    
    private MessageDigestAlgorithm digestAlgorithm;
    
    @BeforeEach
    void setUp() {
        digestAlgorithm = TypedSPILoader.getService(MessageDigestAlgorithm.class, "MD5");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideDigestArguments")
    void assertDigest(final String name, final Object plainValue, final Properties props, final String expected) {
        digestAlgorithm.init(props);
        assertThat(Hex.encodeHexString(digestAlgorithm.digest(plainValue)), is(expected));
    }
    
    private static Stream<Arguments> provideDigestArguments() {
        return Stream.of(
                Arguments.of("Text without salt", "test", new Properties(), "098f6bcd4621d373cade4e832627b4f6"),
                Arguments.of("UTF-8 bytes without salt", "test".getBytes(StandardCharsets.UTF_8), new Properties(), "098f6bcd4621d373cade4e832627b4f6"),
                Arguments.of("Configured salt", "test", PropertiesBuilder.build(new Property("salt", "202cb962ac5907")), "0c243d2934937738f36514035d95344a"),
                Arguments.of("Text with salt", "test", PropertiesBuilder.build(new Property("salt", "salt")), "315240c61218a4a861ec949166a85ef0"),
                Arguments.of("Chinese text", "中文", new Properties(), "a7bac2239fcdcb3a067903d8077c4a07"),
                Arguments.of("Chinese bytes with salt", "中文".getBytes(StandardCharsets.UTF_8), PropertiesBuilder.build(new Property("salt", "盐")), "3d4e5d6bae87398145d538c70a7ac58b"),
                Arguments.of("Invalid UTF-8 with salt", new byte[]{(byte) 0xC3, 0x28}, PropertiesBuilder.build(new Property("salt", "salt")), "bd3cece2bc4d3c492eefa3bd788005db"),
                Arguments.of("Surrogate pair across salt boundary", String.valueOf((char) 0xD83D), PropertiesBuilder.build(new Property("salt", String.valueOf((char) 0xDE00))),
                        "2a02eac39d716a70ecf37579185927b6"),
                Arguments.of("Number as text", 123, new Properties(), "202cb962ac59075b964b07152d234b70"));
    }
    
    @Test
    void assertDigestWithNullPlaintext() {
        assertNull(digestAlgorithm.digest(null));
    }
}
