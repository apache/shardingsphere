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

package org.apache.shardingsphere.proxy.arguments;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapArgumentsTest {
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideBootstrapArguments")
    void assertBootstrapArguments(final String scenario, final String[] args, final Optional<Integer> expectedPort, final Optional<String> expectedPortErrorMessage,
                                  final String expectedConfigPath, final List<String> expectedAddresses, final Optional<String> expectedSocketPath,
                                  final Optional<String> expectedSocketPathErrorMessage) {
        BootstrapArguments arguments = new BootstrapArguments(args);
        if (expectedPortErrorMessage.isPresent()) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, arguments::getPort);
            assertThat(scenario + ": port error message", ex.getMessage(), is(expectedPortErrorMessage.get()));
        } else {
            Optional<Integer> actualPort = arguments.getPort();
            if (expectedPort.isPresent()) {
                assertTrue(actualPort.isPresent());
                assertThat(scenario + ": port value", actualPort.get(), is(expectedPort.get()));
            } else {
                assertFalse(actualPort.isPresent());
            }
        }
        assertThat(scenario + ": configuration path", arguments.getConfigurationPath(), is(expectedConfigPath));
        assertThat(scenario + ": addresses", arguments.getAddresses(), is(expectedAddresses));
        if (expectedSocketPathErrorMessage.isPresent()) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, arguments::getSocketPath);
            assertThat(scenario + ": socket path error message", ex.getMessage(), is(expectedSocketPathErrorMessage.get()));
        } else {
            Optional<String> actualSocketPath = arguments.getSocketPath();
            if (expectedSocketPath.isPresent()) {
                assertTrue(actualSocketPath.isPresent());
                assertThat(scenario + ": socket path", actualSocketPath.get(), is(expectedSocketPath.get()));
            } else {
                assertFalse(actualSocketPath.isPresent());
            }
        }
    }
    
    private static Stream<Arguments> provideBootstrapArguments() {
        String defaultConfigPath = getDefaultConfigPath();
        return Stream.of(
                Arguments.of("without arguments", new String[]{}, Optional.empty(), Optional.empty(), defaultConfigPath,
                        Collections.singletonList("0.0.0.0"), Optional.empty(), Optional.empty()),
                Arguments.of("negative port", new String[]{"-1"}, Optional.empty(), Optional.empty(), defaultConfigPath,
                        Collections.singletonList("0.0.0.0"), Optional.empty(), Optional.empty()),
                Arguments.of("non numeric port", new String[]{"WrongArgument"}, Optional.empty(), Optional.of("Invalid port `WrongArgument`."),
                        defaultConfigPath, Collections.singletonList("0.0.0.0"), Optional.empty(), Optional.empty()),
                Arguments.of("valid port uses default configuration path", new String[]{"3306"}, Optional.of(3306), Optional.empty(),
                        defaultConfigPath, Collections.singletonList("0.0.0.0"), Optional.empty(), Optional.empty()),
                Arguments.of("pad leading slash", new String[]{"0", "test/"}, Optional.of(0), Optional.empty(), "/test/",
                        Collections.singletonList("0.0.0.0"), Optional.empty(), Optional.empty()),
                Arguments.of("pad trailing slash", new String[]{"0", "/test"}, Optional.of(0), Optional.empty(), "/test/",
                        Collections.singletonList("0.0.0.0"), Optional.empty(), Optional.empty()),
                Arguments.of("default addresses when arguments less than three", new String[]{"0", "conf"}, Optional.of(0), Optional.empty(),
                        "/conf/", Collections.singletonList("0.0.0.0"), Optional.empty(), Optional.empty()),
                Arguments.of("filter non inet address", new String[]{"0", "conf", "127.0.0.1,/tmp/shardingsphere.sock"}, Optional.of(0), Optional.empty(),
                        "/conf/", Collections.singletonList("127.0.0.1"), Optional.of("/tmp/shardingsphere.sock"), Optional.empty()),
                Arguments.of("socket path empty when only inet addresses", new String[]{"0", "conf", "127.0.0.1,1.1.1.1"}, Optional.of(0), Optional.empty(),
                        "/conf/", Arrays.asList("127.0.0.1", "1.1.1.1"), Optional.empty(), Optional.empty()),
                Arguments.of("invalid socket path", new String[]{"0", "conf", "127.0.0.1,\0"}, Optional.of(0), Optional.empty(),
                        "/conf/", Collections.singletonList("127.0.0.1"), Optional.empty(), Optional.of("Invalid path `\0`.")));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static String getDefaultConfigPath() {
        return (String) Plugins.getMemberAccessor().get(BootstrapArguments.class.getDeclaredField("DEFAULT_CONFIG_PATH"), null);
    }
}
