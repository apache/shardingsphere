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

package org.apache.shardingsphere.authority.rule;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AuthorityRuleTest {
    
    @Test
    public void assertGetRuleType() {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.emptyList(), new ShardingSphereAlgorithmConfiguration("ALL_PRIVILEGES_PERMITTED", new Properties()));
        AuthorityRule authorityRule = new AuthorityRule(ruleConfig, Collections.emptyMap(), users);
        assertThat(authorityRule.getType(), is(AuthorityRule.class.getSimpleName()));
    }
}
