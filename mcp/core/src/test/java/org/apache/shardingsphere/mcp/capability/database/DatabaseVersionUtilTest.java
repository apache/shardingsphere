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

package org.apache.shardingsphere.mcp.capability.database;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DatabaseVersionUtilTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isVersionAtLeastArguments")
    void assertIsVersionAtLeast(final String name, final String databaseVersion, final int major, final int minor, final int patch, final boolean expected) {
        assertThat(DatabaseVersionUtil.isVersionAtLeast(databaseVersion, major, minor, patch), is(expected));
    }
    
    private static Stream<Arguments> isVersionAtLeastArguments() {
        return Stream.of(
                Arguments.of("null version", null, 1, 0, 0, false),
                Arguments.of("major is less than target", "4.0.0", 5, 0, 0, false),
                Arguments.of("major is greater than target", "6.0.0", 5, 0, 0, true),
                Arguments.of("minor is less than target", "5.1.0", 5, 2, 0, false),
                Arguments.of("minor is greater than target", "5.3.0", 5, 2, 0, true),
                Arguments.of("missing patch is less than target", "5.2", 5, 2, 1, false),
                Arguments.of("patch equals target with suffix", " 5.2.1-log ", 5, 2, 1, true),
                Arguments.of("missing minor and patch default to zero", "5", 5, 0, 0, true));
    }
}
