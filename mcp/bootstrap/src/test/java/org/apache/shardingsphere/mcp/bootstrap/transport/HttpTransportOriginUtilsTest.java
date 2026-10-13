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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HttpTransportOriginUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertNormalizeOriginArguments")
    void assertNormalizeOrigin(final String name, final String origin, final String expected) {
        assertThat(HttpTransportOriginUtils.normalizeOrigin(origin), is(expected));
    }
    
    private static Stream<Arguments> assertNormalizeOriginArguments() {
        return Stream.of(
                Arguments.of("HTTPS host normalization", "HTTPS://Gateway.Example.Test:8443", "https://gateway.example.test:8443"),
                Arguments.of("IPv6 origin", "http://[::1]:8080", "http://[::1]:8080"),
                Arguments.of("malformed origin", "://bad-origin", ""),
                Arguments.of("unsupported scheme", "ftp://foo.example.test", ""));
    }
}
