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

package org.apache.shardingsphere.infra.auth.yaml.config;

import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlEngineShardingSphereUserConfigurationTest {
    
    @Test
    public void assertUnmarshal() {
        YamlShardingSphereUserConfiguration actual = YamlEngine.unmarshal("password: pwd\nauthorizedSchemas: db1", YamlShardingSphereUserConfiguration.class);
        assertThat(actual.getPassword(), is("pwd"));
        assertThat(actual.getAuthorizedSchemas(), is("db1"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertUnmarshalMap() {
        Map<String, Object> actual = (Map<String, Object>) YamlEngine.unmarshal("password: pwd\nauthorizedSchemas: db1", Collections.emptyList());
        assertThat(actual.get("password").toString(), is("pwd"));
        assertThat(actual.get("authorizedSchemas").toString(), is("db1"));
    }
    
    @Test
    public void assertUnmarshalProperties() {
        Properties actual = YamlEngine.unmarshalProperties("password: pwd\nauthorizedSchemas: db1", Collections.singletonList(Properties.class));
        assertThat(actual.getProperty("authorizedSchemas"), is("db1"));
        assertThat(actual.getProperty("password"), is("pwd"));
    }
    
    @Test
    public void assertMarshal() {
        YamlShardingSphereUserConfiguration actual = new YamlShardingSphereUserConfiguration();
        actual.setPassword("pwd");
        actual.setAuthorizedSchemas("db1");
        assertThat(YamlEngine.marshal(actual), is("authorizedSchemas: db1\npassword: pwd\n"));
    }
}
