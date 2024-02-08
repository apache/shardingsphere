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

package org.apache.shardingsphere.authority.fixture;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.mockito.Answers;

import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

public final class PrivilegeProviderFixture implements PrivilegeProvider {
    
    @Override
    public Map<Grantee, ShardingSpherePrivileges> build(final AuthorityRuleConfiguration ruleConfig) {
        ShardingSpherePrivileges privileges = mockPrivileges();
        return ruleConfig.getUsers().stream().collect(Collectors.toMap(ShardingSphereUser::getGrantee, each -> privileges));
    }
    
    private ShardingSpherePrivileges mockPrivileges() {
        return mock(ShardingSpherePrivileges.class,
                withSettings().defaultAnswer(invocation -> Boolean.TYPE == invocation.getMethod().getReturnType() ? true : Answers.RETURNS_DEFAULTS.answer(invocation)));
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
