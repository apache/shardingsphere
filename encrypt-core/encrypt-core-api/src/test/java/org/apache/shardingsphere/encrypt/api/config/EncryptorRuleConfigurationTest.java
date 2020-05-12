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

package org.apache.shardingsphere.encrypt.api.config;

import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EncryptorRuleConfigurationTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertConstructorWithoutType() {
        new EncryptorRuleConfiguration(null, new Properties());
    }
    
    @Test
    public void assertConstructorWithoutAssistedQueryColumnsAndProperties() {
        EncryptorRuleConfiguration actual = new EncryptorRuleConfiguration("TEST", new Properties());
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProperties(), is(new Properties()));
    }
    
    @Test
    public void assertConstructorWithMinArguments() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        EncryptorRuleConfiguration actual = new EncryptorRuleConfiguration("TEST", props);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProperties(), is(props));
    }
    
    @Test
    public void assertConstructorWithMaxArguments() {
        Properties props = new Properties();
        props.setProperty("key", "value");
        EncryptorRuleConfiguration actual = new EncryptorRuleConfiguration("TEST", props);
        assertThat(actual.getType(), is("TEST"));
        assertThat(actual.getProperties(), is(props));
    }
}
