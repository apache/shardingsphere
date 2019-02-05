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

package org.apache.shardingsphere.api.config.sharding;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class KeyGeneratorConfigurationTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithoutType() {
        new KeyGeneratorConfiguration("", "id", new Properties());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithoutColumn() {
        new KeyGeneratorConfiguration("TEST", "", new Properties());
    }
    
    @Test
    public void assertConstructorWithoutProperties() {
        KeyGeneratorConfiguration actual = new KeyGeneratorConfiguration("TEST", "id", null);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getColumn(), is("id"));
        assertThat(actual.getProperties(), is(new Properties()));
    }
    
    @Test
    public void assertConstructorWithFullArguments() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        KeyGeneratorConfiguration actual = new KeyGeneratorConfiguration("TEST", "id", props);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getColumn(), is("id"));
        assertThat(actual.getProperties(), is(props));
    }
}
