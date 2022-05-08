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

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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
    public void assertGetPortWithSingleArgument() {
        Optional<Integer> actual = new BootstrapArguments(new String[]{"3306"}).getPort();
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
}
