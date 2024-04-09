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

package org.apache.shardingsphere.infra.url.absolutepath;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AbsolutePathURLLoaderTest {
    
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void assertGetContentOnLinux() {
        assertGetContent(1783);
    }
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void assertGetContentOnWindows() {
        assertGetContent(1839);
    }
    
    private void assertGetContent(final int expectedLength) {
        String actual = new AbsolutePathURLLoader().load(
                Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config/absolutepath/fixture.yaml")).getPath(), new Properties());
        assertThat(actual.length(), is(expectedLength));
    }
}
