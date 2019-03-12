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

package org.apache.shardingsphere.core.yaml.engine;

import org.apache.shardingsphere.core.yaml.config.common.YamlAuthentication;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlEngineTest {
    
    @Test
    public void assertUnmarshal() {
        YamlAuthentication actual = YamlEngine.unmarshal("username: root\npassword: pwd", YamlAuthentication.class);
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("pwd"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertUnmarshalMap() {
        Map<String, Object> actual = (Map<String, Object>) YamlEngine.unmarshal("username: root\npassword: pwd");
        assertThat(actual.get("username").toString(), is("root"));
        assertThat(actual.get("password").toString(), is("pwd"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertUnmarshalProperties() {
        Properties actual = YamlEngine.unmarshalProperties("username: root\npassword: pwd");
        assertThat(actual.getProperty("username"), is("root"));
        assertThat(actual.getProperty("password"), is("pwd"));
    }
    
    @Test
    public void assertMarshal() {
        YamlAuthentication actual = new YamlAuthentication();
        actual.setUsername("root");
        actual.setPassword("pwd");
        assertThat(YamlEngine.marshal(actual), is("password: pwd\nusername: root\n"));
    }
}
