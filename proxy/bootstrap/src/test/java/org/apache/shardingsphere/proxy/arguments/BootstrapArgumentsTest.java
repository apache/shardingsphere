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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class BootstrapArgumentsTest {
    
    @Test
    public void assertGetPortWithEmptyArgument() {
        assertFalse(new BootstrapArguments(new String[]{}).getPort().isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetPortWithWrongArgument() {
        new BootstrapArguments(new String[]{"WrongArgument"}).getPort();
    }
    
    @Test
    public void assertGetPortWithDefaultArgument() {
        assertFalse(new BootstrapArguments(new String[]{"-1"}).getPort().isPresent());
    }
    
    @Test
    public void assertGetPortWithSingleArgument() {
        Optional<Integer> actual = new BootstrapArguments(new String[]{"3306"}).getPort();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(3306));
    }
    
    @Test
    public void assertGetPortWithTwoArgument() {
        Optional<Integer> actual = new BootstrapArguments(new String[]{"3306", "/test_conf/"}).getPort();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(3306));
    }
    
    @Test
    public void assertGetPortWithThreeArgument() {
        Optional<Integer> actual = new BootstrapArguments(new String[]{"3306", "/test_conf/", "127.0.0.1"}).getPort();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(3306));
    }
    
    @Test
    public void assertGetConfigurationPathWithEmptyArgument() {
        assertThat(new BootstrapArguments(new String[]{}).getConfigurationPath(), is("/conf/"));
    }
    
    @Test
    public void assertGetConfigurationPathWithSingleArgument() {
        assertThat(new BootstrapArguments(new String[]{"3306"}).getConfigurationPath(), is("/conf/"));
    }
    
    @Test
    public void assertGetConfigurationPathWithTwoArguments() {
        assertThat(new BootstrapArguments(new String[]{"3306", "test_conf"}).getConfigurationPath(), is("/test_conf/"));
        assertThat(new BootstrapArguments(new String[]{"3306", "/test_conf"}).getConfigurationPath(), is("/test_conf/"));
        assertThat(new BootstrapArguments(new String[]{"3306", "test_conf/"}).getConfigurationPath(), is("/test_conf/"));
        assertThat(new BootstrapArguments(new String[]{"3306", "/test_conf/"}).getConfigurationPath(), is("/test_conf/"));
    }
    
    @Test
    public void assertGetAddressesWithEmptyArgument() {
        assertThat(new BootstrapArguments(new String[]{}).getAddresses(), is(Collections.singletonList("0.0.0.0")));
    }
    
    @Test
    public void assertGetAddressesWithSingleArgument() {
        assertThat(new BootstrapArguments(new String[]{"3306"}).getAddresses(), is(Collections.singletonList("0.0.0.0")));
    }
    
    @Test
    public void assertGetAddressesWithTwoArgument() {
        assertThat(new BootstrapArguments(new String[]{"3306", "test_conf"}).getAddresses(), is(Collections.singletonList("0.0.0.0")));
    }
    
    @Test
    public void assertGetAddressesWithThreeArguments() {
        assertThat(new BootstrapArguments(new String[]{"3306", "test_conf", "127.0.0.1"}).getAddresses(), is(Collections.singletonList("127.0.0.1")));
        assertThat(new BootstrapArguments(new String[]{"3306", "test_conf", "1.1.1.1,127.0.0.1"}).getAddresses(), is(Arrays.asList("1.1.1.1", "127.0.0.1")));
    }
    
    @Test
    public void assertGetForce() {
        assertFalse(new BootstrapArguments(new String[]{"3306", "test_conf", "127.0.0.1"}).getForce());
        assertFalse(new BootstrapArguments(new String[]{"3306", "test_conf", "127.0.0.1", "false"}).getForce());
        assertTrue(new BootstrapArguments(new String[]{"3306", "test_conf", "127.0.0.1", "true "}).getForce());
        assertTrue(new BootstrapArguments(new String[]{"3306", "test_conf", "127.0.0.1", "true"}).getForce());
        assertTrue(new BootstrapArguments(new String[]{"3306", "test_conf", "127.0.0.1", "TrUe"}).getForce());
    }
}
