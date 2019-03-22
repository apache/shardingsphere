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

package org.apache.shardingsphere.core.yaml.swapper.impl;

import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthentication;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AuthenticationYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        YamlAuthentication actual = new AuthenticationYamlSwapper().swap(new Authentication("root", "pwd"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("pwd"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlAuthentication yamlAuthentication = new YamlAuthentication();
        yamlAuthentication.setUsername("root");
        yamlAuthentication.setPassword("pwd");
        Authentication actual = new AuthenticationYamlSwapper().swap(yamlAuthentication);
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("pwd"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSwapToObjectWithoutUsername() {
        new AuthenticationYamlSwapper().swap(new YamlAuthentication());
    }
}
