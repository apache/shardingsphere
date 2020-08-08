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

package org.apache.shardingsphere.infra.auth.yaml.swapper;

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlProxyUserConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AuthenticationYamlSwapperTest {
        
    @Test
    public void assertSwapToYaml() {
        ProxyUser user1 = new ProxyUser("pwd1", Collections.singleton("db1"));
        ProxyUser user2 = new ProxyUser("pwd2", Collections.singleton("db2"));
        Authentication authentication = new Authentication();
        authentication.getUsers().put("user1", user1);
        authentication.getUsers().put("user2", user2);
        YamlAuthenticationConfiguration actual = new AuthenticationYamlSwapper().swapToYamlConfiguration(authentication);
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("user2").getPassword(), is("pwd2"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlProxyUserConfiguration user1 = new YamlProxyUserConfiguration();
        user1.setPassword("pwd1");
        user1.setAuthorizedSchemas("db1");
        YamlProxyUserConfiguration user2 = new YamlProxyUserConfiguration();
        user2.setPassword("pwd2");
        user2.setAuthorizedSchemas("db2,db1");
        YamlAuthenticationConfiguration configuration = new YamlAuthenticationConfiguration();
        configuration.getUsers().put("user1", user1);
        configuration.getUsers().put("user2", user2);
        Authentication actual = new AuthenticationYamlSwapper().swapToObject(configuration);
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("user2").getAuthorizedSchemas().size(), is(2));
    }
    
    @Test
    public void assertSwapToObjectForNull() {
        Authentication actual = new AuthenticationYamlSwapper().swapToObject(null);
        assertTrue(actual.getUsers().isEmpty());
    }
}
