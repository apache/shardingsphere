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

import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlShardingSphereUserConfiguration;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingSphereUserYamlSwapperTest {
    
    @Test
    public void assertSwapToYaml() {
        YamlShardingSphereUserConfiguration actual = new ShardingSphereUserYamlSwapper().swapToYamlConfiguration(new ShardingSphereUser("pwd", Collections.singleton("db1")));
        assertThat(actual.getAuthorizedSchemas(), is("db1"));
        assertThat(actual.getPassword(), is("pwd"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShardingSphereUserConfiguration yamlUserConfig = new YamlShardingSphereUserConfiguration();
        yamlUserConfig.setAuthorizedSchemas("db1");
        yamlUserConfig.setPassword("pwd");
        ShardingSphereUser actual = new ShardingSphereUserYamlSwapper().swapToObject(yamlUserConfig);
        assertThat(actual.getAuthorizedSchemas().iterator().next(), is("db1"));
        assertThat(actual.getPassword(), is("pwd"));
    }
    
    @Test
    public void assertSwapToObjectWithoutAuthorizedSchemas() {
        YamlShardingSphereUserConfiguration yamlUserConfig = new YamlShardingSphereUserConfiguration();
        yamlUserConfig.setPassword("pwd");
        ShardingSphereUser actual = new ShardingSphereUserYamlSwapper().swapToObject(yamlUserConfig);
        assertThat(actual.getAuthorizedSchemas().size(), is(0));
    }
}
