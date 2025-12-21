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

import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BootstrapArgumentsTest {
    
    @Test
    void assertGetPortWithoutArguments() {
        assertFalse(new BootstrapArguments(new String[]{}).getPort().isPresent());
    }
    
    @Test
    void assertGetPortWithNegativeArgument() {
        assertFalse(new BootstrapArguments(new String[]{"-1"}).getPort().isPresent());
    }
    
    @Test
    void assertGetPortWithNonNumericArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new BootstrapArguments(new String[]{"WrongArgument"}).getPort());
        assertThat(ex.getMessage(), is("Invalid port `WrongArgument`."));
    }
    
    @Test
    void assertGetPortWithValidArgument() {
        Optional<Integer> actualPort = new BootstrapArguments(new String[]{"3306"}).getPort();
        assertTrue(actualPort.isPresent());
        assertThat(actualPort.get(), is(3306));
    }
    
    @Test
    void assertGetConfigurationPathWithSingleArgumentUsesDefaultPath() throws ReflectiveOperationException {
        assertThat(new BootstrapArguments(new String[]{"3306"}).getConfigurationPath(), is(Plugins.getMemberAccessor().get(BootstrapArguments.class.getDeclaredField("DEFAULT_CONFIG_PATH"), null)));
    }
    
    @Test
    void assertGetConfigurationPathPadsLeadingSlash() {
        assertThat(new BootstrapArguments(new String[]{"0", "test/"}).getConfigurationPath(), is("/test/"));
    }
    
    @Test
    void assertGetConfigurationPathPadsTrailingSlash() {
        assertThat(new BootstrapArguments(new String[]{"0", "/test"}).getConfigurationPath(), is("/test/"));
    }
    
    @Test
    void assertGetAddressesDefaultWhenArgumentsLessThanThree() {
        assertThat(new BootstrapArguments(new String[]{"0", "conf"}).getAddresses(), is(Collections.singletonList("0.0.0.0")));
    }
    
    @Test
    void assertGetSocketPathWhenArgumentsLessThanThree() {
        assertFalse(new BootstrapArguments(new String[]{"0", "conf"}).getSocketPath().isPresent());
    }
    
    @Test
    void assertGetAddressesFiltersNonInetAddress() {
        assertThat(new BootstrapArguments(new String[]{"0", "conf", "127.0.0.1,/tmp/shardingsphere.sock"}).getAddresses(), is(Collections.singletonList("127.0.0.1")));
    }
    
    @Test
    void assertGetSocketPathReturnsFirstNonInetAddress() {
        Optional<String> actualSocketPath = new BootstrapArguments(new String[]{"0", "conf", "127.0.0.1,/tmp/shardingsphere.sock"}).getSocketPath();
        assertTrue(actualSocketPath.isPresent());
        assertThat(actualSocketPath.get(), is("/tmp/shardingsphere.sock"));
    }
    
    @Test
    void assertGetSocketPathWhenOnlyInetAddresses() {
        assertFalse(new BootstrapArguments(new String[]{"0", "conf", "127.0.0.1,1.1.1.1"}).getSocketPath().isPresent());
    }
    
    @Test
    void assertGetSocketPathWithInvalidPath() {
        BootstrapArguments arguments = new BootstrapArguments(new String[]{"0", "conf", "127.0.0.1,\0"});
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, arguments::getSocketPath);
        assertThat(ex.getMessage(), is("Invalid path `\0`."));
    }
}
