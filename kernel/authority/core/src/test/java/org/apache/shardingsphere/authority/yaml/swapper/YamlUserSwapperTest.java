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

package org.apache.shardingsphere.authority.yaml.swapper;

import org.apache.shardingsphere.authority.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlUserSwapperTest {
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlUserConfiguration actual = new YamlUserSwapper().swapToYamlConfiguration(new ShardingSphereUser("foo_user", "foo_pwd", "127.0.0.1"));
        assertNotNull(actual);
        assertThat(actual.getUser(), is("foo_user@127.0.0.1"));
        assertThat(actual.getPassword(), is("foo_pwd"));
    }
    
    @Test
    void assertSwapToNullYamlConfiguration() {
        assertNull(new YamlUserSwapper().swapToYamlConfiguration(null));
    }
    
    @Test
    void assertSwapToObject() {
        YamlUserConfiguration user = new YamlUserConfiguration();
        user.setUser("foo_user@127.0.0.1");
        user.setPassword("foo_pwd");
        ShardingSphereUser actual = new YamlUserSwapper().swapToObject(user);
        assertNotNull(actual);
        assertThat(actual.getGrantee().getUsername(), is("foo_user"));
        assertThat(actual.getGrantee().getHostname(), is("127.0.0.1"));
        assertThat(actual.getPassword(), is("foo_pwd"));
    }
    
    @Test
    void assertSwapToObjectWithUserEndWithAt() {
        YamlUserConfiguration user = new YamlUserConfiguration();
        user.setUser("foo_user@");
        user.setPassword("foo_pwd");
        ShardingSphereUser actual = new YamlUserSwapper().swapToObject(user);
        assertNotNull(actual);
        assertThat(actual.getGrantee().getUsername(), is("foo_user"));
        assertThat(actual.getGrantee().getHostname(), is("%"));
        assertThat(actual.getPassword(), is("foo_pwd"));
    }
    
    @Test
    void assertSwapToObjectWithEmptyUsername() {
        YamlUserConfiguration user = new YamlUserConfiguration();
        user.setUser("@127.0.0.1");
        assertThrows(IllegalArgumentException.class, () -> new YamlUserSwapper().swapToObject(user));
    }
    
    @Test
    void assertSwapToNullObject() {
        assertNull(new YamlUserSwapper().swapToObject(null));
    }
}
