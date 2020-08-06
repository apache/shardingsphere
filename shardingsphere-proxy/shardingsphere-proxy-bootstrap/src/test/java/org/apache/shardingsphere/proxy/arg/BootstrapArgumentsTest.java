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

package org.apache.shardingsphere.proxy.arg;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BootstrapArgumentsTest {
    
    @Test
    public void assertEmptyArgs() {
        BootstrapArguments actual = new BootstrapArguments(new String[]{});
        assertThat(actual.getPort(), is(3307));
        assertThat(actual.getConfigurationPath(), is("/conf/"));
    }
    
    @Test
    public void assertWrongParam() {
        String param = "WrongParam";
        try {
            new BootstrapArguments(new String[]{param});
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(String.format("Invalid port `%s`.", param)));
        }
    }
    
    @Test
    public void assertOneArgs() {
        BootstrapArguments actual = new BootstrapArguments(new String[]{"3306"});
        assertThat(actual.getPort(), is(3306));
        assertThat(actual.getConfigurationPath(), is("/conf/"));
    }
    
    @Test
    public void assertTwoArgs() {
        BootstrapArguments actual = new BootstrapArguments(new String[]{"3305", "test"});
        assertThat(actual.getPort(), is(3305));
        assertThat(actual.getConfigurationPath(), is("/test/"));
        actual = new BootstrapArguments(new String[]{"3304", "/test1"});
        assertThat(actual.getPort(), is(3304));
        assertThat(actual.getConfigurationPath(), is("/test1/"));
        actual = new BootstrapArguments(new String[]{"3303", "test2/"});
        assertThat(actual.getPort(), is(3303));
        assertThat(actual.getConfigurationPath(), is("/test2/"));
        actual = new BootstrapArguments(new String[]{"3302", "/test3/"});
        assertThat(actual.getPort(), is(3302));
        assertThat(actual.getConfigurationPath(), is("/test3/"));
    }
}
