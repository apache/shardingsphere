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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import org.apache.shardingsphere.driver.jdbc.exception.syntax.URLProviderNotFoundException;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(AutoMockExtension.class)
class ShardingSphereURLManagerTest {
    
    private final int fooDriverConfigLengthOnUnix = 999;
    
    private final int fooDriverConfigLengthOnWindows = 1040;
    
    private final String urlPrefix = "jdbc:shardingsphere:";
    
    @Test
    void assertNewConstructorWithEmptyURL() {
        assertThrows(URLProviderNotFoundException.class, () -> ShardingSphereURLManager.getContent("jdbc:shardingsphere:", urlPrefix));
    }
    
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void assertToClasspathConfigurationFile() {
        byte[] actual = ShardingSphereURLManager.getContent("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml", urlPrefix);
        assertThat(actual.length, is(fooDriverConfigLengthOnUnix));
    }
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void assertToClasspathConfigurationFileOnWindows() {
        byte[] actual = ShardingSphereURLManager.getContent("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml", urlPrefix);
        assertThat(actual.length, is(fooDriverConfigLengthOnWindows));
    }
    
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void assertToAbsolutePathConfigurationFile() {
        String absolutePath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        byte[] actual = ShardingSphereURLManager.getContent("jdbc:shardingsphere:absolutepath:" + absolutePath, urlPrefix);
        assertThat(actual.length, is(fooDriverConfigLengthOnUnix));
    }
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void assertToAbsolutePathConfigurationFileOnWindows() {
        String absolutePath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        byte[] actual = ShardingSphereURLManager.getContent("jdbc:shardingsphere:absolutepath:" + absolutePath, urlPrefix);
        assertThat(actual.length, is(fooDriverConfigLengthOnWindows));
    }
}
