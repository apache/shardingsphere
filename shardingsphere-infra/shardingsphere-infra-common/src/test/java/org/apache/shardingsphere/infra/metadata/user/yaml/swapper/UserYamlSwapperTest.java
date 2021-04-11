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

import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUserConfiguration;
import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class UserYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        ShardingSphereUser shardingSphereUser = new ShardingSphereUser("user1", "pwd1", "127.0.0.1");
        YamlUserConfiguration yamlUser = new UserYamlSwapper().swapToYamlConfiguration(shardingSphereUser);
        assertThat(yamlUser.getPassword(), is("pwd1"));
        assertThat(yamlUser.getHostname(), is("127.0.0.1"));
        assertThat(yamlUser.getUsername(), is("user1"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlUserConfiguration user1 = new YamlUserConfiguration();
        user1.setUsername("user1");
        user1.setPassword("pwd1");
        user1.setHostname("127.0.0.1");
        ShardingSphereUser actualUser = new UserYamlSwapper().swapToObject(user1);
        assertThat(actualUser.getPassword(), is("pwd1"));
        assertThat(actualUser.getGrantee().getHostname(), is("127.0.0.1"));
        assertThat(actualUser.getGrantee().getUsername(), is("user1"));
    }
    
    @Test
    public void assertSwapToObjectForNull() {
        ShardingSphereUser actual = new UserYamlSwapper().swapToObject(null);
        assertTrue(Objects.isNull(actual));
        YamlUserConfiguration actualYamlUser = new UserYamlSwapper().swapToYamlConfiguration(null);
        assertTrue(Objects.isNull(actualYamlUser));
    }
}
