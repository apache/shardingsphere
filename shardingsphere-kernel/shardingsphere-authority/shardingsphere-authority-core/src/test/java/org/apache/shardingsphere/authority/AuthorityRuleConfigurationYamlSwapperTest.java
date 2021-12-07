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

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityRuleConfigurationYamlSwapperTest {

    private AuthorityRuleConfigurationYamlSwapper swapper =  new AuthorityRuleConfigurationYamlSwapper();

    /**
     * test swapToYamlConfiguration with authority rule configuration.
     */
    @Test
    public void swapToYamlConfigurationTest() {
        AuthorityRuleConfiguration authorityRuleConfiguration = mock(AuthorityRuleConfiguration.class);
        ShardingSphereAlgorithmConfiguration configuration = mock(ShardingSphereAlgorithmConfiguration.class);


        when(authorityRuleConfiguration.getProvider()).thenReturn(configuration);
        when(configuration.getType()).thenReturn("type");
        when(configuration.getProps()).thenReturn(new Properties());

        final YamlAuthorityRuleConfiguration result = swapper.swapToYamlConfiguration(authorityRuleConfiguration);
        assertNotNull(result);
        assertEquals(0, result.getUsers().size());
        assertNotNull(result.getProvider());
    }

    /**
     * test swap to objects.
     */
    @Test
    public void swapToObjectTest() {
        YamlAuthorityRuleConfiguration authorityRuleConfiguration = mock(YamlAuthorityRuleConfiguration.class);
        YamlShardingSphereAlgorithmConfiguration configuration = mock(YamlShardingSphereAlgorithmConfiguration.class);

        when(authorityRuleConfiguration.getUsers()).thenReturn(Collections.singletonList("root@localhost:pass"));
        when(authorityRuleConfiguration.getProvider()).thenReturn(configuration);
        when(authorityRuleConfiguration.getProvider()).thenReturn(configuration);
        when(configuration.getType()).thenReturn("type");
        when(configuration.getProps()).thenReturn(new Properties());

        swapper.swapToObject(authorityRuleConfiguration);
    }

    /**
     * test get type.
     */
    @Test
    public void getTypeClass() {
        assertEquals(AuthorityRuleConfiguration.class, swapper.getTypeClass());
    }

    /**
     * test rule tag name.
     */
    @Test
    public void getRuleTagName() {
        assertEquals("AUTHORITY", swapper.getRuleTagName());
    }

    /**
     * test get order.
     */
    @Test
    public void getOrder() {
        assertEquals(500, swapper.getOrder());
    }
}