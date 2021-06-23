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

package org.apache.shardingsphere.infra.metadata.user.yaml.swapper;

import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlEngineUserConfigurationTest {
    
    @Test
    public void assertUnmarshal() {
        YamlUserConfiguration actual = YamlEngine.unmarshal("password: pwd", YamlUserConfiguration.class);
        assertThat(actual.getPassword(), is("pwd"));
    }
    
    @Test
    public void assertSecureUnmarshalProperties() {
        Properties actual = YamlEngine.unmarshal("password: pwd", Properties.class);
        assertThat(actual.getProperty("password"), is("pwd"));
    }
    
    @Test
    public void assertMarshal() {
        YamlUserConfiguration actual = new YamlUserConfiguration();
        actual.setPassword("pwd");
        assertThat(YamlEngine.marshal(actual), is("password: pwd\n"));
    }
}
