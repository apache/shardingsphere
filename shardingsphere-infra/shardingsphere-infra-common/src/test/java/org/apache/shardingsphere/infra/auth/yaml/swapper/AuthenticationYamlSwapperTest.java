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

import org.apache.shardingsphere.infra.auth.memory.MemoryAuthentication;
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.memory.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.memory.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.auth.memory.yaml.swapper.AuthenticationYamlSwapper;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AuthenticationYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        MemoryAuthentication authentication = new MemoryAuthentication();
        authentication.getUsers().put("user1", new ShardingSphereUser("pwd1", Collections.singleton("db1")));
        authentication.getUsers().put("user2", new ShardingSphereUser("pwd2", Collections.singleton("db2")));
        YamlAuthenticationConfiguration actual = new AuthenticationYamlSwapper().swapToYamlConfiguration(authentication);
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("user1").getPassword(), is("pwd1"));
        assertThat(actual.getUsers().get("user1").getAuthorizedSchemas(), is("db1"));
        assertThat(actual.getUsers().get("user2").getPassword(), is("pwd2"));
        assertThat(actual.getUsers().get("user2").getAuthorizedSchemas(), is("db2"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlUserConfiguration user1 = new YamlUserConfiguration();
        user1.setPassword("pwd1");
        user1.setAuthorizedSchemas("db1");
        YamlUserConfiguration user2 = new YamlUserConfiguration();
        user2.setPassword("pwd2");
        user2.setAuthorizedSchemas("db2,db1");
        Map<String, YamlUserConfiguration> users = new HashMap<>(2, 1);
        users.put("user1", user1);
        users.put("user2", user2);
        YamlAuthenticationConfiguration yamlConfig = new YamlAuthenticationConfiguration();
        yamlConfig.setUsers(users);
        MemoryAuthentication actual = new AuthenticationYamlSwapper().swapToObject(yamlConfig);
        Optional<ShardingSphereUser> actualUser1 = actual.findUser("user1");
        assertTrue(actualUser1.isPresent());
        assertThat(actualUser1.get().getAuthorizedSchemas().size(), is(1));
        Optional<ShardingSphereUser> actualUser2 = actual.findUser("user2");
        assertTrue(actualUser2.isPresent());
        assertThat(actualUser2.get().getAuthorizedSchemas().size(), is(2));
    }
    
    @Test
    public void assertSwapToObjectForNull() {
        MemoryAuthentication actual = new AuthenticationYamlSwapper().swapToObject(null);
        assertTrue(actual.getUsers().isEmpty());
    }
}
