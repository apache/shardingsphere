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

package org.apache.shardingsphere.database.protocol.firebird.constant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdArchTypeTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("valueOfCases")
    void assertValueOf(final String name, final int code, final FirebirdArchType expected) {
        assertThat(FirebirdArchType.valueOf(code), is(expected));
    }
    
    @Test
    void assertValueOfWithInvalidCode() {
        assertThrows(NullPointerException.class, () -> FirebirdArchType.valueOf(999));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isValidCases")
    void assertIsValid(final String name, final FirebirdArchType arch, final boolean expectedValid) {
        assertThat(FirebirdArchType.isValid(arch), is(expectedValid));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("architectureCases")
    void assertArchitecture(final String name, final String osName, final String osArch, final String expectedArchitecture) throws ReflectiveOperationException, IOException {
        assertThat(resolveArchitectureByEnvironment(osName, osArch), is(expectedArchitecture));
    }
    
    private String resolveArchitectureByEnvironment(final String osName, final String osArch) throws ReflectiveOperationException, IOException {
        String originalOsName = System.getProperty("os.name");
        String originalOsArch = System.getProperty("os.arch");
        try {
            setOrClearProperty("os.name", osName);
            setOrClearProperty("os.arch", osArch);
            try (URLClassLoader classLoader = new URLClassLoader(classPathUrls(), null)) {
                Class<?> archTypeClass = Class.forName(FirebirdArchType.class.getName(), true, classLoader);
                Object architecture = archTypeClass.getField("ARCHITECTURE").get(null);
                return ((Enum<?>) architecture).name();
            }
        } finally {
            setOrClearProperty("os.name", originalOsName);
            setOrClearProperty("os.arch", originalOsArch);
        }
    }
    
    private void setOrClearProperty(final String key, final String value) {
        if (null == value) {
            System.clearProperty(key);
            return;
        }
        System.setProperty(key, value);
    }
    
    private URL[] classPathUrls() throws MalformedURLException {
        String[] classPathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
        URL[] result = new URL[classPathEntries.length];
        for (int i = 0; i < classPathEntries.length; i++) {
            result[i] = Paths.get(classPathEntries[i]).toUri().toURL();
        }
        return result;
    }
    
    private static Stream<Arguments> valueOfCases() {
        return Stream.of(
                Arguments.of("resolve ARCH_GENERIC by code", 1, FirebirdArchType.ARCH_GENERIC),
                Arguments.of("resolve ARCH_INTEL_32 by code", 29, FirebirdArchType.ARCH_INTEL_32),
                Arguments.of("resolve duplicate linux code to ARCH_LINUX", 36, FirebirdArchType.ARCH_LINUX));
    }
    
    private static Stream<Arguments> isValidCases() {
        return Stream.of(
                Arguments.of("ARCH_GENERIC is valid", FirebirdArchType.ARCH_GENERIC, true),
                Arguments.of("ARCHITECTURE is valid", FirebirdArchType.ARCHITECTURE, true),
                Arguments.of("ARCH_MAX is not valid", FirebirdArchType.ARCH_MAX, false));
    }
    
    private static Stream<Arguments> architectureCases() {
        return Stream.of(
                Arguments.of("sun sparc maps to ARCH_SUN4", "SunOS", "sparc", "ARCH_SUN4"),
                Arguments.of("sun amd64 maps to ARCH_SUNX86", "SunOS", "amd64", "ARCH_SUNX86"),
                Arguments.of("sun unknown arch maps to ARCH_SUN", "SunOS", "mips", "ARCH_SUN"),
                Arguments.of("hpux maps to ARCH_HPUX", "HP-UX", "ia64", "ARCH_HPUX"),
                Arguments.of("aix maps to ARCH_RT", "AIX", "ppc", "ARCH_RT"),
                Arguments.of("linux loong maps to ARCH_LINUX_LOONG", "Linux", "loongarch64", "ARCH_LINUX_LOONG"),
                Arguments.of("linux non loong maps to ARCH_LINUX", "Linux", "x86_64", "ARCH_LINUX"),
                Arguments.of("freebsd maps to ARCH_FREEBSD", "FreeBSD", "amd64", "ARCH_FREEBSD"),
                Arguments.of("netbsd maps to ARCH_NETBSD", "NetBSD", "amd64", "ARCH_NETBSD"),
                Arguments.of("mac powerpc maps to ARCH_DARWIN_PPC", "Mac OS X", "powerpc", "ARCH_DARWIN_PPC"),
                Arguments.of("darwin ppc64 maps to ARCH_DARWIN_PPC64", "Darwin", "ppc64", "ARCH_DARWIN_PPC64"),
                Arguments.of("darwin arm maps to ARCH_DARWIN_X64_ARM", "Darwin", "aarch64", "ARCH_DARWIN_X64_ARM"),
                Arguments.of("darwin x64 maps to ARCH_DARWIN_X64", "Darwin", "x86_64", "ARCH_DARWIN_X64"),
                Arguments.of("windows amd64 maps to ARCH_WINNT_64", "Windows 10", "amd64", "ARCH_WINNT_64"),
                Arguments.of("windows non amd64 i386 maps to ARCH_INTEL_32", "Windows 10", "i386", "ARCH_INTEL_32"),
                Arguments.of("i386 maps to ARCH_INTEL_32", "Unknown", "i386", "ARCH_INTEL_32"),
                Arguments.of("arm maps to ARCH_ARM", "Unknown", "arm", "ARCH_ARM"),
                Arguments.of("other os and arch maps to ARCH_GENERIC", "Unknown", "x86_64", "ARCH_GENERIC"));
    }
}
