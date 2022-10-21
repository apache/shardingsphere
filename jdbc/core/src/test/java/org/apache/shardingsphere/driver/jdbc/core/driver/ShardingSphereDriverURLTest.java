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

import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShardingSphereDriverURLTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewConstructorWithEmptyURL() {
        new ShardingSphereDriverURL("jdbc:shardingsphere:");
    }
    
    @Test
    public void assertToClasspathConfigurationFile() {
        ShardingSphereDriverURL actual = new ShardingSphereDriverURL("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
        assertThat(actual.toConfigurationBytes().length, is(822));
    }
    
    @Test
    public void assertToConfigurationFile() {
        String absolutePath = Objects.requireNonNull(ShardingSphereDriverURLTest.class.getClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        ShardingSphereDriverURL actual = new ShardingSphereDriverURL("jdbc:shardingsphere:" + absolutePath);
        assertThat(actual.toConfigurationBytes().length, is(822));
    }
    
    @Test
    public void assertToConfigurationFileWithOtherParameters() {
        String absolutePath = Objects.requireNonNull(ShardingSphereDriverURLTest.class.getClassLoader().getResource("config/driver/foo-driver-fixture.yaml")).getPath();
        ShardingSphereDriverURL actual = new ShardingSphereDriverURL("jdbc:shardingsphere:" + absolutePath + "?xxx=xxx&yyy=yyy");
        assertThat(actual.toConfigurationBytes().length, is(822));
    }
}
